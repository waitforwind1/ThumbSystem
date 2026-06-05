package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.usst.thumbs.common.BlogConstant;
import com.usst.thumbs.common.RedisLuaScriptConstant;
import com.usst.thumbs.common.ThumbConstant;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.mapper.BlogMapper;
import com.usst.thumbs.mapper.ThumbMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Thumb;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.DoThumbRequest;
import com.usst.thumbs.model.vo.BlogVO;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.InteractionEventService;
import com.usst.thumbs.service.ThumbService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.usst.thumbs.common.ThumbConstant.ACTION_CANCEL;
import static com.usst.thumbs.common.UserState.USER_LOGIN_STATE;

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
    private Cache<String, BlogVO> blogDetailLocalCache;

    @Resource
    private Cache<String, List<BlogVO>> blogPageCache;

    @Resource
    private InteractionEventService interactionEventService;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getId();
        Long blogId = doThumbRequest.getBlogid();
        if(blogId == null || blogId <= 0)
            throw new BusinessException(ResultType.PARAM_ERROR,"文章id不合法");
        Blog blog = blogMapper.selectById(blogId);
        if(blog==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"1111文章不存在");
        String userThumbKey = ThumbConstant.USER_THUMB_KEY.formatted(userId);
        String tempThumbKey = getCurrentTempThumbKey();
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.THUMB_SCRIPT,
                List.of(tempThumbKey, userThumbKey),
                userId,
                blogId
        );
        if(result.equals(-1L))
            throw new BusinessException(ResultType.ALREADY_EXITS_ERROR,"不能重复点赞");
        redisTemplate.delete(BlogConstant.BLOG_DETAIL_LOCAL_KEY.formatted(blogId));
        blogDetailLocalCache.invalidate(BlogConstant.BLOG_DETAIL_LOCAL_KEY.formatted(blogId));
        removeFirstPageCache();
        interactionEventService.saveThumbEvent(
                userId,
                blogId,
                blog.getUserId(),
                ThumbConstant.ACTION_ADD
        );
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
                .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED));
        if(blog==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"文章不存在或者已下架");
        String userThumbKey = ThumbConstant.USER_THUMB_KEY.formatted(userId);
        String tempThumbKey = getCurrentTempThumbKey();
        Long res = redisTemplate.execute(
                RedisLuaScriptConstant.CANCLE_THUMB_SCRIPT,
                List.of(tempThumbKey, userThumbKey),
                userId,
                blogId
        );
        if(res.equals(-1L))
            throw new BusinessException(ResultType.ALREADY_EXITS_ERROR,"还没有点赞，无法取消");
        blogDetailLocalCache.invalidate(BlogConstant.BLOG_DETAIL_LOCAL_KEY.formatted(blogId));
        redisTemplate.delete(BlogConstant.BLOG_DETAIL_LOCAL_KEY.formatted(blogId));
        removeFirstPageCache();
        interactionEventService.saveThumbEvent(userId, blogId, blog.getUserId(), ACTION_CANCEL);
        return true;
    }

    @Override
    public Boolean hasThumb(Long userId, Long blogId) {
        if(blogId==null || userId==null)
            return false;
        String key = ThumbConstant.USER_THUMB_KEY.formatted(userId);
        return redisTemplate.opsForHash().hasKey(key,blogId.toString());
    }

    private String getCurrentTempThumbKey() {
        LocalDateTime now = LocalDateTime.now();
        int second = (now.getSecond() / 10) * 10;
        String date = now.format(DateTimeFormatter.ofPattern("HH:mm:")) + String.format("%02d", second);
        return com.usst.thumbs.utils.RedisKeyUtil.getTempThumbsKey(date);
    }

    private void removeFirstPageCache() {
        for (int size : List.of(10, 20, 50)) {
            String pageOneKey = BlogConstant.BLOG_PAGE_KEY.formatted(1, size);
            String pageTwoKey = BlogConstant.BLOG_PAGE_KEY.formatted(2, size);
            redisTemplate.delete(pageOneKey);
            redisTemplate.delete(pageTwoKey);
            blogPageCache.invalidate(pageOneKey);
            blogPageCache.invalidate(pageTwoKey);
        }
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

}




