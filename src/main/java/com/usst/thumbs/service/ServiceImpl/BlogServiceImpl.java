package com.usst.thumbs.service.ServiceImpl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.usst.thumbs.common.BlogConstant;
import com.usst.thumbs.common.FavoriteConstant;
import com.usst.thumbs.common.UserConstant;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.mapper.BlogMapper;
import com.usst.thumbs.mapper.ShareMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Share;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.BlogAddRequest;
import com.usst.thumbs.model.request.BlogSearchRequest;
import com.usst.thumbs.model.vo.BlogInteractionVO;
import com.usst.thumbs.model.vo.BlogVO;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.BlogService;
import com.usst.thumbs.service.FavoriteService;
import com.usst.thumbs.service.ThumbService;
import com.usst.thumbs.service.UserService;
import com.usst.thumbs.utils.RedisKeyUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.usst.thumbs.common.BlogConstant.DEFAULT_MAX_PAGE_SIZE;
import static com.usst.thumbs.common.BlogConstant.DEFAULT_PAGE_SIZE;
import static com.usst.thumbs.common.UserState.USER_LOGIN_STATE;

/**
* @author 22097
* @description 针对表【blog】的数据库操作Service实现
* @createDate 2026-04-28 20:26:51
*/
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
    implements BlogService{

    @Resource
    private  UserService userService;

    @Resource
    private ThumbService thumbService;

    @Resource
    private FavoriteService favoriteService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private Cache<String,List<BlogVO>> blogPageCache;

    @Resource
    private Cache<String,BlogVO> blogDetailCache;
    
    @Autowired
    private Cache<String, Object> localCache;

    @Resource
    private ShareMapper shareMapper;

    @Override
    public Boolean writeBlog(BlogAddRequest blogAddRequest,HttpServletRequest request) {
        if(request==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"Http请求为空");
        if(blogAddRequest.getContent()==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"内容为空");
        if(blogAddRequest.getTitle()==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"标题为空");
        if(this.lambdaQuery().eq(Blog::getTitle,blogAddRequest.getTitle()).one()!=null)
            throw new BusinessException(ResultType.ALREADY_EXITS_ERROR,"该博客标题已经存在,更换一个");
        User user = getLoginUser(request);
        if(user==null)
            throw new BusinessException(ResultType.NOT_LOGIN,"请先登录");
        Long userId = user.getId();
        Blog blog =Blog.builder()
                .title(blogAddRequest.getTitle())
                .content(blogAddRequest.getContent())
                .coverImage(blogAddRequest.getCoverImage())
                .category(blogAddRequest.getCategory())
                .summary(blogAddRequest.getSummary())
                .tag(blogAddRequest.getTags() == null ? null : String.join(",", blogAddRequest.getTags()))
                .userId(userId)
                .hotScore(0.0)
                .favoriteCount(0L)
                .shareCount(0L)
                .thumbCount(0L)
                .status(1)
                .viewCount(0L)
                .commentCount(0L)
                .isDelete(0)
                .build();
        try {
            boolean save = this.save(blog);
            if(!save){
                throw new BusinessException(ResultType.SYSTEM_ERROR,"发布失败");
            }
        } catch (BusinessException e) {
            throw new DuplicateKeyException("你已经发布过同名的博客了,更换标题");
        }
        removeFirstPageCache();
        return true;
    }

    @Override
    public Boolean updateBlog(Long blogId, BlogAddRequest blogAddRequest, HttpServletRequest request) {
        if (request == null || blogAddRequest == null || blogId == null || blogId <= 0) {
            throw new BusinessException(ResultType.PARAM_ERROR, "参数错误");
        }
        User user = getLoginUser(request);
        Blog oldBlog = this.getById(blogId);
        if (oldBlog == null) {
            throw new BusinessException(ResultType.NOT_FOUND, "文章不存在");
        }
        boolean isAuthor = user.getId().equals(oldBlog.getUserId());
        boolean isAdmin = user.getIsAdmin().equals(UserConstant.USER_IS_ADMIN);
        if (!isAuthor && !isAdmin) {
            throw new BusinessException(ResultType.NO_AUTH, "没有权限修改该文章");
        }
        if (blogAddRequest.getTitle() == null || blogAddRequest.getTitle().isBlank()) {
            throw new BusinessException(ResultType.PARAM_ERROR, "标题为空");
        }
        if (blogAddRequest.getContent() == null || blogAddRequest.getContent().isBlank()) {
            throw new BusinessException(ResultType.PARAM_ERROR, "内容为空");
        }
        Blog sameTitleBlog = this.lambdaQuery()
                .eq(Blog::getTitle, blogAddRequest.getTitle())
                .ne(Blog::getId, blogId)
                .one();
        if (sameTitleBlog != null) {
            throw new BusinessException(ResultType.ALREADY_EXITS_ERROR, "该博客标题已经存在,更换一个");
        }
        boolean updated = this.lambdaUpdate()
                .eq(Blog::getId, blogId)
                .set(Blog::getTitle, blogAddRequest.getTitle())
                .set(Blog::getContent, blogAddRequest.getContent())
                .set(Blog::getCoverImage, blogAddRequest.getCoverImage())
                .set(Blog::getCategory, blogAddRequest.getCategory())
                .set(Blog::getSummary, blogAddRequest.getSummary())
                .set(Blog::getTag, blogAddRequest.getTags() == null ? null : String.join(",", blogAddRequest.getTags()))
                .update();
        if (!updated) {
            throw new BusinessException(ResultType.DATABASE_ERROR, "修改文章失败");
        }
        removeBlogDetailCache(blogId);
        removeFirstPageCache();
        return true;
    }

    @Override
    public Boolean updateBlogStatus(Long blogId, Integer status, HttpServletRequest request) {
        if (request == null || blogId == null || blogId <= 0 || status == null) {
            throw new BusinessException(ResultType.PARAM_ERROR, "参数错误");
        }
        User user = getLoginUser(request);
        if (user == null || user.getIsAdmin() == null || !user.getIsAdmin().equals(UserConstant.USER_IS_ADMIN)) {
            throw new BusinessException(ResultType.NO_AUTH, "无管理员权限");
        }
        boolean updated = this.lambdaUpdate()
                .eq(Blog::getId, blogId)
                .set(Blog::getStatus, status)
                .update();
        if (!updated) {
            throw new BusinessException(ResultType.DATABASE_ERROR, "更新文章状态失败");
        }
        removeBlogDetailCache(blogId);
        removeFirstPageCache();
        return true;
    }

    @Override
    public BlogVO blogDetail(Long blogId, HttpServletRequest httpServletRequest) {
        if(blogId==null || blogId<0)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        if(httpServletRequest==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"请求为空");
        String key = BlogConstant.BLOG_DETAIL_LOCAL_KEY.formatted(blogId);
        BlogVO present = blogDetailCache.getIfPresent(key);
        if(present!=null){
            return fillInteractionStatus(copyBlogVO(present),httpServletRequest);
        }
        Object object = getRedisValueOrDelete(key);
        if(object instanceof BlogVO blogVO){
            blogDetailCache.put(key,blogVO);
            return fillInteractionStatus(blogVO,httpServletRequest);
        }
        Blog one = this.lambdaQuery()
                .eq(Blog::getId, blogId)
                .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED)
                .one();
        if(one==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"博客不存在或者已下架");
        this.lambdaUpdate()
                .eq(Blog::getId,blogId)
                .setSql("view_count = view_count + 1")
                .update();
        BlogVO blogVO = convertToBlogVO(one, httpServletRequest);
        redisTemplate.opsForValue().set(key,blogVO);
        blogDetailCache.put(key,blogVO);
        return blogVO;
    }

    /**
     * 分页查询
     * @param pageNo
     * @param pageSize
     * @param request
     * @return
     */
    @Override
    public List<BlogVO> pageGetBlog(Integer pageNo, Integer pageSize, HttpServletRequest request) {
        int current = pageNo==null||pageNo<=0?BlogConstant.DEFAULT_PAGE_NO:pageNo;
        int size = pageSize==null || pageSize<=0?DEFAULT_PAGE_SIZE:pageSize;
        if(size>BlogConstant.DEFAULT_MAX_PAGE_SIZE){
            size = DEFAULT_MAX_PAGE_SIZE;
        }
        boolean needCache = current<=2;
        String key = BlogConstant.BLOG_PAGE_KEY.formatted(current,size);
        if(needCache){
            List<BlogVO> blogVOList = blogPageCache.getIfPresent(key);
            if(blogVOList!=null)
                return fillInteractionStatus(blogVOList, request);
            Object redisValue = getRedisValueOrDelete(key);
            //todo:这里的stream映射写法
            if(redisValue instanceof List<?> list){
                blogVOList = list.stream()
                        .filter(item->item instanceof BlogVO)
                        .map(object->(BlogVO)object)
                        .toList();
                blogPageCache.put(key,blogVOList);
                return fillInteractionStatus(blogVOList, request);
            }
        }
        Page<Blog> page = new Page<>(current,size);
        Page<Blog> blogPage = this.page(page,new LambdaQueryWrapper<Blog>()
                .eq(Blog::getStatus,BlogConstant.BLOG_STATUS_PUBLISHED)
                .orderByDesc(Blog::getCreateTime));
        if(blogPage==null|| blogPage.getRecords().isEmpty())
            return new ArrayList<>();
        List<BlogVO> blogVOS = blogPage.getRecords().stream()
                .map(blog -> convertToBlogVO(blog, request))
                .toList();
        if(needCache){
            blogPageCache.put(key,blogVOS);
            redisTemplate.opsForValue().set(key,blogVOS);
        }
        return blogVOS;
    }

    @Override
    public Boolean deleteBlog(Long blogId, HttpServletRequest request) {
        User user  = getLoginUser(request);
        if(user==null)
            throw new BusinessException(ResultType.NOT_LOGIN,"请登录后再操作");
        if(blogId==null || blogId<=0)
            throw new BusinessException(ResultType.PARAM_ERROR,"博客ID为空");
        Blog blog = this.getById(blogId);
        if(blog==null)
            throw new BusinessException(ResultType.NOT_FOUND,"博客不存在");
        boolean isAuthor = user.getId().equals(blog.getUserId());
        boolean isAdmin = user.getIsAdmin().equals(UserConstant.USER_IS_ADMIN);
        if(!isAdmin && !isAuthor)
            throw new BusinessException(ResultType.NO_AUTH,"没有权限删除");
        boolean removed = this.removeById(blogId);
        if(!removed)
            throw new BusinessException(ResultType.DATABASE_ERROR,"删除失败");
        removeBlogDetailCache(blogId);
        removeFirstPageCache();
        return true;
    }

    @Override
    public BlogVO convertToBlogVO(Blog blog, HttpServletRequest request) {
        User author = userService.getById(blog.getUserId());
        BlogVO blogVO = BlogVO.builder()
                .id(blog.getId())
                .commentCount(blog.getCommentCount())
                .thumbCount(blog.getThumbCount())
                .viewCount(blog.getViewCount())
                .hasFavorite(false)
                .hasThumb(false)
                .hasShare(false)
                .createTime(blog.getCreateTime())
                .updateTime(blog.getUpdateTime())
                .hotScore(blog.getHotScore())
                .status(blog.getStatus())
                .title(blog.getTitle())
                .content(blog.getContent())
                .coverImage(blog.getCoverImage())
                .category(blog.getCategory())
                .tag(blog.getTag())
                .summary(blog.getSummary())
                .shareCount(blog.getShareCount())
                .favoriteCount(blog.getFavoriteCount())
                .username(author == null ? null : author.getUsername())
                .avatar(author == null ? null : author.getAvatar())
                .userId(blog.getUserId())
                .build();
        return fillInteractionStatus(blogVO, request);
    }

    @Override
    public List<BlogVO> searchBlog(BlogSearchRequest searchRequest, HttpServletRequest httpRequest) {
        if(httpRequest==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        if(searchRequest==null)
            return Collections.emptyList();
        int current = searchRequest.getPageNo()==null ||searchRequest.getPageNo()<=0?1:searchRequest.getPageNo();
        int size = searchRequest.getPageSize()==null || searchRequest.getPageSize()<=0?10:Math.min(searchRequest.getPageSize(),50);
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<Blog>()
                .eq(Blog::getStatus,BlogConstant.BLOG_STATUS_PUBLISHED)
                .eq(searchRequest.getUserId()!=null,Blog::getUserId,searchRequest.getUserId())
                .eq(StrUtil.isNotBlank(searchRequest.getCategory()),Blog::getCategory,searchRequest.getCategory())
                .like(StrUtil.isNotBlank(searchRequest.getTag()),Blog::getTag,searchRequest.getTag())
                .and(StrUtil.isNotBlank(searchRequest.getKeyWord()),w->w
                        .like(Blog::getTitle,searchRequest.getKeyWord())
                        .or()
                        .like(Blog::getContent,searchRequest.getKeyWord()))
                .orderByDesc(Blog::getCreateTime);
        Page<Blog> page = new Page<>(current,size);
        Page<Blog> blogPage = this.page(page, wrapper);
        return blogPage.getRecords().stream()
                .map(blog -> convertToBlogVO(blog, httpRequest))
                .toList();
    }


    @Override
    public List<BlogVO> listByAuthor(Long userId, Integer pageNo, Integer pageSize, HttpServletRequest request) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ResultType.PARAM_ERROR, "用户 id 不合法");
        }
        int current = pageNo == null || pageNo <= 0 ? 1 : pageNo;
        int size = pageSize == null || pageSize <= 0 ? 10 : Math.min(pageSize, 50);
        Page<Blog> page = this.page(new Page<>(current, size), new LambdaQueryWrapper<Blog>()
                .eq(Blog::getUserId, userId)
                .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED)
                .orderByDesc(Blog::getCreateTime));
        return page.getRecords().stream()
                .map(blog -> convertToBlogVO(blog, request))
                .toList();
    }

    @Override
    public BlogInteractionVO getInteractionStatus(Long blogId, HttpServletRequest request) {
        if (blogId == null || blogId <= 0) {
            throw new BusinessException(ResultType.PARAM_ERROR, "博客 id 不合法");
        }
        Blog blog = this.getById(blogId);
        if (blog == null) {
            throw new BusinessException(ResultType.NOT_FOUND, "博客不存在");
        }
        boolean hasThumb = false;
        boolean hasFavorite = false;
        boolean hasShare = false;
        try {
            User loginUser = userService.getLoginUser(request);
            hasThumb = thumbService.hasThumb(loginUser.getId(), blogId);
            hasFavorite = favoriteService.hasFavorite(loginUser.getId(), blogId);
            hasShare = shareMapper.selectCount(new LambdaQueryWrapper<Share>()
                    .eq(Share::getUserId, loginUser.getId())
                    .eq(Share::getBlogId, blogId)) > 0;
        } catch (BusinessException ignored) {
            // 未登录用户只返回计数，不返回个人状态。
        }
        return BlogInteractionVO.builder()
                .blogId(blogId)
                .hasThumb(hasThumb)
                .hasFavorite(hasFavorite)
                .hasShare(hasShare)
                .thumbCount(blog.getThumbCount())
                .favoriteCount(blog.getFavoriteCount())
                .build();
    }

    public void removeBlogDetailCache(Long blogId){
        String key = BlogConstant.BLOG_DETAIL_LOCAL_KEY.formatted(blogId);
        blogDetailCache.invalidate(key);
        redisTemplate.delete(key);
    }

    public void removeFirstPageCache(){
        for(int size:List.of(10,20,50)){
            String pageOneKey = BlogConstant.BLOG_PAGE_KEY.formatted(1,size);
            String pageTwoKey = BlogConstant.BLOG_PAGE_KEY.formatted(2,size);
            redisTemplate.delete(pageOneKey);
            redisTemplate.delete(pageTwoKey);
            blogPageCache.invalidate(pageOneKey);
            blogPageCache.invalidate(pageTwoKey);
        }
    }
    public User getLoginUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        return user;
    }

    public BlogVO fillInteractionStatus(BlogVO blogVo,HttpServletRequest request){
        User loginUser = getLoginUser(request);
        if (loginUser == null || blogVo == null || blogVo.getId() == null) {
            return blogVo;
        }
        Object thumb = redisTemplate.opsForHash()
                .get(RedisKeyUtil.getUserThumbsKey(loginUser.getId()), blogVo.getId().toString());
        Object favorite = redisTemplate.opsForHash()
                .get(FavoriteConstant.USER_FAVORITE_KEY.formatted(loginUser.getId()), blogVo.getId().toString());
        boolean hasShare = shareMapper.selectCount(new LambdaQueryWrapper<Share>()
                .eq(Share::getUserId, loginUser.getId())
                .eq(Share::getBlogId, blogVo.getId())) > 0;
        blogVo.setHasThumb(thumb != null);
        blogVo.setHasFavorite(favorite != null);
        blogVo.setHasShare(hasShare);
        return blogVo;
    }

    public BlogVO clearInteractionStatus(BlogVO blogVo){
        blogVo.setHasFavorite(false);
        blogVo.setHasThumb(false);
        blogVo.setHasShare(false);
        return blogVo;
    }

    private Object getRedisValueOrDelete(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (SerializationException e) {
            redisTemplate.delete(key);
            return null;
        }
    }

    private List<BlogVO> fillInteractionStatus(List<BlogVO> blogVOS, HttpServletRequest request) {
        if (blogVOS == null || blogVOS.isEmpty()) {
            return blogVOS;
        }
        return blogVOS.stream()
                .map(blogVO -> fillInteractionStatus(copyBlogVO(blogVO), request))
                .toList();
    }

    private BlogVO copyBlogVO(BlogVO blogVO) {
        return BlogVO.builder()
                .id(blogVO.getId())
                .userId(blogVO.getUserId())
                .username(blogVO.getUsername())
                .avatar(blogVO.getAvatar())
                .title(blogVO.getTitle())
                .content(blogVO.getContent())
                .coverImage(blogVO.getCoverImage())
                .category(blogVO.getCategory())
                .tag(blogVO.getTag())
                .summary(blogVO.getSummary())
                .thumbCount(blogVO.getThumbCount())
                .viewCount(blogVO.getViewCount())
                .favoriteCount(blogVO.getFavoriteCount())
                .commentCount(blogVO.getCommentCount())
                .shareCount(blogVO.getShareCount())
                .hotScore(blogVO.getHotScore())
                .status(blogVO.getStatus())
                .hasThumb(false)
                .hasFavorite(false)
                .hasShare(false)
                .createTime(blogVO.getCreateTime())
                .updateTime(blogVO.getUpdateTime())
                .build();
    }





}




