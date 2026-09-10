package com.usst.thumbs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usst.thumbs.mapper.DeadMessageMapper;
import com.usst.thumbs.model.DeadMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeadMessagePersistenceService extends ServiceImpl<DeadMessageMapper, DeadMessage> {

    private static final String REDIS_STREAM_MESSAGE_TYPE = "REDIS_STREAM";
    private static final int STATUS_PENDING = 0;

    private final DeadMessageMapper deadMessageMapper;
    private final ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public void saveRedisStreamDeadMessage(MapRecord<String, String, String> record) {
        String deadMessageId = record.getId().getValue();
        Long existing = deadMessageMapper.selectCount(
                new LambdaQueryWrapper<DeadMessage>()
                        .eq(DeadMessage::getMessageType, REDIS_STREAM_MESSAGE_TYPE)
                        .eq(DeadMessage::getDeadMessageId, deadMessageId)
        );
        if (existing != null && existing > 0) {
            return;
        }

        Map<String, String> value = record.getValue();
        Map<String, String> originalPayload = new LinkedHashMap<>(value);
        originalPayload.remove("originalMessageId");
        originalPayload.remove("deliveryCount");
        originalPayload.remove("deadReason");
        originalPayload.remove("deadAt");
        DeadMessage deadMessage = DeadMessage.builder()
                .eventId(value.get("eventId"))
                .originalMessageId(value.get("originalMessageId"))
                .deadMessageId(deadMessageId)
                .messageType(REDIS_STREAM_MESSAGE_TYPE)
                .deliveryCount(parseDeliveryCount(value.get("deliveryCount")))
                .deadReason(value.get("deadReason"))
                .payload(toJson(originalPayload))
                .status(STATUS_PENDING)
                .build();

        if (deadMessageMapper.insert(deadMessage) != 1) {
            throw new IllegalStateException("Redis 死信写入 dead_message 表失败");
        }
    }

    private Integer parseDeliveryCount(String value) {
        try {
            return value == null ? 0 : Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private String toJson(Map<String, String> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("死信 payload 序列化失败", exception);
        }
    }
}
