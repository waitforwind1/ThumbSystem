package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.common.constant.BlogConstant;
import com.usst.thumbs.common.constant.InteractionEventConstant;
import com.usst.thumbs.common.constant.ThumbConstant;
import com.usst.thumbs.common.redis.InteractionStreamConstant;
import com.usst.thumbs.common.redis.RedisLuaScriptConstant;
import com.usst.thumbs.common.exception.BusinessException;
import com.usst.thumbs.mapper.BlogMapper;
import com.usst.thumbs.mapper.ThumbMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Thumb;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.DoThumbRequest;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.ThumbService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.usst.thumbs.common.constant.ThumbConstant.ACTION_CANCEL;
import static com.usst.thumbs.common.constant.UserState.USER_LOGIN_STATE;

/**
* @author 22097
* @description 针对表【thumbs】的数据库操作Service实现
* @createDate 2026-04-28 21:04:00
*/
@Service("thumbService")
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb>
    implements ThumbService {

    @Resource
    private BlogMapper blogMapper;

    @Autowired
    private  RedisTemplate<String,Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getId();
        Long blogId = doThumbRequest.getBlogid();
        if(blogId == null || blogId <= 0)
            throw new BusinessException(ResultType.PARAM_ERROR,"文章id不合法");
        Blog blog = blogMapper.selectOne(new LambdaQueryWrapper<Blog>()
                .eq(Blog::getId, blogId)
                .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED)
                .eq(Blog::getIsDelete, 0));
        if(blog==null)
            throw new BusinessException(ResultType.NOT_FOUND,"文章不存在或已下架");
        ensureUserThumbStateLoaded(userId);
        String userThumbKey = ThumbConstant.USER_THUMB_KEY.formatted(userId);
        String blogCountKey = ThumbConstant.BLOG_THUMB_COUNT_KEY.formatted(blogId);
        stringRedisTemplate.opsForValue().setIfAbsent(blogCountKey, String.valueOf(blog.getThumbCount()));
        String eventId = UUID.randomUUID().toString();
        Long result = stringRedisTemplate.execute(
                RedisLuaScriptConstant.INTERACTION_STREAM_SCRIPT,
                List.of(userThumbKey,blogCountKey, InteractionStreamConstant.STREAM_KEY),
                eventId,
                String.valueOf(userId),
                String.valueOf(blogId),
                String.valueOf(blog.getUserId()),
                String.valueOf(InteractionEventConstant.THUMB_TYPE),
                String.valueOf(ThumbConstant.ACTION_ADD),
                String.valueOf(System.currentTimeMillis())
        );
        if(result.equals(-1L))
            throw new BusinessException(ResultType.ALREADY_EXITS_ERROR,"不能重复点赞");
        return true;
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getId();
        Long blogId = doThumbRequest.getBlogid();
        if(blogId == null || blogId <= 0)
            throw new BusinessException(ResultType.PARAM_ERROR,"文章id不合法");
        Blog blog = blogMapper.selectOne(new LambdaQueryWrapper<Blog>()
                .eq(Blog::getId, blogId)
                .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED)
                .eq(Blog::getIsDelete, 0));
        if(blog==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"文章不存在或者已下架");
        ensureUserThumbStateLoaded(userId);
        String userThumbKey = ThumbConstant.USER_THUMB_KEY.formatted(userId);
        String blogCountKey = ThumbConstant.BLOG_THUMB_COUNT_KEY.formatted(blogId);
        stringRedisTemplate.opsForValue().setIfAbsent(blogCountKey, String.valueOf(blog.getThumbCount()));
        String eventId = UUID.randomUUID().toString();
        Long res = stringRedisTemplate.execute(
                RedisLuaScriptConstant.INTERACTION_STREAM_SCRIPT,
                List.of(userThumbKey,blogCountKey, InteractionStreamConstant.STREAM_KEY),
                eventId,
                String.valueOf(userId),
                String.valueOf(blogId),
                String.valueOf(blog.getUserId()),
                String.valueOf(InteractionEventConstant.THUMB_TYPE),
                String.valueOf(ACTION_CANCEL),
                String.valueOf(System.currentTimeMillis())
        );
        if(res.equals(-1L))
            throw new BusinessException(ResultType.ALREADY_EXITS_ERROR,"还没有点赞，无法取消");
        return true;
    }

    @Override
    public Boolean hasThumb(Long userId, Long blogId) {
        if(blogId==null || userId==null)
            return false;
        ensureUserThumbStateLoaded(userId);
        String key = ThumbConstant.USER_THUMB_KEY.formatted(userId);
        return redisTemplate.opsForHash().hasKey(key, blogId.toString());
    }


    private User getLoginUser(HttpServletRequest request){
        if(request==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"请求为空");
        Object object = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(object instanceof User user){
            return user;
        }
        throw new BusinessException(ResultType.PARAM_ERROR,"用户未登录");
    }

    private void ensureUserThumbStateLoaded(Long userId) {
        String readyKey = ThumbConstant.USER_THUMB_STATE_READY_KEY.formatted(userId);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(readyKey))) {
            return;
        }
        Map<Object, Object> historicalState = this.list(
                new LambdaQueryWrapper<Thumb>()
                        .eq(Thumb::getUserId, userId))
                            .stream()
                            .collect(Collectors.toMap(
                            thumb -> thumb.getBlogId().toString(),
                            ignored -> "1",
                                 (left, right) -> left));
        if (!historicalState.isEmpty()) {
            redisTemplate.opsForHash().putAll(ThumbConstant.USER_THUMB_KEY.formatted(userId), historicalState);
        }
        stringRedisTemplate.opsForValue().set(readyKey, "1");
    }

}



