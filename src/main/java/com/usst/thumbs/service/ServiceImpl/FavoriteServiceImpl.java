package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.common.BlogConstant;
import com.usst.thumbs.common.FavoriteConstant;
import com.usst.thumbs.common.InteractionEventConstant;
import com.usst.thumbs.common.redis.InteractionStreamConstant;
import com.usst.thumbs.common.redis.RedisLuaScriptConstant;
import com.usst.thumbs.common.exception.BusinessException;
import com.usst.thumbs.mapper.BlogMapper;
import com.usst.thumbs.mapper.FavoriteMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Favorite;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.DoFavoriteRequest;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.usst.thumbs.common.CommonConstant.DUPLICATE_RES;
import static com.usst.thumbs.common.UserState.USER_LOGIN_STATE;

/**
* @author 22097
* @description 针对表【favorite】的数据库操作Service实现
* @createDate 2026-05-16 18:18:35
*/
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite>
    implements FavoriteService{

    private final BlogMapper blogMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public FavoriteServiceImpl(BlogMapper blogMapper, RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
        this.blogMapper = blogMapper;
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Boolean doFavorite(DoFavoriteRequest doFavoriteRequest,HttpServletRequest request) {
        checkRequest(doFavoriteRequest);
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getId();
        Long blogId = doFavoriteRequest.getBlogId();
        Blog blog = findPublishedBlog(blogId);
        if(blog==null)
            throw new BusinessException(ResultType.NOT_FOUND,"文章不存在或已下架");
        ensureUserFavoriteStateLoaded(userId);
        String userFavoriteKey = FavoriteConstant.USER_FAVORITE_KEY.formatted(userId);
        String blogFavoriteCountKey = FavoriteConstant.BLOG_FAVORITE_COUNT_KEY.formatted(blogId);
        stringRedisTemplate.opsForValue().setIfAbsent(blogFavoriteCountKey, String.valueOf(blog.getFavoriteCount()));
        String eventId = UUID.randomUUID().toString();
        Long res = stringRedisTemplate.execute(
                RedisLuaScriptConstant.INTERACTION_STREAM_SCRIPT,
                List.of(userFavoriteKey,blogFavoriteCountKey,InteractionStreamConstant.STREAM_KEY),
                eventId,
                String.valueOf(userId),
                String.valueOf(blogId),
                String.valueOf(blog.getUserId()),
                String.valueOf(InteractionEventConstant.FAVORITE_TYPE),
                String.valueOf(InteractionEventConstant.ACTION_ADD),
                String.valueOf(System.currentTimeMillis())
        );
        if(res.equals(DUPLICATE_RES))
            throw new BusinessException(ResultType.PARAM_ERROR,"已经收藏过啦");
        return true;
    }

    @Override
    public Boolean undoFavorite(DoFavoriteRequest doFavoriteRequest,HttpServletRequest request) {
        checkRequest(doFavoriteRequest);
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getId();
        Long blogId = doFavoriteRequest.getBlogId();
        Blog blog = findPublishedBlog(blogId);
        if(blog==null)
            throw new BusinessException(ResultType.NOT_FOUND,"文章不存在或已下架");
        ensureUserFavoriteStateLoaded(userId);
        String userFavoriteKey = FavoriteConstant.USER_FAVORITE_KEY.formatted(userId);
        String blogFavoriteCountKey = FavoriteConstant.BLOG_FAVORITE_COUNT_KEY.formatted(blogId);
        stringRedisTemplate.opsForValue().setIfAbsent(blogFavoriteCountKey, String.valueOf(blog.getFavoriteCount()));
        String eventId = UUID.randomUUID().toString();
        Long res = stringRedisTemplate.execute(
                RedisLuaScriptConstant.INTERACTION_STREAM_SCRIPT,
                List.of(userFavoriteKey,blogFavoriteCountKey, InteractionStreamConstant.STREAM_KEY),
                eventId,
                String.valueOf(userId),
                String.valueOf(blogId),
                String.valueOf(blog.getUserId()),
                String.valueOf(InteractionEventConstant.FAVORITE_TYPE),
                String.valueOf(InteractionEventConstant.ACTION_CANCEL),
                String.valueOf(System.currentTimeMillis())
        );
        if(res.equals(DUPLICATE_RES))
            throw new BusinessException(ResultType.PARAM_ERROR,"还未收藏，不能取消😢😢😢");
        return true;
    }

    @Override
    public Boolean hasFavorite(Long userId,Long blogId) {
        if(userId==null || blogId==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        ensureUserFavoriteStateLoaded(userId);
        String key = FavoriteConstant.USER_FAVORITE_KEY.formatted(userId);
        return redisTemplate.opsForHash().hasKey(key, blogId.toString());
    }

    @Override
    public List<Blog> listMyFavorite(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getId();
        List<Long> blogIds = this.list(new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId).orderByAsc(Favorite::getCreateTime)).stream()
                .map(Favorite::getBlogId)
                .toList();
        if(blogIds.isEmpty())
            return Collections.emptyList();
        List<Blog> blogList = blogMapper.selectByIds(blogIds);
        Map<Long, Blog> blogMap = blogList.stream()
                .collect(Collectors.toMap(Blog::getId, blog -> blog));
        List<Blog> blogs = new ArrayList<>();
        for (Long blogId : blogIds) {
            Blog blog = blogMap.get(blogId);
            if(blog!=null){
                blogs.add(blog);
            }
        }
        return blogs;
    }

    private void checkRequest(DoFavoriteRequest doFavoriteRequest){
        if(doFavoriteRequest==null || doFavoriteRequest.getBlogId()==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
    }

    private Blog findPublishedBlog(Long blogId) {
        return blogMapper.selectOne(new LambdaQueryWrapper<Blog>()
                .eq(Blog::getId, blogId)
                .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED)
                .eq(Blog::getIsDelete, 0));
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

    private void ensureUserFavoriteStateLoaded(Long userId) {
        String readyKey = FavoriteConstant.USER_FAVORITE_STATE_READY_KEY.formatted(userId);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(readyKey))) {
            return;
        }
        Map<Object, Object> historicalState = this.list(new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId))
                .stream()
                .collect(Collectors.toMap(
                        favorite -> favorite.getBlogId().toString(),
                        ignored -> "1",
                        (left, right) -> left));
        if (!historicalState.isEmpty()) {
            redisTemplate.opsForHash().putAll(FavoriteConstant.USER_FAVORITE_KEY.formatted(userId), historicalState);
        }
        stringRedisTemplate.opsForValue().set(readyKey, "1");
    }
}




