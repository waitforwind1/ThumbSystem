package com.usst.thumbs.job;

import com.usst.thumbs.common.constant.BlogConstant;
import com.usst.thumbs.common.constant.HotConstant;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.service.BlogService;
import com.usst.thumbs.service.HotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotListRefresh {
    private final StringRedisTemplate stringRedisTemplate;
    private final BlogService blogService;
    private final HotService hotService;
    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 */5 * * * *")
    void refreshHotRank(){
        RLock lock = redissonClient.getLock(HotConstant.HOT_CONTENT_LOCK);
        String temporaryKey = HotConstant.HOT_CONTENT_KEY + ":refresh:" + UUID.randomUUID();
        lock.lock();
        try {
            // 清理旧版本热度公式留下的负分，保证数据库与 Redis 都以 0 为下限。
            blogService.lambdaUpdate()
                    .lt(Blog::getHotScore, 0)
                    .set(Blog::getHotScore, 0.0)
                    .update();
            List<Blog> blogList = blogService.lambdaQuery()
                    .eq(Blog::getStatus, BlogConstant.BLOG_STATUS_PUBLISHED)
                    .orderByDesc(Blog::getUpdateTime)
                    .last("limit 100")
                    .list();
            for (Blog blog : blogList) {
                Double score = hotService.calculateHotScore(blog);
                stringRedisTemplate.opsForZSet().add(temporaryKey, blog.getId().toString(), score);
                blogService.lambdaUpdate()
                        .eq(Blog::getId,blog.getId())
                        .set(Blog::getHotScore,score)
                        .update();
            }
            if (blogList.isEmpty()) {
                stringRedisTemplate.delete(HotConstant.HOT_CONTENT_KEY);
            } else {
                stringRedisTemplate.rename(temporaryKey, HotConstant.HOT_CONTENT_KEY);
            }
            stringRedisTemplate.opsForValue().set(HotConstant.HOT_CONTENT_READY, "1");
        } catch (RuntimeException exception) {
            stringRedisTemplate.delete(temporaryKey);
            log.error("刷新热榜失败", exception);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
