package com.usst.thumbs.service.ServiceImpl;

import com.usst.thumbs.Repository.BlogRepository;
import com.usst.thumbs.common.constant.BlogConstant;
import com.usst.thumbs.common.constant.FavoriteConstant;
import com.usst.thumbs.common.constant.HotConstant;
import com.usst.thumbs.common.constant.ThumbConstant;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.es.BlogEsDoc;
import com.usst.thumbs.service.BlogSearchService;
import com.usst.thumbs.service.BlogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@SpringBootTest
class BlogSearchServiceImplTest {

    @Autowired
    private BlogService blogService;
    @Autowired
    private BlogSearchService blogSearchService;
    @Autowired
    private BlogRepository blogRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * ES 第一次使用初始化全量更新
     */
    @Test
    public void test() {
        List<BlogEsDoc> list = blogService.lambdaQuery()
                .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED)
                .list()
                .stream()
                .map(blog -> {
                    BlogEsDoc document = new BlogEsDoc();
                    BeanUtils.copyProperties(blog, document);
                    return document;
                })
                .toList();
        blogRepository.saveAll(list);
    }

    @Test
    public void init() {
        List<Blog> blogList = blogService.lambdaQuery()
                .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED)
                .list();
        for (Blog blog : blogList) {
            Long blogId = blog.getId();
            Double hotScore = blog.getHotScore();
            redisTemplate.opsForZSet().add(HotConstant.HOT_CONTENT_KEY, blogId.toString(), hotScore);
        }

        for (Blog blog : blogList) {
            Long blogId = blog.getId();
            Long thumbCount = blog.getThumbCount();
            Long favoriteCount = blog.getFavoriteCount();
            redisTemplate.opsForValue().set(ThumbConstant.BLOG_THUMB_COUNT_KEY.formatted(blogId),thumbCount);
            redisTemplate.opsForValue().set(FavoriteConstant.BLOG_FAVORITE_COUNT_KEY.formatted(blogId),favoriteCount);
        }
    }


}