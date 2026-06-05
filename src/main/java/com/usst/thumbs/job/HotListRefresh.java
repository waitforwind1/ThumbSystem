package com.usst.thumbs.job;

import com.usst.thumbs.common.BlogConstant;
import com.usst.thumbs.common.HotConstant;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.service.BlogService;
import com.usst.thumbs.service.HotService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HotListRefresh {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private BlogService blogService;

    @Resource
    private HotService hotService;

    @Scheduled(cron = "0 */5 * * * *")
    void refreshHotRank(){
        redisTemplate.delete(HotConstant.HOT_CONTENT_KEY);
        List<Blog> blogList = blogService.lambdaQuery()
                .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED)
                .orderByDesc(Blog::getUpdateTime)
                .last("limit 100")
                .list();
        for (Blog blog : blogList) {
            Double score = hotService.calculateHotScore(blog);
            redisTemplate.opsForZSet().add(HotConstant.HOT_LOCAL_KEY,blog.getId().toString(),score);
            blogService.lambdaUpdate()
                    .eq(Blog::getId,blog.getId())
                    .set(Blog::getHotScore,score)
                    .update();
        }
    }
}
