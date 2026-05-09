package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.common.RedisLuaScriptConstant;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.listener.thumb.msg.ThumbEvent;
import com.usst.thumbs.mapper.ThumbsMapper;
import com.usst.thumbs.model.Thumbs;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.enums.LuaStatesEnum;
import com.usst.thumbs.model.request.DoThumbRequest;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.ThumbsService;
import com.usst.thumbs.service.UserService;
import com.usst.thumbs.utils.RedisKeyUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service("thumbService")
public class ThumbServiceMQImpl extends ServiceImpl<ThumbsMapper, Thumbs>
        implements ThumbsService{


    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PulsarTemplate<ThumbEvent> pulsarTemplate;

    public ThumbServiceMQImpl(UserService userService, RedisTemplate<String, Object> redisTemplate, PulsarTemplate<ThumbEvent> pulsarTemplate) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.pulsarTemplate = pulsarTemplate;
    }

    @Override
    public Boolean addThumbs(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        return null;
    }

    @Override
    public Boolean rmThumbs(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        return null;
    }



    // 这里的逻辑是：在脚本执行没有出错并且事件消息发送到队列没有异常后就返回true，对于后边的异步处理消息是否一致则是由后面的一致性保证了
    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if(doThumbRequest==null || request==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        User loginUser = userService.getLoginUser(request);
        if(loginUser==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        Long loginUserId = loginUser.getId();
        Long blogId = doThumbRequest.getBlogid();
        String userThumbKey = RedisKeyUtil.getUserThumbsKey(loginUserId);
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.THUMB_SCRIPT_MQ,
                List.of(userThumbKey),
                blogId
        );
        if(result== LuaStatesEnum.FAIL.getValue())
            throw new BusinessException(ResultType.PARAM_ERROR,"用户已点赞");
        // @Builder注解的作用 链式调用 赋值 更为简便明确
        // 创建一个自定义的点赞事件 用于放入队列 异步处理
        ThumbEvent thumbEvent = ThumbEvent.builder()
                .blogId(blogId)
                .userId(loginUserId)
                .eventType(ThumbEvent.EventType.INCR)
                .eventTime(LocalDateTime.now())
                .build();
        pulsarTemplate.sendAsync("thumb-topic",thumbEvent).exceptionally(ex->{
            redisTemplate.opsForHash().delete(userThumbKey,blogId.toString());
            log.error("点赞事件发送失败:userid={},blogid={}",loginUserId,blogId,ex);
            return null;
        });

        return true;
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if(doThumbRequest==null || request==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        User loginUser = userService.getLoginUser(request);
        if(loginUser==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        Long loginUserId = loginUser.getId();
        Long blogId = doThumbRequest.getBlogid();
        String userThumbKey = RedisKeyUtil.getUserThumbsKey(loginUserId);
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.CANCLE_THUMB_SCRIPT_MQ,
                List.of(userThumbKey),
                blogId
        );
        if(result== LuaStatesEnum.FAIL.getValue())
            throw new BusinessException(ResultType.PARAM_ERROR,"用户未点赞");
        // @Builder注解的作用 链式调用 赋值 更为简便明确
        // 创建一个自定义的点赞事件 用于放入队列 异步处理
        ThumbEvent thumbEvent = ThumbEvent.builder()
                .blogId(blogId)
                .userId(loginUserId)
                .eventType(ThumbEvent.EventType.DECR)
                .eventTime(LocalDateTime.now())
                .build();
        pulsarTemplate.sendAsync("thumb-topic",thumbEvent).exceptionally(ex->{
            redisTemplate.opsForHash().put(userThumbKey,blogId.toString(),1L);
            log.error("点赞事件发送失败:userid={},blogid={}",loginUserId,blogId,ex);
            return null;
        });

        return true;
    }

    @Override
    public Boolean hasThumb(Long userid, Long blogid) {
        if(userid==null || blogid==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数为空");
        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserThumbsKey(userid),blogid.toString());
    }
}




