package com.usst.thumbs.job;

import com.usst.thumbs.common.constant.BlogIndexEventConstant;
import com.usst.thumbs.common.constant.RabbitMQConstant;
import com.usst.thumbs.mapper.BlogIndexEventMapper;
import com.usst.thumbs.model.BlogIndexEvent;
import com.usst.thumbs.service.BlogIndexEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.usst.thumbs.common.constant.RabbitMQConstant.BLOG_INDEX_EVENT_TYPE;
import static com.usst.thumbs.common.constant.RabbitMQConstant.BLOG_INDEX_ROUTING_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlogEventPublishJob {

    private final BlogIndexEventService blogIndexEventService;
    private final RabbitTemplate rabbitTemplate;
    private final BlogIndexEventMapper blogIndexEventMapper;

    @Scheduled(fixedRate = 3000)
    public void publishBlogEvent() {
        List<BlogIndexEvent> blogIndexEvents = blogIndexEventService.listPublishedEvents(100);
        for (BlogIndexEvent blogIndexEvent : blogIndexEvents) {
            if(blogIndexEvent.getRetryCount() > BlogIndexEventConstant.MAX_RETRY_COUNT){
                blogIndexEventService.markDead(blogIndexEvent.getEventId(), blogIndexEvent.getErrorMsg());
                continue;
            }
            // 发布事件之前 先尝试更新其状态为sending 就是获取锁的类似效果 拿不到就说明有其他线程发布了
            if(!blogIndexEventService.tryMarkSending(blogIndexEvent.getEventId()))
                continue;
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.BLOG_INDEX_EXCHANGE,
                    BLOG_INDEX_ROUTING_KEY,
                    blogIndexEvent,
                    message -> {
                        message.getMessageProperties().setHeader(
                                RabbitMQConstant.RABBITMQ_HEADER_EVENT_ID,
                                blogIndexEvent.getEventId()
                        );
                        message.getMessageProperties().setHeader(
                                RabbitMQConstant.RABBITMQ_HEADER_EVENT_TYPE,
                                BLOG_INDEX_EVENT_TYPE
                        );
                        message.getMessageProperties().setMessageId(blogIndexEvent.getEventId());
                        return message;
                    },
                    new CorrelationData(BLOG_INDEX_EVENT_TYPE+":"+blogIndexEvent.getEventId())
            );
        }
    }

    @Scheduled(fixedDelay = 10000)
    private void recoveryBlogIndexEvent() {
        blogIndexEventService.recoverTimeoutSending();
    }


}
