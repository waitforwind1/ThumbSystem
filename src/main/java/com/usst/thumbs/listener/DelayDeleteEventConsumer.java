package com.usst.thumbs.listener;

import com.usst.thumbs.common.BlogConstant;
import com.usst.thumbs.model.DTO.InteractionEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@RequiredArgsConstructor
public class DelayDeleteEventConsumer {

    private final RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = "cache.delete.queue")
    public void process(final InteractionEventDTO eventDTO) {
        redisTemplate.delete(BlogConstant.BLOG_DETAIL_KEY.formatted(eventDTO.getBlogId()));
        return;
    }
}
