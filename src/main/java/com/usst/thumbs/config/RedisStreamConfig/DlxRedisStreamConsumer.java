package com.usst.thumbs.config.RedisStreamConfig;

import com.usst.thumbs.common.redis.InteractionStreamConstant;
import com.usst.thumbs.service.DeadMessagePersistenceService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlxRedisStreamConsumer {

    private static final String DEAD_CONSUMER = "dead-message-db-consumer";

    private final StringRedisTemplate stringRedisTemplate;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private final DeadMessagePersistenceService deadMessagePersistenceService;

    @PostConstruct
    public void initialize() {
        createGroup();
        container.receive(
                Consumer.from(InteractionStreamConstant.DEAD_GROUP, DEAD_CONSUMER),
                StreamOffset.create(InteractionStreamConstant.DEAD_STREAM_KEY, ReadOffset.lastConsumed()),
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
                List.of(InteractionStreamConstant.DEAD_STREAM_KEY),
                InteractionStreamConstant.DEAD_GROUP
        );
    }

    private void handleMessage(MapRecord<String, String, String> message) {
        try {
            deadMessagePersistenceService.saveRedisStreamDeadMessage(message);
            acknowledge(message);
        } catch (Exception exception) {
            log.error("Redis 死信落库失败, deadMessageId={}", message.getId(), exception);
        }
    }

    /** 本地基础补偿：死信落库失败后，重新读取该固定消费者自己的 Pending。 */
    @Scheduled(fixedDelay = 30_000)
    public void retryPendingDeadMessages() {
        StreamOperations<String, String, String> operations = stringRedisTemplate.opsForStream();
        List<MapRecord<String, String, String>> pending = operations.read(
                Consumer.from(InteractionStreamConstant.DEAD_GROUP, DEAD_CONSUMER),
                StreamReadOptions.empty().count(50),
                StreamOffset.create(InteractionStreamConstant.DEAD_STREAM_KEY, ReadOffset.from("0-0"))
        );
        if (pending == null) {
            return;
        }
        pending.forEach(this::handleMessage);
    }

    private void acknowledge(MapRecord<String, String, String> message) {
        stringRedisTemplate.opsForStream().acknowledge(
                InteractionStreamConstant.DEAD_STREAM_KEY,
                InteractionStreamConstant.DEAD_GROUP,
                message.getId()
        );
    }
}
