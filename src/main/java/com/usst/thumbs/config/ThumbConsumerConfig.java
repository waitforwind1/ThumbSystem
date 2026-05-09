package com.usst.thumbs.config;

import org.apache.pulsar.client.api.BatchReceivePolicy;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.RedeliveryBackoff;
import org.apache.pulsar.client.impl.MultiplierRedeliveryBackoff;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.pulsar.annotation.PulsarListenerConsumerBuilderCustomizer;

import java.util.concurrent.TimeUnit;

// 配置队列消费者 设置参数
@Configuration
public class ThumbConsumerConfig<T> implements PulsarListenerConsumerBuilderCustomizer<T> {
    @Override
    public void customize(ConsumerBuilder<T> consumerBuilder) {
        consumerBuilder.batchReceivePolicy(BatchReceivePolicy.builder()
                .maxNumMessages(1000)
                .timeout(5000, TimeUnit.MILLISECONDS)
                .build());
    }

    @Bean
    public RedeliveryBackoff negativeAckRedeliveryBackoff(){
        return MultiplierRedeliveryBackoff.builder()
                .minDelayMs(1000)
                .multiplier(2)
                .maxDelayMs(60000)
                .build();
    }

    @Bean
    public RedeliveryBackoff ackTimeoutRedeliveryBackoff(){
        return MultiplierRedeliveryBackoff.builder()
                .minDelayMs(5000)
                .multiplier(3)
                .maxDelayMs(300000)
                .build();
    }

    @Bean
    public DeadLetterPolicy deadLetterPolicy(){
        return DeadLetterPolicy.builder()
                .maxRedeliverCount(3)
                .deadLetterTopic("thumb-dlq-topic")
                .build();
    }
}
