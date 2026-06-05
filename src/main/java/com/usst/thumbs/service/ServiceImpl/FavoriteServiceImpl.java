package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.usst.thumbs.common.BlogConstant;
import com.usst.thumbs.common.FavoriteConstant;
import com.usst.thumbs.common.RedisLuaScriptConstant;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.mapper.BlogMapper;
import com.usst.thumbs.mapper.FavoriteMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Favorite;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.DoFavoriteRequest;
import com.usst.thumbs.model.vo.BlogVO;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.FavoriteService;
import com.usst.thumbs.service.InteractionEventService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final InteractionEventService interactionEventService;
    private final Cache<String, BlogVO> blogDetailCache;
    private final Cache<String, List<BlogVO>> blogPageCache;

    public FavoriteServiceImpl(BlogMapper blogMapper, RedisTemplate<String, Object> redisTemplate, InteractionEventService interactionEventService, @Qualifier("blogDetailCache") Cache<String ,BlogVO> blogDetailCache, @Qualifier("blogPageCache") Cache<String, List<BlogVO>> blogPageCache) {
        this.blogMapper = blogMapper;
        this.redisTemplate = redisTemplate;
        this.interactionEventService = interactionEventService;
        this.blogDetailCache = blogDetailCache;
        this.blogPageCache = blogPageCache;
    }

    @Override
    public Boolean doFavorite(DoFavoriteRequest doFavoriteRequest,HttpServletRequest request) {
        checkRequest(doFavoriteRequest);
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getId();
        Long blogId = doFavoriteRequest.getBlogId();
        Blog blog = blogMapper.selectById(blogId);
        if(blog==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"内容不存在");
        String userFavoriteKey = FavoriteConstant.USER_FAVORITE_KEY.formatted(userId);
        String blogFavoriteCountKey = FavoriteConstant.BLOG_FAVORITE_COUNT_KEY.formatted(blogId);
        Long res = redisTemplate.execute(
                RedisLuaScriptConstant.DO_FAVORITE_SCRIPT,
                List.of(userFavoriteKey, blogFavoriteCountKey),
                blogId
        );
        if(res.equals(DUPLICATE_RES))
            throw new BusinessException(ResultType.PARAM_ERROR,"已经收藏过啦");
        interactionEventService.saveFavoriteEvent(userId, blogId, blog.getUserId(), FavoriteConstant.ACTION_ADD);
        removeBlogCache(blogId);
        return true;
    }

    @Override
    public Boolean undoFavorite(DoFavoriteRequest doFavoriteRequest,HttpServletRequest request) {
        checkRequest(doFavoriteRequest);
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getId();
        Long blogId = doFavoriteRequest.getBlogId();
        Blog blog = blogMapper.selectById(blogId);
        if(blog==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"内容不存在");
        String userFavoriteKey = FavoriteConstant.USER_FAVORITE_KEY.formatted(userId);
        String blogFavoriteCountKey = FavoriteConstant.BLOG_FAVORITE_COUNT_KEY.formatted(blogId);
        Long res = redisTemplate.execute(
                RedisLuaScriptConstant.UNDO_FAVORITE_SCRIPT,
                List.of(userFavoriteKey, blogFavoriteCountKey),
                blogId
        );
        if(res.equals(DUPLICATE_RES))
            throw new BusinessException(ResultType.PARAM_ERROR,"还未收藏，不能取消😢😢😢");
        interactionEventService.saveFavoriteEvent(userId, blogId, blog.getUserId(), FavoriteConstant.ACTION_CANCEL);
        removeBlogCache(blogId);
        return true;
    }

    @Override
    public Boolean hasFavorite(Long userId,Long blogId) {
        if(userId==null || blogId==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        String key = FavoriteConstant.USER_FAVORITE_KEY.formatted(userId);
        return redisTemplate.opsForHash().hasKey(key,blogId.toString());
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
        List<Blog> blogList = blogMapper.selectBatchIds(blogIds);
        Map<Long, Blog> blogMap = blogList.stream()
                .collect(Collectors.toMap(Blog::getId, blog -> blog));
        List<Blog> blogs = new ArrayList<>();
        for (Long blogId : blogIds) {
            blogs.add(blogMap.get(blogId));
        }
        return blogs;
    }

    private void checkRequest(DoFavoriteRequest doFavoriteRequest){
        if(doFavoriteRequest==null || doFavoriteRequest.getBlogId()==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
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

    private void removeBlogCache(Long blogId) {
        blogDetailCache.invalidate(BlogConstant.BLOG_DETAIL_LOCAL_KEY.formatted(blogId));
        redisTemplate.delete(BlogConstant.BLOG_DETAIL_LOCAL_KEY.formatted(blogId));
        for (int size : List.of(10, 20, 50)) {
            String pageOneKey = BlogConstant.BLOG_PAGE_KEY.formatted(1, size);
            String pageTwoKey = BlogConstant.BLOG_PAGE_KEY.formatted(2, size);
            redisTemplate.delete(pageOneKey);
            redisTemplate.delete(pageTwoKey);
            blogPageCache.invalidate(pageOneKey);
            blogPageCache.invalidate(pageTwoKey);
        }
    }
}




