package com.usst.thumbs.service.ServiceImpl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.Repository.BlogRepository;
import com.usst.thumbs.common.constant.BlogConstant;
import com.usst.thumbs.common.constant.FavoriteConstant;
import com.usst.thumbs.common.constant.ThumbConstant;
import com.usst.thumbs.common.constant.UserConstant;
import com.usst.thumbs.common.exception.BusinessException;
import com.usst.thumbs.common.redis.RedissonConstant;
import com.usst.thumbs.mapper.BlogMapper;
import com.usst.thumbs.mapper.ShareMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Share;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.es.BlogEsDoc;
import com.usst.thumbs.model.request.BlogAddRequest;
import com.usst.thumbs.model.request.BlogSearchRequest;
import com.usst.thumbs.model.vo.BlogInteractionVO;
import com.usst.thumbs.model.vo.BlogVO;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.usst.thumbs.common.constant.BlogConstant.*;
import static com.usst.thumbs.common.constant.BlogIndexEventConstant.DELETE_ACTION;
import static com.usst.thumbs.common.constant.BlogIndexEventConstant.SAVE_ACTION;
import static com.usst.thumbs.common.constant.InteractionEventConstant.*;
import static com.usst.thumbs.common.constant.UserState.USER_LOGIN_STATE;

/**
* @author 22097
* @description 针对表【blog】的数据库操作Service实现
* @createDate 2026-04-28 20:26:51
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
    implements BlogService{

    @Autowired
    private BlogRepository blogRepository;

    @Resource
    private  UserService userService;

    @Resource
    private ThumbService thumbService;

    @Resource
    private FavoriteService favoriteService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private ShareMapper shareMapper;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private BlogIndexEventService blogIndexEventService;
    @Autowired
    private InteractionEventService interactionEventService;

    @Override
    public Boolean addBlog(Blog blog, HttpServletRequest request) {
            boolean save =this.save(blog);
            if(ObjectUtil.isNull(save))
                throw new BusinessException(ResultType.SYSTEM_ERROR,"数据插入失败");
            BlogEsDoc blogEsDoc = new BlogEsDoc();
            BeanUtil.copyProperties(blog,blogEsDoc);
            BlogEsDoc saved = blogRepository.save(blogEsDoc);
            if(ObjectUtil.isNull(saved))
                throw new BusinessException(ResultType.SYSTEM_ERROR,"ES数据同步失败");
            return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean writeBlog(BlogAddRequest blogAddRequest,HttpServletRequest request) {
        if(request==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"Http请求为空");
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
        interactionEventService.saveInitHotEvent(UUID.randomUUID().toString(),blog.getId(),INIT_HOT_TYPE);
        blogIndexEventService.saveBlogIndexEvent(blog.getId(),SAVE_ACTION);
        removeFirstPageCache();
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        blogIndexEventService.saveBlogIndexEvent(blogId,SAVE_ACTION);
        removeBlogDetailCache(blogId);
        removeFirstPageCache();
        interactionEventService.saveDelayDeleteEvent(UUID.randomUUID().toString(),blogId,DELAY_DELETE_TYPE);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        if(status == BLOG_STATUS_PUBLISHED){
            blogIndexEventService.saveBlogIndexEvent(blogId,SAVE_ACTION);
            interactionEventService.saveInitHotEvent(UUID.randomUUID().toString(),blogId,INIT_HOT_TYPE);
        }else if(status == BLOG_STATUS_OFFLINE){
            blogIndexEventService.saveBlogIndexEvent(blogId,DELETE_ACTION);
            interactionEventService.saveDeleteHotEvent(UUID.randomUUID().toString(),blogId,DELETE_HOT_TYPE);
        }

        removeBlogDetailCache(blogId);
        removeFirstPageCache();
        return true;
    }

    // 加入了 缓存空值 和 加入了互斥锁的文章详情的缓存
    @Override
    public BlogVO blogDetail(Long blogId, HttpServletRequest httpServletRequest) {
        if(blogId==null || blogId <= 0)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        if(httpServletRequest==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"请求为空");
        this.lambdaUpdate()
                .eq(Blog::getId,blogId)
                .setSql("view_count = view_count + 1")
                .update();
        String key = BlogConstant.BLOG_DETAIL_KEY.formatted(blogId);
        Object value = redisTemplate.opsForValue().get(key);
        if(value!=null){
            if(value.equals(CACHE_NULL_VALUE))
                return null;
            else{
                return fillInteractionStatus(copyBlogVO((BlogVO) value), httpServletRequest);
            }
        }

        //  Redisson的设置互斥锁 防止缓存击穿
        String locKey = RedissonConstant.REDISSON_LOCK_KEY.formatted(blogId);
        RLock lock = redissonClient.getLock(locKey);

        try {
            boolean locked = lock.tryLock(
                    1,
                    10,
                    TimeUnit.SECONDS
            );

            if(!locked)
                throw new BusinessException(ResultType.SYSTEM_ERROR,"系统繁忙，稍后重试");

            value = redisTemplate.opsForValue().get(key);
            if(value!=null){
                if(value.equals(CACHE_NULL_VALUE))
                    return null;
                else{
                    return fillInteractionStatus(copyBlogVO((BlogVO) value), httpServletRequest);
                }
            }

            Blog one = this.lambdaQuery()
                    .eq(Blog::getId, blogId)
                    .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED)
                    .one();

            // 空值缓存   用于放置redis缓存击穿
            if(one==null){
                redisTemplate.opsForValue().set(
                        key,
                        CACHE_NULL_VALUE,
                        BlogConstant.EMPTY_PAGE_TTL,
                        TimeUnit.MINUTES);
                return null;
            }else{
                BlogVO cachedBlogVO = buildBaseBlogVO(one);
                redisTemplate.opsForValue().set(
                        key,
                        cachedBlogVO,
                        // 过期时间加入随机值  预防缓存雪崩
                        BlogConstant.BLOG_DETAIL_TTL+ ThreadLocalRandom.current().nextInt(1,11),
                        TimeUnit.MINUTES);
                return fillInteractionStatus(copyBlogVO(cachedBlogVO), httpServletRequest);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(
                    ResultType.SYSTEM_ERROR,
                    "获取缓存锁失败"
            );

        } finally {
            // 最后得到锁的线程一定要记得释放锁
            if(lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }

    /**
     * 分页查询
     * @param pageNo
     * @param pageSize
     * @param request
     * @return
     */
    @Override
    public List<BlogVO> pageGetBlog(
            Integer pageNo,
            Integer pageSize,
            HttpServletRequest request) {

        pageNo = pageNo == null || pageNo <= 0
                ? BlogConstant.DEFAULT_PAGE_NO
                : pageNo;

        pageSize = pageSize == null || pageSize <= 0
                ? DEFAULT_PAGE_SIZE
                : pageSize;

        if (pageSize > BlogConstant.DEFAULT_MAX_PAGE_SIZE) {
            pageSize = DEFAULT_MAX_PAGE_SIZE;
        }

        boolean needCache = pageNo <= 2;

        // 第三页以后不缓存
        if (!needCache) {

            Page<Blog> blogPage = this.page(
                    new Page<>(pageNo, pageSize),
                    new LambdaQueryWrapper<Blog>()
                            .eq(Blog::getStatus, BLOG_STATUS_PUBLISHED)
                            .orderByDesc(Blog::getCreateTime)
            );

            List<BlogVO> blogVOS = blogPage.getRecords()
                    .stream()
                    .map(this::buildBaseBlogVO)
                    .toList();

            return fillInteractionStatus(blogVOS, request);
        }

        String key = BlogConstant.BLOG_PAGE_KEY.formatted(
                pageNo,
                pageSize
        );

        // 第一次查询缓存
        // 缓存模型或序列化器升级后，旧值可能无法反序列化；删掉坏缓存并回源，不能让首页直接 500。
        Object object = getRedisValueOrDelete(key);

        // 空值缓存
        if (ObjectUtil.equals(CACHE_NULL_VALUE, object)) {
            return new ArrayList<>();
        }

        // 正常缓存
        if (object instanceof List<?> list) {

            List<BlogVO> cachedBaseList = list.stream()
                    .filter(BlogVO.class::isInstance)
                    .map(BlogVO.class::cast)
                    .toList();

            return fillInteractionStatus(
                    cachedBaseList,
                    request
            );
        }

        // 缓存 miss，获取互斥锁
        RLock lock = redissonClient.getLock(
                RedissonConstant.REDISSON_PAGE_LOCK_KEY
                        .formatted(pageNo, pageSize)
        );

        try {

            boolean tryLock = lock.tryLock(
                    1,
                    TimeUnit.SECONDS
            );

            if (!tryLock) {
                throw new BusinessException(
                        ResultType.SYSTEM_ERROR,
                        "系统繁忙 稍后重试"
                );
            }

            // 拿到锁以后第二次检查缓存
            object = getRedisValueOrDelete(key);

            if (ObjectUtil.equals(CACHE_NULL_VALUE, object)) {
                return new ArrayList<>();
            }

            if (object instanceof List<?> list) {

                List<BlogVO> cachedBaseList = list.stream()
                        .filter(BlogVO.class::isInstance)
                        .map(BlogVO.class::cast)
                        .toList();

                return fillInteractionStatus(
                        cachedBaseList,
                        request
                );
            }

            // Redis 仍然没有，查数据库
            Page<Blog> blogPage = this.page(
                    new Page<>(pageNo, pageSize),
                    new LambdaQueryWrapper<Blog>()
                            .eq(
                                    Blog::getStatus,
                                    BlogConstant.BLOG_STATUS_PUBLISHED
                            )
                            .orderByDesc(Blog::getCreateTime)
            );

            List<BlogVO> baseBlogVOS = blogPage.getRecords()
                    .stream()
                    .map(this::buildBaseBlogVO)
                    .toList();

            // 防缓存穿透
            if (baseBlogVOS.isEmpty()) {

                redisTemplate.opsForValue().set(
                        key,
                        CACHE_NULL_VALUE,
                        BlogConstant.BLOG_PAGE_TTL,
                        TimeUnit.MINUTES
                );

                return new ArrayList<>();
            }

            // 正常缓存 + 随机 TTL 防雪崩
            redisTemplate.opsForValue().set(
                    key,
                    baseBlogVOS,
                    BlogConstant.BLOG_PAGE_TTL
                            + ThreadLocalRandom.current().nextLong(1, 11),
                    TimeUnit.MINUTES
            );

            return fillInteractionStatus(
                    baseBlogVOS,
                    request
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            log.error("获取缓存锁失败", e);

            throw new BusinessException(
                    ResultType.SYSTEM_ERROR,
                    "获取缓存锁失败"
            );

        } finally {

            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        interactionEventService.saveDeleteHotEvent(UUID.randomUUID().toString(),blog.getId(),DELETE_HOT_TYPE);
        blogIndexEventService.saveBlogIndexEvent(blogId,DELETE_ACTION);
        removeBlogDetailCache(blogId);
        removeFirstPageCache();
        return true;
    }

    @Override
    public BlogVO convertToBlogVO(Blog blog, HttpServletRequest request) {
        return fillInteractionStatus(buildBaseBlogVO(blog), request);
    }

    /** Only public, user-independent fields may enter the article Redis cache. */
    private BlogVO buildBaseBlogVO(Blog blog) {
        User author = userService.getById(blog.getUserId());
        return BlogVO.builder()
                .id(blog.getId())
                .commentCount(blog.getCommentCount())
                .thumbCount(blog.getThumbCount())
                .viewCount(blog.getViewCount())
                .createTime(blog.getCreateTime())
                .updateTime(blog.getUpdateTime())
                .hotScore(blog.getHotScore())
                .status(blog.getStatus())
                .hasThumb(false)
                .hasFavorite(false)
                .hasShare(false)
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
                .thumbCount(getRealtimeCount(ThumbConstant.BLOG_THUMB_COUNT_KEY.formatted(blogId), blog.getThumbCount()))
                .favoriteCount(getRealtimeCount(FavoriteConstant.BLOG_FAVORITE_COUNT_KEY.formatted(blogId), blog.getFavoriteCount()))
                .build();
    }

    public void removeBlogDetailCache(Long blogId){
        String key = BlogConstant.BLOG_DETAIL_KEY.formatted(blogId);
        redisTemplate.delete(key);
    }

    public void removeFirstPageCache(){
        for(int size:List.of(10,20,50)){
            String pageOneKey = BlogConstant.BLOG_PAGE_KEY.formatted(1,size);
            String pageTwoKey = BlogConstant.BLOG_PAGE_KEY.formatted(2,size);
            redisTemplate.delete(pageOneKey);
            redisTemplate.delete(pageTwoKey);
        }
    }
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        return user;
    }

    public BlogVO fillInteractionStatus(BlogVO blogVo,HttpServletRequest request){
        if (blogVo == null || blogVo.getId() == null) {
            return blogVo;
        }

        blogVo.setThumbCount(getRealtimeCount(
                ThumbConstant.BLOG_THUMB_COUNT_KEY.formatted(blogVo.getId()), blogVo.getThumbCount()));
        blogVo.setFavoriteCount(getRealtimeCount(
                FavoriteConstant.BLOG_FAVORITE_COUNT_KEY.formatted(blogVo.getId()), blogVo.getFavoriteCount()));

        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            return clearInteractionStatus(blogVo);
        }
        boolean hasShare = shareMapper.selectCount(new LambdaQueryWrapper<Share>()
                .eq(Share::getUserId, loginUser.getId())
                .eq(Share::getBlogId, blogVo.getId())) > 0;
        blogVo.setHasThumb(thumbService.hasThumb(loginUser.getId(), blogVo.getId()));
        blogVo.setHasFavorite(favoriteService.hasFavorite(loginUser.getId(), blogVo.getId()));
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
            log.warn("Redis 缓存反序列化失败，已删除缓存，key={}", key, e);
            return null;
        }
    }

    private Long getRealtimeCount(String key, Long databaseCount) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException ignored) {
                redisTemplate.delete(key);
            }
        }
        return databaseCount == null ? 0L : databaseCount;
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




