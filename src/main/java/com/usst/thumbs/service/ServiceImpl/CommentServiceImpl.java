package com.usst.thumbs.service.ServiceImpl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.common.BlogConstant;
import com.usst.thumbs.common.CommentConstant;
import com.usst.thumbs.common.UserConstant;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.mapper.CommentMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Comment;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.PostCommentRequest;
import com.usst.thumbs.model.request.ReplyCommentRequest;
import com.usst.thumbs.model.vo.CommentVO;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.BlogService;
import com.usst.thumbs.service.CommentService;
import com.usst.thumbs.service.InteractionEventService;
import com.usst.thumbs.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.usst.thumbs.common.UserState.USER_LOGIN_STATE;

/**
* @author 22097
* @description 针对表【comment(保存用户评论)】的数据库操作Service实现
* @createDate 2026-05-16 18:18:31
*/
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{

    @Resource
    private BlogService blogService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private InteractionEventService interactionEventService;

    @Autowired
    private UserService userService;

    @Override
    public Boolean postComment(PostCommentRequest postCommentRequest, HttpServletRequest request) {
        if(postCommentRequest==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getId();
        Long blogId = postCommentRequest.getBlogId();
        String content = postCommentRequest.getContent();
        if(blogId==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"博客不存在");
        if(StrUtil.isBlank(content))
            throw new BusinessException(ResultType.PARAM_ERROR,"不允许发布空内容");
        Blog blog = blogService.lambdaQuery()
                .eq(Blog::getId,blogId)
                .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED)
                .one();
        if(blog==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"博客不存在,刷新后查看");
        Comment comment = Comment.builder()
                .userId(userId)
                .rootId(0L)
                .parentId(0L)
                .blogId(blogId)
                .content(content)
                .replyUserId(null)
                .build();
        boolean saved = this.save(comment);
        boolean updated = blogService.lambdaUpdate()
                .eq(Blog::getId, blogId)
                .setSql("comment_count = comment_count+1")
                .update();
        if(!updated || !saved)
            throw new BusinessException(ResultType.DATABASE_ERROR,"评论发表失败");
        removeFirstPageCache(blogId);
        interactionEventService.saveCommentEvent(userId, blogId, blog.getUserId(), comment.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean replyComment(ReplyCommentRequest replyCommentRequest, HttpServletRequest request) {
        if(replyCommentRequest==null || request==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"请求参数为空");
        if(StrUtil.isBlank(replyCommentRequest.getContent()))
            throw new BusinessException(ResultType.PARAM_ERROR,"回复内容为空");
        Blog blog = blogService.getById(replyCommentRequest.getBlogId());
        if(blog==null)
            throw new BusinessException(ResultType.NOT_FOUND,"内容不存在");
        Comment parent = this.getById(replyCommentRequest.getParentId());
        if (parent == null) {
            throw new BusinessException(ResultType.PARAM_ERROR, "被回复评论不存在");
        }
        User loginUser = getLoginUser(request);
        Long blogId = replyCommentRequest.getBlogId();
        Long rootId = parent.getRootId()==null || parent.getRootId()==0?parent.getId():parent.getRootId();
        Comment comment = Comment.builder()
                .content(replyCommentRequest.getContent())
                .userId(loginUser.getId())
                .blogId(blogId)
                .parentId(parent.getId())
                .rootId(rootId)
                .replyUserId(parent.getUserId())
                .isDelete(0)
                .build();
        boolean saved = this.save(comment);
        boolean updated = blogService.lambdaUpdate()
                .eq(Blog::getId,blogId)
                .setSql("comment_count = comment_count+1")
                .update();
        if(!saved || !updated)
            throw new BusinessException(ResultType.DATABASE_ERROR,"回复评论失败");
        removeFirstPageCache(blogId);
        interactionEventService.saveCommentEvent(loginUser.getId(),blogId,parent.getUserId(),comment.getId());
        return true;
    }

    // 分页查询
    @Override
    public List<CommentVO> pageComment(Long blogId, Integer pageNo, Integer pageSize) {
        if(blogId==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        Blog one = blogService.lambdaQuery().eq(Blog::getId, blogId).eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED).one();
        if(one==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"博客不存在，刷新后查看");
        int current = pageNo==null || pageNo<=0 ?CommentConstant.DEFAULT_PAGE_NO:pageNo;
        int size = pageSize==null || pageSize<=0 ?CommentConstant.DEFAULT_PAGE_SIZE:pageSize;
        if(size>=CommentConstant.MAX_PAGE_SIZE)
            size = CommentConstant.MAX_PAGE_SIZE;
        String key = CommentConstant.BLOG_COMMENT_FIRST_PAGE_KEY.formatted(blogId);
        if(current==1){
            Object object = redisTemplate.opsForValue().get(key);
            if(object instanceof List<?> list){
                return list.stream()
                        .filter(item -> item instanceof CommentVO)
                        .map(item -> (CommentVO) item)
                        .toList();
            }
        }
        Page<Comment> page = new Page<>(current,size);
        Page<Comment> commentPage = this.page(page,new LambdaQueryWrapper<Comment>()
                .eq(Comment::getBlogId,blogId)
                .orderByDesc(Comment::getCreateTime));
        List<CommentVO> result = buildCommentTree(commentPage.getRecords());
        if(current==1){
            redisTemplate.opsForValue().set(key,result, CommentConstant.COMMENT_FIRST_PAGE_TTL, TimeUnit.MINUTES);
        }
        return result;
    }

    // 删除评论
    @Override
    public Boolean deleteComment(Long commentId, HttpServletRequest httpRequest) {
        if(httpRequest==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        if( commentId==null || commentId<=0)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数不合法");
        User loginUser = getLoginUser(httpRequest);
        if(loginUser==null)
            throw new BusinessException(ResultType.NOT_LOGIN,"用户未登录");
        Comment comment = this.getById(commentId);
        if(comment==null)
            throw new BusinessException(ResultType.NOT_FOUND,"评论不存在,刷新后查看");
        // 校验用户是否是当前评论的作者  以及是否是管理员
        if(!loginUser.getId().equals(comment.getUserId()) && !loginUser.getIsAdmin().equals(UserConstant.USER_IS_ADMIN))
            throw new BusinessException(ResultType.NO_AUTH,"没有删除权限");
        boolean updated = this.lambdaUpdate()
                .eq(Comment::getId, commentId)
                .set(Comment::getStatus, CommentConstant.COMMENT_IS_DELETE)
                .set(Comment::getContent,"该评论已删除")
                .update();
        if(!updated)
            throw new BusinessException(ResultType.PARAM_ERROR,"删除评论失败");
        blogService.lambdaUpdate()
                .eq(Blog::getId, comment.getBlogId())
                .setSql("comment_count = GREATEST(comment_count -1,0)")
                .update();
        removeFirstPageCache(comment.getBlogId());
        return true;
    }

    @Override
    public List<CommentVO> pageRootComments(Long blogId,Integer pageNo,Integer pageSize) {
        if(blogId==null || blogId<0)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数不合法");
        int current = pageNo==null || pageNo<0?1:pageNo;
        int size = pageSize==null||pageSize<0?10:Math.min(pageSize,50);
        Page<Comment> page = new Page<>(current,size);
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getRootId, blogId)
                .orderByDesc(Comment::getCreateTime);
        Page<Comment> commentPage = this.page(page, queryWrapper);
        List<Comment> commentList = commentPage.getRecords();
        if(commentList.isEmpty())
            return Collections.emptyList();
        return toCommentVO(commentList);
    }

    @Override
    public List<CommentVO> listReplies(Long rootId, Integer pageNo, Integer pageSize) {
        if (rootId == null || rootId <= 0) {
            throw new BusinessException(ResultType.PARAM_ERROR, "根评论 id 不合法");
        }
        int current = pageNo == null || pageNo <= 0 ? 1 : pageNo;
        int size = pageSize == null || pageSize <= 0 ? 10 : Math.min(pageSize, 50);
        Page<Comment> page = this.page(new Page<>(current, size), new LambdaQueryWrapper<Comment>()
                .eq(Comment::getRootId, rootId)
                .orderByAsc(Comment::getCreateTime));
        return toCommentVO(page.getRecords());
    }

    /**
     * 构建评论树 先需要一次遍历找到所有评论 设置为commentVO 再次遍历就可以设置父子关系并添加到返回列表了
     * @param commentList
     * @return
     */
    private List<CommentVO> buildCommentTree(List<Comment> commentList){
        if(commentList==null|| commentList.isEmpty())
            return Collections.emptyList();
        Map<Long,CommentVO> commentMap = new LinkedHashMap<>();
        for (Comment comment : commentList) {
            User user = userService.getById(comment.getUserId());
            User replyUser = comment.getReplyUserId()==null?null:userService.getById(comment.getReplyUserId());
            CommentVO vo = CommentVO.builder()
                    .id(comment.getId())
                    .userId(comment.getUserId())
                    .username(user == null ? null : user.getUsername())
                    .avatar(user == null ? null : user.getAvatar())
                    .replyUserId(comment.getReplyUserId())
                    .replyUsername(replyUser == null ? null : replyUser.getUsername())
                    .content(comment.getContent())
                    .blogId(comment.getBlogId())
                    .parentId(comment.getParentId())
                    .rootId(comment.getRootId())
                    .crteateTime(comment.getCreateTime())
                    .build();
            commentMap.put(vo.getId(),vo);
        }
        List<CommentVO> root = new ArrayList<>();
        for (CommentVO vo : commentMap.values()) {
            if(vo.getParentId()==null || vo.getParentId()==0){
                root.add(vo);
            }else {
                CommentVO parent = commentMap.get(vo.getParentId());
                if(parent!=null){
                    parent.getChildren().add(vo);
                }else {
                    root.add(vo);
                }
            }
        }
        return root;
    }

    private void removeFirstPageCache(Long blogId){
        String key = CommentConstant.BLOG_COMMENT_FIRST_PAGE_KEY.formatted(blogId);
        redisTemplate.delete(key);
    }

    private List<CommentVO> toCommentVO(List<Comment> commentList){
        List<CommentVO> commentVOS = new ArrayList<>();
        Map<Long,CommentVO> commentVOMap = new HashMap<>();
        for (Comment comment : commentList) {
            CommentVO commentVO =CommentVO.builder().build();
            BeanUtils.copyProperties(comment,commentVO);
            User user = userService.getById(comment.getUserId());
            User replyUser = comment.getReplyUserId()==null?null:userService.getById(comment.getReplyUserId());
            commentVO.setUsername(user == null ? null : user.getUsername());
            commentVO.setAvatar(user == null ? null : user.getAvatar());
            commentVO.setReplyUsername(replyUser == null ? null : replyUser.getUsername());
            commentVOMap.put(commentVO.getId(),commentVO);
        }
        // 构建二级树  保持评论的顺序
        for (Comment comment:commentList) {
            CommentVO commentVO = commentVOMap.get(comment.getId());
            if(commentVO.getParentId() == null || commentVO.getParentId()==0){
                commentVOS.add(commentVO);
            }else{
                CommentVO  parentCommentVO = commentVOMap.get(commentVO.getParentId());
                if(parentCommentVO!=null){
                    parentCommentVO.getChildren().add(commentVO);
                }
            }
        }
        return commentVOS;
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




