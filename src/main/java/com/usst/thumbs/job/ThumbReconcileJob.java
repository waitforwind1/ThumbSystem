package com.usst.thumbs.job;

import com.google.common.collect.Sets;
import com.usst.thumbs.common.ThumbsConstant;
import com.usst.thumbs.listener.thumb.msg.ThumbEvent;
import com.usst.thumbs.model.Thumbs;
import com.usst.thumbs.service.ServiceImpl.ThumbServiceMQImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


// 对账任务
@Service
@Slf4j
public class ThumbReconcileJob {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ThumbServiceMQImpl thumbService;
    private final PulsarTemplate<ThumbEvent> pulsarTemplate;

    public ThumbReconcileJob(RedisTemplate<String, Object> redisTemplate, ThumbServiceMQImpl thumbService, PulsarTemplate<ThumbEvent> pulsarTemplate) {
        this.redisTemplate = redisTemplate;
        this.thumbService = thumbService;
        this.pulsarTemplate = pulsarTemplate;
    }

    @Scheduled(cron = "0 0 2 * * *")
    // 此方法目的是为了保证数据一致性，实现逻辑是拿到目前redis分片中的所有blogId，然后拿到mysql中的blogId,对比，数据库中如果缺少了一些数据 那就重新做事件补偿
    public void run(){
        long startTime = System.currentTimeMillis();
        Set<Long> userIds = new HashSet<>();
        String pattern = ThumbsConstant.USER_THUMB_KEY_PREFIX+"*";
        // 这里使用scan可以分批慢慢扫符合要求的 匹配的上的；但是如果使用keys的话，就是一次性找全部的，如果匹配的很多的话就会导致进程阻塞
        try(Cursor<String> cursor = redisTemplate.scan(ScanOptions.scanOptions().match(pattern).count(1000).build())){
            while (cursor.hasNext()){
                String key  = cursor.next();
                Long userid = Long.valueOf(key.replace(ThumbsConstant.USER_THUMB_KEY_PREFIX,""));
                userIds.add(userid);
            }
        }

        userIds.forEach(userId->{
            Set<Long> redisBlogIds = redisTemplate.opsForHash().keys(ThumbsConstant.USER_THUMB_KEY_PREFIX+userId).stream().map(obj->Long.valueOf(obj.toString())).collect(Collectors.toSet());
            Set<Long> mysqlBlogIds = Optional.ofNullable(
                    thumbService.lambdaQuery().eq(Thumbs::getUserId,userId).list())
                    .orElse(new ArrayList<>())
                    .stream()
                    .map(Thumbs::getBlogId)
                    .collect(Collectors.toSet());
            // 这里的Sets.difference(a, b) ，传入两个set集合，返回a中有的，b中没有的set差集
            Set<Long> diffBlogIds = Sets.difference(redisBlogIds,mysqlBlogIds);
            sendCompensationEvents(userId,diffBlogIds);
        });
        log.info("对账任务完成，消耗{}ms",System.currentTimeMillis()-startTime);
    }

    // 构造事件 发送到队列
    private void sendCompensationEvents(Long userId, Set<Long> diffBlogIds) {
        diffBlogIds.forEach(blogId->{
            // 构造事件
            ThumbEvent thumbEvent = new ThumbEvent(userId,blogId, ThumbEvent.EventType.INCR, LocalDateTime.now());
            pulsarTemplate.sendAsync("thumb-topic",thumbEvent)
                    .exceptionally(e->{
                log.error("补偿事件发送失败：userId = {},blogId = {}",userId,blogId,e);
                return null;
            });
        });
    }
}
