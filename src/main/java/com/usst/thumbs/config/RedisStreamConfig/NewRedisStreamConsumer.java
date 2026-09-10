package com.usst.thumbs.config.RedisStreamConfig;

import com.usst.thumbs.common.redis.InteractionStreamConstant;
import com.usst.thumbs.service.InteractionStreamPersistenceService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewRedisStreamConsumer {

    private static final String RETRY_CONSUMER = "interaction-retry-consumer";
    private static final int MAX_DELIVERY_COUNT = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(30);
    private static final Duration STREAM_RETENTION = Duration.ofDays(7);
    private static final String CONSUMER_NAME =
            "interaction-consumer-" + UUID.randomUUID().toString().substring(0, 8);

    private final StringRedisTemplate stringRedisTemplate;
    private final InteractionStreamPersistenceService persistenceService;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @PostConstruct
    public void initialize() {
        createGroup();
        container.receive(
                Consumer.from(InteractionStreamConstant.GROUP, CONSUMER_NAME),
                StreamOffset.create(InteractionStreamConstant.STREAM_KEY, ReadOffset.lastConsumed()),
                this::handleMessage
        );
    }

    private void createGroup() {
        stringRedisTemplate.execute(
                new DefaultRedisScript<>("""
                        local result = redis.pcall('XGROUP', 'CREATE', KEYS[1], ARGV[1], '0', 'MKSTREAM')
                        if type(result) == 'table' and result.err and not string.find(result.err, 'BUSYGROUP') then
                            return redis.error_reply(result.err)
                        end
                        return 1
                        """, Long.class),
                List.of(InteractionStreamConstant.STREAM_KEY),
                InteractionStreamConstant.GROUP
        );
    }

    private void handleMessage(MapRecord<String, String, String> record) {
        try {
            persistenceService.persist(record.getValue());
            acknowledge(record.getId());
        } catch (Exception exception) {
            // 不 ACK，消息留在 Pending，等待定时任务重试。
            log.error("Redis Stream 消息首次消费失败, messageId={}", record.getId(), exception);
        }
    }

    @Scheduled(fixedDelay = 10_000)
    public void retryPending() {
        StreamOperations<String, String, String> operations = stringRedisTemplate.opsForStream();
        PendingMessages pendingMessages = operations.pending(
                InteractionStreamConstant.STREAM_KEY,
                InteractionStreamConstant.GROUP,
                Range.unbounded(),
                50
        );
        if (pendingMessages.isEmpty()) {
            return;
        }

        for (PendingMessage pendingMessage : pendingMessages) {
            if (pendingMessage.getElapsedTimeSinceLastDelivery().compareTo(RETRY_DELAY) < 0) {
                continue;
            }
            retryMessage(operations, pendingMessage);
        }
    }

    private void retryMessage(StreamOperations<String, String, String> operations,
                              PendingMessage pendingMessage) {
        List<MapRecord<String, String, String>> records = operations.claim(
                InteractionStreamConstant.STREAM_KEY,
                InteractionStreamConstant.GROUP,
                RETRY_CONSUMER,
                RETRY_DELAY,
                pendingMessage.getId()
        );

        long currentDeliveryCount = pendingMessage.getTotalDeliveryCount() + 1;
        for (MapRecord<String, String, String> record : records) {
            try {
                persistenceService.persist(record.getValue());
                acknowledge(record.getId());
                log.info("Redis Stream 消息重试成功, messageId={}, deliveryCount={}",
                        record.getId(), currentDeliveryCount);
            } catch (Exception exception) {
                if (currentDeliveryCount >= MAX_DELIVERY_COUNT) {
                    moveToDeadStream(operations, record, currentDeliveryCount, exception);
                } else {
                    log.error("Redis Stream 消息重试失败, messageId={}, deliveryCount={}",
                            record.getId(), currentDeliveryCount, exception);
                }
            }
        }
    }

    private void moveToDeadStream(StreamOperations<String, String, String> operations,
                                  MapRecord<String, String, String> originalRecord,
                                  long deliveryCount,
                                  Exception exception) {
        Map<String, String> deadPayload = new HashMap<>(originalRecord.getValue());
        deadPayload.put("originalMessageId", originalRecord.getId().getValue());
        deadPayload.put("deliveryCount", String.valueOf(deliveryCount));
        deadPayload.put("deadReason", buildDeadReason(exception));
        deadPayload.put("deadAt", String.valueOf(System.currentTimeMillis()));

        RecordId deadMessageId = operations.add(InteractionStreamConstant.DEAD_STREAM_KEY, deadPayload);
        if (deadMessageId == null) {
            // 写死信失败时不能 ACK 原消息，否则会丢失。
            throw new IllegalStateException("写入 Redis 死信 Stream 失败");
        }

        acknowledge(originalRecord.getId());
        log.error("Redis Stream 消息进入死信, originalMessageId={}, deadMessageId={}, deliveryCount={}",
                originalRecord.getId(), deadMessageId, deliveryCount, exception);
    }

    private void acknowledge(RecordId messageId) {
        stringRedisTemplate.opsForStream().acknowledge(
                InteractionStreamConstant.STREAM_KEY,
                InteractionStreamConstant.GROUP,
                messageId
        );
    }

    /**
     * A pending record is not yet confirmed in MySQL. Cleanup is therefore
     * skipped whenever the consumer group still has pending messages.
     */
    @Scheduled(cron = "${thumbs.jobs.interaction-stream-cleanup.cron:0 45 3 * * *}")
    public void trimAcknowledgedHistory() {
        StreamOperations<String, String, String> operations = stringRedisTemplate.opsForStream();
        PendingMessages pendingMessages = operations.pending(
                InteractionStreamConstant.STREAM_KEY,
                InteractionStreamConstant.GROUP,
                Range.unbounded(),
                1
        );
        if (!pendingMessages.isEmpty()) {
            log.warn("Skip Redis Stream cleanup because pending records exist, pendingCount={}",
                    pendingMessages.size());
            return;
        }

        String minId = (System.currentTimeMillis() - STREAM_RETENTION.toMillis()) + "-0";
        Long removed = stringRedisTemplate.execute(
                new DefaultRedisScript<>(
                        "return redis.call('XTRIM', KEYS[1], 'MINID', '~', ARGV[1])",
                        Long.class),
                List.of(InteractionStreamConstant.STREAM_KEY),
                minId
        );
        log.info("Trimmed acknowledged Redis Stream history, minId={}, removed={}", minId, removed);
    }

    private String buildDeadReason(Exception exception) {
        Throwable rootCause = exception;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        String reason = rootCause.getClass().getName() + ": "
                + Objects.toString(rootCause.getMessage(), "无异常信息");
        return reason.length() <= 500 ? reason : reason.substring(0, 500);
    }
}
