package com.usst.thumbs.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.usst.thumbs.common.InteractionEventConstant;
import com.usst.thumbs.common.RabbitMQConstant;
import com.usst.thumbs.model.DTO.InteractionEventDTO;
import com.usst.thumbs.model.InteractionEvent;
import com.usst.thumbs.service.InteractionEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "thumbs.jobs.interaction-event-publish.enabled", havingValue = "true", matchIfMissing = true)
public class InteractionEventPublishJob {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private InteractionEventService interactionEventService;

    @Scheduled(fixedDelay = 3000)
    public void publishWaitingEvents(){
        List<InteractionEvent> events = interactionEventService.list(new LambdaQueryWrapper<InteractionEvent>()
                .in(InteractionEvent::getStatus, InteractionEventConstant.STATUS_WAIT_SEND, InteractionEventConstant.STATUS_SNED_FAIL)
                .lt(InteractionEvent::getRetryCount, InteractionEventConstant.MAX_RETRY_COUNT)
                .last("limit 100"));
        for (InteractionEvent event : events) {
            try {
                InteractionEventDTO eventDTO = InteractionEventDTO.builder()
                        .eventId(event.getEventId())
                        .userId(event.getUserId())
                        .blogId(event.getBlogId())
                        .targetUserId(event.getTargetUserId())
                        .commentId(event.getCommentId())
                        .action(event.getAction())
                        .type(event.getType())
                        .build();
                rabbitTemplate.convertAndSend(
                        RabbitMQConstant.INTERACTION_EXCHANGE,
                        RabbitMQConstant.INTERACTION_ROUTING_KEY,
                        eventDTO
                );
                interactionEventService.markSent(event.getEventId());
            } catch (AmqpException e) {
                log.info("互动事件发送失败,eventId = {}",event.getEventId());
                interactionEventService.markSendFail(event.getEventId(),e.getMessage());
            }
        }
    }
}
