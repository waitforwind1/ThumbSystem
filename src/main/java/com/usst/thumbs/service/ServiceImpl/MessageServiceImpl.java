package com.usst.thumbs.service.ServiceImpl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.common.MessageConstant;
import com.usst.thumbs.common.UserConstant;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.mapper.MessageMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.DTO.InteractionEventDTO;
import com.usst.thumbs.model.Message;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.vo.MessageVO;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.BlogService;
import com.usst.thumbs.service.MessageService;
import com.usst.thumbs.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.usst.thumbs.common.UserState.USER_LOGIN_STATE;

/**
* @author 22097
* @description 针对表【message(message站内消息表)】的数据库操作Service实现
* @createDate 2026-05-29 18:58:04
*/
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService{

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserService userService;
    private final BlogService blogService;

    public MessageServiceImpl(RedisTemplate<String, Object> redisTemplate, UserService userService, BlogService blogService) {
        this.redisTemplate = redisTemplate;
        this.userService = userService;
        this.blogService = blogService;
    }

    @Override
    public void createThumbMessage(InteractionEventDTO event) {
        User user = userService.getById(event.getUserId());
        Blog blog = blogService.getById(event.getBlogId());
        createInteractionMessage(event, MessageConstant.TYPE_THUMB, "点赞通知", "用户"+user.getUsername()+"点赞了你的博客"+blog.getTitle());
    }

    @Override
    public void createCommentMessage(InteractionEventDTO event) {
        User user = userService.getById(event.getUserId());
        Blog blog = blogService.getById(event.getBlogId());
        createInteractionMessage(event, MessageConstant.TYPE_COMMENT, "评论通知", "用户"+user.getUsername()+"评论了你的博客"+blog.getTitle());
    }

    @Override
    public void createReplyMessage(InteractionEventDTO event) {
        User user = userService.getById(event.getUserId());

        createInteractionMessage(event, MessageConstant.TYPE_REPLY, "回复通知", "用户"+user.getUsername()+"回复了你的评论");
    }

    @Override
    public void createFavoriteMessage(InteractionEventDTO event) {
        User user = userService.getById(event.getUserId());
        Blog blog = blogService.getById(event.getBlogId());
        createInteractionMessage(event, MessageConstant.TYPE_FAVORITE, "收藏通知", "用户"+user.getUsername()+"点赞了你的博客"+blog.getTitle());
    }

    @Override
    public void createShareMessage(InteractionEventDTO event) {
        User user = userService.getById(event.getUserId());
        Blog blog = blogService.getById(event.getBlogId());
        createInteractionMessage(event,MessageConstant.TYPE_SHARE,"分享通知","用户"+user.getUsername()+"分享了你的博客"+blog.getTitle());
    }

    void createInteractionMessage(InteractionEventDTO eventDTO,Integer type,String title,String content){
        if(eventDTO==null ||eventDTO.getUserId() ==null||eventDTO.getTargetUserId()==null)
            return;
        if(eventDTO.getTargetUserId().equals(eventDTO.getUserId()))
            return;
        Message message = Message.builder()
                .senderId(eventDTO.getUserId())
                .receiverId(eventDTO.getTargetUserId())
                .commentId(eventDTO.getCommentId())
                .isDelete(0)
                .blogId(eventDTO.getBlogId())
                .content(content)
                .type(type)
                .title(title)
                .build();
        boolean saved = this.save(message);
        if(saved){
            redisTemplate.opsForValue().increment(MessageConstant.MESSAGE_UNREAD_KEY.formatted(eventDTO.getTargetUserId()));
        }
    }

    /**
     * 获取未读消息数量
     * @param request
     * @return
     */
    @Override
    public Long getUnReadCount(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        String key = MessageConstant.MESSAGE_UNREAD_KEY.formatted(loginUser.getId());
        Object object = redisTemplate.opsForValue().get(key);
        if(object!=null){
            return Long.valueOf(object.toString());
        }
        long count = this.count(new LambdaQueryWrapper<Message>()
                .eq(Message::getIsRead, MessageConstant.UNREAD)
                .eq(Message::getReceiverId, loginUser.getId()));
        redisTemplate.opsForValue().set(key,count);
        return count;
    }

    @Override
    public List<MessageVO> pageMessage(Integer pageNo, Integer pageSize, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        int current = pageNo==null || pageNo<=0? MessageConstant.DEFAULT_PAGE_NO:pageNo;
        int size = pageSize==null || pageSize<=0? MessageConstant.DEFAULT_PAGE_SIZE:pageSize;
        if(size>MessageConstant.MAX_PAGE_SIZE){
            size = MessageConstant.MAX_PAGE_SIZE;
        }
        Page<Message> page = new Page<>(current,size);
        List<Message> messageList = this.page(page,new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, loginUser.getId())
                .orderByDesc(Message::getCreateTime)).getRecords();
        if(messageList==null || messageList.isEmpty()){
            return Collections.emptyList();
        }
        return toVo(messageList);
    }

    private List<MessageVO> toVo(List<Message> messageList){
        List<MessageVO> messageVOList = new ArrayList<>();
        for (Message message : messageList) {
            User user = userService.getById(message.getSenderId());
            MessageVO messageVO = MessageVO.builder()
                    .id(message.getId())
                    .isRead(message.getIsRead())
                    .senderId(message.getSenderId())
                    .senderName(user==null?null:user.getUsername())
                    .sendAvatar(user==null? null:user.getAvatar())
                    .createTime(message.getCreateTime())
                    .title(message.getTitle())
                    .type(message.getType())
                    .blogId(message.getBlogId())
                    .content(message.getContent())
                    .commentId(message.getCommentId())
                    .build();
            messageVOList.add(messageVO);
        }
        return messageVOList;
    }

    @Override
    public Boolean readMessage(HttpServletRequest request, Long messageId) {
        User loginUser = getLoginUser(request);
        Message message = this.getById(messageId);
        if(message==null || !message.getReceiverId().equals(loginUser.getId()))
            throw new BusinessException(ResultType.PARAM_ERROR,"消息为空");
        if(message.getIsRead()!=null && message.getIsRead().equals(MessageConstant.READ))
            return true;
        boolean updated = this.lambdaUpdate().eq(Message::getReceiverId, loginUser.getId())
                .eq(Message::getId, messageId)
                .set(Message::getIsRead, MessageConstant.READ)
                .update();
        if(!updated) {
            throw new BusinessException(ResultType.DATABASE_ERROR, "设置已读失败");
        }
        String key = MessageConstant.MESSAGE_UNREAD_KEY.formatted(loginUser.getId());
        Object object = redisTemplate.opsForValue().get(key);
        if(object!=null && Long.parseLong(object.toString())>0) {
            redisTemplate.opsForValue().decrement(key);
        }
        return true;
    }

    @Override
    public Boolean readAll(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        String key = MessageConstant.MESSAGE_UNREAD_KEY.formatted(loginUser.getId());
        this.lambdaUpdate().eq(Message::getReceiverId,loginUser.getId())
                .eq(Message::getIsRead,MessageConstant.UNREAD)
                .set(Message::getIsRead,MessageConstant.READ)
                .update();
        redisTemplate.opsForValue().set(key,0);
        return true;
    }

    @Override
    public Boolean deleteMessage(Long messageId,HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        Message message = this.lambdaQuery().eq(Message::getId, messageId).one();
        if(!loginUser.getIsAdmin().equals(UserConstant.USER_IS_ADMIN) && !loginUser.getId().equals(message.getReceiverId()))
            throw new BusinessException(ResultType.NO_AUTH,"没有权限删除");
        boolean removed = this.removeById(messageId);
        if(!removed)
            throw new BusinessException(ResultType.DATABASE_ERROR,"删除失败");
        if(message.getIsRead().equals(MessageConstant.UNREAD)){
            String key = MessageConstant.MESSAGE_UNREAD_KEY.formatted(messageId);
            Object value = redisTemplate.opsForValue().get(key);
            if(value!=null && Long.parseLong(value.toString())>0){
                redisTemplate.opsForValue().decrement(MessageConstant.MESSAGE_UNREAD_KEY.formatted(messageId));
            }
        }
        return true;
    }
    @Override
    public Boolean createSystemMessage(Long receiverId, String title, String content) {
        if (receiverId == null || StrUtil.isBlank(title) || StrUtil.isBlank(content)) {
            throw new BusinessException(ResultType.PARAM_ERROR, "系统消息参数错误");
        }
        Message message = Message.builder()
                .senderId(null)
                .receiverId(receiverId)
                .type(MessageConstant.TYPE_SYSTEM)
                .title(title)
                .content(content)
                .isRead(MessageConstant.UNREAD)
                .isDelete(0)
                .build();
        boolean saved = this.save(message);
        if (saved) {
            redisTemplate.opsForValue().increment(MessageConstant.MESSAGE_UNREAD_KEY.formatted(receiverId));
        }
        return saved;
    }

    private User getLoginUser(HttpServletRequest request){
        if(request==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数为空");
        Object object = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(object instanceof User user){
            return user;
        }
        throw new BusinessException(ResultType.NOT_LOGIN,"用户未登录");
    }
}




