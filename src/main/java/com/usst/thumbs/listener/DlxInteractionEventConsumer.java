package com.usst.thumbs.listener;

import com.usst.thumbs.common.RabbitMQConstant;
import com.usst.thumbs.common.exception.BusinessException;
import com.usst.thumbs.model.DeadMessage;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.DeadMessagePersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DlxInteractionEventConsumer {

    private final DeadMessagePersistenceService deadMessagePersistenceService;

    public DlxInteractionEventConsumer(DeadMessagePersistenceService deadMessagePersistenceService) {
        this.deadMessagePersistenceService = deadMessagePersistenceService;
    }

    @RabbitListener(queues = RabbitMQConstant.INTERACTION_DLX_QUEUE)
    public void dlxInteractionEventConsumer(final Message message) {
        String eventId = message.getMessageProperties().getHeader(RabbitMQConstant.RABBITMQ_HEADER_EVENT_ID);
        String messageId = message.getMessageProperties().getMessageId();
        List<Map<String,Object>> xDeathList  = message.getMessageProperties().getHeader("x-death");
        Map<String, Object> deathInfo = xDeathList.get(0);
        String reason = deathInfo.get("reason").toString();
        Integer count = (Integer) deathInfo.get("count");
        Object time = deathInfo.get("time");
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        String body = new String(
                message.getBody(),
                StandardCharsets.UTF_8
        );
        DeadMessage deadMessage =new DeadMessage();
        deadMessage.setEventId(eventId);
        deadMessage.setDeadMessageId(messageId);
        deadMessage.setDeadReason(reason);
        deadMessage.setDeliveryCount(count);
        deadMessage.setCreateTime((Date) time);
        deadMessage.setMessageType("INTERACTION_DLX");
        log.info(
                "DLQ message, eventId={}, messageId={}, body={}, headers={}",
                eventId,
                messageId,
                body,
                headers
        );
        try {
            deadMessagePersistenceService.save(deadMessage);
        }catch (Exception e) {
            throw new BusinessException(ResultType.SYSTEM_ERROR,"保存死信消息失败");
        }
    }
}
