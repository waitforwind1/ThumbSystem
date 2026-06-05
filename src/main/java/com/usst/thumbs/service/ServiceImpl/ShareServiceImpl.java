package com.usst.thumbs.service.ServiceImpl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.usst.thumbs.common.BlogConstant;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.mapper.ShareMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Share;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.ShareRequest;
import com.usst.thumbs.model.vo.BlogVO;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.BlogService;
import com.usst.thumbs.service.InteractionEventService;
import com.usst.thumbs.service.ShareService;
import com.usst.thumbs.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 22097
* @description 针对表【share(保存分享内容数据)】的数据库操作Service实现
* @createDate 2026-05-16 18:18:38
*/
@Service
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Share>
    implements ShareService{

    @Resource
    private BlogService blogService;
    @Resource
    private UserService userService;

    @Resource
    private InteractionEventService interactionEventService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private Cache<String, BlogVO> blogDetailCache;
    @Resource
    private Cache<String, List<BlogVO>> blogPageCache;

    @Override
    public Boolean shareBlog(ShareRequest request, HttpServletRequest httpRequest) {
        if (request == null || request.getBlogId() == null) {
            throw new BusinessException(ResultType.PARAM_ERROR, "博客 id 为空");
        }
        if (StrUtil.isBlank(request.getUrl())) {
            throw new BusinessException(ResultType.PARAM_ERROR, "分享链接为空");
        }
        User loginUser = userService.getLoginUser(httpRequest);
        Blog blog = blogService.getById(request.getBlogId());
        if (blog == null) {
            throw new BusinessException(ResultType.NOT_FOUND, "博客不存在");
        }
        Share share = Share.builder()
                .blogId(request.getBlogId())
                .userId(loginUser.getId())
                .url(request.getUrl())
                .path(request.getPath())
                .build();
        boolean saved = this.save(share);
        boolean updated = blogService.lambdaUpdate()
                .eq(Blog::getId, request.getBlogId())
                .setSql("share_count = share_count + 1")
                .update();
        if (!saved || !updated) {
            throw new BusinessException(ResultType.DATABASE_ERROR, "分享失败");
        }
        removeBlogCache(request.getBlogId());
        interactionEventService.saveShareEvent(loginUser.getId(), blog.getId(), blog.getUserId());
        return true;
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




