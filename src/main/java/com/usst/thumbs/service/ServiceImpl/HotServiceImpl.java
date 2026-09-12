package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.usst.thumbs.common.constant.BlogConstant;
import com.usst.thumbs.common.constant.HotConstant;
import com.usst.thumbs.common.exception.BusinessException;
import com.usst.thumbs.mapper.BlogMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.vo.BlogVO;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.BlogService;
import com.usst.thumbs.service.HotService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HotServiceImpl implements HotService {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private BlogService blogService;

    @Resource
    private BlogMapper blogMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> INCREMENT_HOT_SCORE_SCRIPT = new DefaultRedisScript<>("""
            local score = redis.call('ZINCRBY', KEYS[1], ARGV[1], ARGV[2])
            if tonumber(score) < 0 then
                redis.call('ZADD', KEYS[1], 0, ARGV[2])
            end
            return 1
            """, Long.class);
    private RedissonClient redissonClient;

    @Override
    public void deleteHotScore(Long blogId) {
        if(blogId==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        redisTemplate.opsForHash().delete(HotConstant.HOT_CONTENT_KEY,blogId.toString());
    }

    @Override
    public void initBlogHotScore(Long blogId) {
        if(blogId==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        redisTemplate.opsForZSet().add(HotConstant.HOT_CONTENT_KEY,blogId.toString(),0.0);
    }

    @Override
    public void incrHotScore(Long blogId, double score) {
        if(blogId==null || score==0){
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        }
        ensureHotStateLoaded();
        stringRedisTemplate.execute(INCREMENT_HOT_SCORE_SCRIPT,
                List.of(HotConstant.HOT_CONTENT_KEY),
                String.valueOf(score),
                blogId.toString());
        blogMapper.incrementHotScore(blogId, score);
    }

    @Override
    public List<BlogVO> listHot(Integer limit, HttpServletRequest request) {
        int size = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        List<Long> existingIds = new ArrayList<>();
        List<BlogVO> result = new ArrayList<>();
        ensureHotStateLoaded();
        Set<Object> ids = redisTemplate.opsForZSet()
                .reverseRange(HotConstant.HOT_CONTENT_KEY, 0, size - 1);
        if (ids != null && !ids.isEmpty()) {
            List<Long> blogIds = ids.stream()
                    .map(Object::toString)
                    .map(Long::valueOf)
                    .toList();
            List<Blog> blogs = blogService.list(new LambdaQueryWrapper<Blog>()
                    .in(Blog::getId, blogIds)
                    .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED));
            Map<Long, Blog> blogMap = blogs.stream()
                    .collect(Collectors.toMap(Blog::getId, blog -> blog));
            result.addAll(blogIds.stream()
                    .map(blogMap::get)
                    .filter(Objects::nonNull)
                    .map(blog -> blogService.convertToBlogVO(blog, request))
                    .toList());
            existingIds.addAll(blogIds);
            if (result.size() >= size) {
                return result;
            }
        }
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<Blog>()
                .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED);
        if (!existingIds.isEmpty()) {
            queryWrapper.notIn(Blog::getId, existingIds);
        }
        queryWrapper.orderByDesc(Blog::getHotScore)
                .orderByDesc(Blog::getCreateTime);
        Page<Blog> page = blogService.page(new Page<>(1, size - result.size()), queryWrapper);
        result.addAll(page.getRecords().stream()
                .map(blog -> blogService.convertToBlogVO(blog, request))
                .toList());
        return result;
    }

    private void ensureHotStateLoaded(){
        String hotReadyKey = HotConstant.HOT_CONTENT_READY;
        if(redisTemplate.hasKey(hotReadyKey))
            return;
        RLock lock = redissonClient.getLock("lock:hot:content:init");
        lock.lock();
        try {
            List<Blog> blogList = blogService.lambdaQuery()
                    .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED)
                    .orderByDesc(Blog::getHotScore)
                    .last("limit 100")
                    .list();
            for (Blog blog : blogList) {
                Double hotScore = blog.getHotScore();
                if(hotScore== null)
                    hotScore = 0.0;
                hotScore = Math.max(hotScore, 0.0);
                redisTemplate.opsForZSet()
                        .add(HotConstant.HOT_CONTENT_KEY,blog.getId().toString(),hotScore);
            }
            redisTemplate.opsForValue().set(hotReadyKey,"1");
        } finally {
            lock.unlock();
        }

    }

    @Override
    public Double calculateHotScore(Blog blog) {
        if (blog == null) {
            return 0.0;
        }
        long thumb = Optional.ofNullable(blog.getThumbCount()).orElse(0L);
        long favorite = Optional.ofNullable(blog.getFavoriteCount()).orElse(0L);
        long comment = Optional.ofNullable(blog.getCommentCount()).orElse(0L);
        long share = Optional.ofNullable(blog.getShareCount()).orElse(0L);
        long view = Optional.ofNullable(blog.getViewCount()).orElse(0L);
        long ageHours = 0L;
        if (blog.getCreateTime() != null) {
            ageHours = Duration.between(
                    blog.getCreateTime().toInstant(),
                    Instant.now()
            ).toHours();
        }
        double score = thumb * 3.0
                + favorite * 5.0
                + comment * 4.0
                + share * 6.0
                + view
                - ageHours * 0.5;
        return Math.max(score, 0.0);
    }

    @Override
    public List<BlogVO> listHotByCategory(String category, Integer limit, HttpServletRequest request) {
        if (category == null || category.isBlank()) {
            throw new BusinessException(ResultType.PARAM_ERROR, "分类不能为空");
        }

        int size = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);

        String hotCategoryKey = HotConstant.HOT_CONTENT_CATEGORY_KEY.formatted(category);

        Set<Object> ids = redisTemplate.opsForZSet()
                .reverseRange(hotCategoryKey, 0, size - 1);

        List<Long> existingIds = new ArrayList<>();
        List<BlogVO> result = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            List<Long> blogIds = ids.stream()
                    .map(Object::toString)
                    .map(Long::valueOf)
                    .toList();

            List<Blog> blogs = blogService.list(new LambdaQueryWrapper<Blog>()
                    .in(Blog::getId, blogIds)
                    .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED)
                    .eq(Blog::getCategory, category));

            Map<Long, Blog> blogMap = blogs.stream()
                    .collect(Collectors.toMap(Blog::getId, blog -> blog));

            result.addAll(blogIds.stream()
                    .map(blogMap::get)
                    .filter(Objects::nonNull)
                    .map(blog -> blogService.convertToBlogVO(blog, request))
                    .toList());
            existingIds.addAll(blogIds);

            if (result.size() >= size) {
                return result;
            }
        }

        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<Blog>()
                .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED)
                .eq(Blog::getCategory, category);
        if (!existingIds.isEmpty()) {
            queryWrapper.notIn(Blog::getId, existingIds);
        }
        queryWrapper.orderByDesc(Blog::getHotScore)
                .orderByDesc(Blog::getCreateTime);

        Page<Blog> page = blogService.page(new Page<>(1, size - result.size()), queryWrapper);

        result.addAll(page.getRecords().stream()
                .map(blog -> blogService.convertToBlogVO(blog, request))
                .toList());
        return result;
    }
}
