package com.usst.thumbs.job;

import com.usst.thumbs.common.RabbitMQConstant;
import com.usst.thumbs.model.DTO.InteractionEventDTO;
import com.usst.thumbs.model.InteractionEvent;
import com.usst.thumbs.service.InteractionEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.usst.thumbs.common.InteractionEventConstant.DELAY_DELETE_TYPE;
import static com.usst.thumbs.common.RabbitMQConstant.*;

@Slf4j
@Component
@ConditionalOnProperty(name = "thumbs.jobs.interaction-event-publish.enabled", havingValue = "true", matchIfMissing = true)
public class InteractionEventPublishJob {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private InteractionEventService interactionEventService;

    /**
     * 定时任务 每3秒扫描并发放事件任务
     */
    @Scheduled(fixedDelay = 3000)
    public void publishWaitingEvents(){
        for (InteractionEvent event : interactionEventService.listPublishedEvents(100)) {
            if(!interactionEventService.tryMarkSending(event.getEventId()))
                continue;
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
                if(event.getType().equals(DELAY_DELETE_TYPE)){
                    rabbitTemplate.convertAndSend(
                            RabbitMQConstant.DELAY_DELETE_EXCHANGE,
                            DELAY_DELETE_ROUTING_KEY,
                            eventDTO,
                            // 额外传参  eventId 在callback时候可以获取
                            message -> {
                                message.getMessageProperties().setMessageId(event.getEventId());
                                message.getMessageProperties().setHeaders(Map.of(RabbitMQConstant.RABBITMQ_HEADER_EVENT_TYPE,DELAY_DELETE_EVENT_TYPE,RabbitMQConstant.RABBITMQ_HEADER_EVENT_ID,event.getEventId()));
                                return message;
                            },
                            new CorrelationData(DELAY_DELETE_EVENT_TYPE+":"+event.getEventId())
                    );
                }else{
                    rabbitTemplate.convertAndSend(
                            RabbitMQConstant.INTERACTION_EXCHANGE,
                            RabbitMQConstant.INTERACTION_ROUTING_KEY,
                            eventDTO,
                            // 额外传参  eventId 在callback时候可以获取
                            message -> {
                                message.getMessageProperties().setMessageId(event.getEventId());
                                message.getMessageProperties().setHeaders(Map.of(RabbitMQConstant.RABBITMQ_HEADER_EVENT_TYPE,INTERACTION_EVENT_TYPE,RabbitMQConstant.RABBITMQ_HEADER_EVENT_ID,event.getEventId()));
                                return message;
                            },
                            new CorrelationData(INTERACTION_EVENT_TYPE+":"+event.getEventId())
                    );
                }
            } catch (AmqpException e) {
                log.info("互动事件发送失败,eventId = {},重试",event.getEventId());
                interactionEventService.markRetry(event.getEventId());
            }
        }
    }

    /**
     * 每10秒扫描发送超时的事件 设置为retry 重新发送
     */
    @Scheduled(fixedDelay = 10000)
    public void recoverTimeoutSending(){
        interactionEventService.recoverTimeoutSending();
    }
}
