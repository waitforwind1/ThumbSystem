package com.usst.thumbs.config.rabbitmqConfig;

import com.usst.thumbs.common.RabbitMQConstant;
import com.usst.thumbs.service.BlogIndexEventService;
import com.usst.thumbs.service.InteractionEventService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.usst.thumbs.common.RabbitMQConstant.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final RabbitTemplate rabbitTemplate;

    private final InteractionEventService interactionEventService;
    private final BlogIndexEventService blogIndexEventService;

    @PostConstruct
    public void init(){
        rabbitTemplate.setMandatory(true);

        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData == null) {
                log.error("收到 Confirm，但 eventId 缺失，ack={}, cause={}", ack, cause);
                return;
            }
            String event = correlationData.getId();

            // 判断回退数据  有的话就不能直接设置markSent
            ReturnedMessage returned = correlationData.getReturned();
            if(returned!=null)
                return;

            String[] split = event.split(":", 2);
            String eventType = split[0];
            String eventId = split[1];

            if(ack){
                if(eventType.equals(BLOG_INDEX_EVENT_TYPE)){
                    blogIndexEventService.markSent(eventId);
                }else if(eventType.equals(INTERACTION_EVENT_TYPE) || eventType.equals(DELAY_DELETE_EVENT_TYPE)){
                    interactionEventService.markSent(eventId);
                }
                log.info("MQ 发布确认成功，eventId={}", eventId);
            }else{
                if(eventType.equals(BLOG_INDEX_EVENT_TYPE)){
                    blogIndexEventService.markRetry(eventId);
                }else if(eventType.equals(INTERACTION_EVENT_TYPE) || eventType.equals(DELAY_DELETE_EVENT_TYPE)){
                    interactionEventService.markRetry(eventId);
                }
                log.error("MQ 发布确认失败，eventId={}, cause={}", eventId, cause);
            }
        });

        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            Message message = returnedMessage.getMessage();
            String eventId = message.getMessageProperties().getHeader(RabbitMQConstant.RABBITMQ_HEADER_EVENT_ID) ;
            String eventType = message.getMessageProperties().getHeader(RABBITMQ_HEADER_EVENT_TYPE);
            if(eventType.equals(BLOG_INDEX_EVENT_TYPE)){
                blogIndexEventService.markRetry(eventId);
            }else if(eventType.equals(INTERACTION_EVENT_TYPE) || eventType.equals(DELAY_DELETE_EVENT_TYPE)){
                interactionEventService.markRetry(eventId);
            }
            log.info("找不到queue，eventID = {},消息无法路由",eventId);
        });
    }

    @Bean
    public DirectExchange blogIndexExchange(){
        return new DirectExchange(
                BLOG_INDEX_EXCHANGE,
                true,
                false);
    }

    @Bean
    public Queue blogIndexQueue(){
        Map<String ,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", BLOG_INDEX_DLX_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", BLOG_INDEX_DLX_ROUTING_KEY);
        return QueueBuilder.durable(BLOG_INDEX_QUEUE)
                .withArguments(arguments)
                .build();
    }

    @Bean
    public Binding blogIndexBinding(){
        return BindingBuilder.bind(blogIndexQueue())
                .to(blogIndexExchange())
                .with(RabbitMQConstant.BLOG_INDEX_ROUTING_KEY);
    }

    @Bean
    public DirectExchange blogIndexDlxExchange(){
        return new DirectExchange(
                BLOG_INDEX_DLX_EXCHANGE,
                true,
                false);
    }

    @Bean
    public Queue blogIndexDlxQueue(){
        return QueueBuilder.durable(BLOG_INDEX_DLX_QUEUE).build();
    }

    @Bean
    public Binding blogIndexDlxBinding(){
        return BindingBuilder.bind(blogIndexDlxQueue())
                .to(blogIndexDlxExchange())
                .with(RabbitMQConstant.BLOG_INDEX_DLX_ROUTING_KEY);
    }


    @Bean
    public DirectExchange interactionExchange(){
        return new DirectExchange(RabbitMQConstant.INTERACTION_EXCHANGE,true,false);
    }

    @Bean
    public DirectExchange interactionDlxExchange(){
        return new DirectExchange(RabbitMQConstant.INTERACTION_DLX_EXCHANGE,true,false);
    }

    @Bean
    public Queue interactionQueue(){
        Map<String,Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange",RabbitMQConstant.INTERACTION_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key",RabbitMQConstant.INTERACTION_DLX_ROUTING_KEY);
        return QueueBuilder
                .durable(RabbitMQConstant.INTERACTION_QUEUE)
                .withArguments(args)
                .build();
    }

    @Bean
    public Queue interactionDlxQueue(){
        return QueueBuilder.durable(RabbitMQConstant.INTERACTION_DLX_QUEUE).build();
    }

    @Bean
    public Binding interactonBinding(){
        return BindingBuilder.bind(interactionQueue())
                .to(interactionExchange())
                .with(RabbitMQConstant.INTERACTION_ROUTING_KEY);
    }

    @Bean
    public Binding interactionDlxBinding(){
        return BindingBuilder
                .bind(interactionDlxQueue())
                .to(interactionDlxExchange())
                .with(RabbitMQConstant.INTERACTION_DLX_ROUTING_KEY);

    }

    /**
     * 延迟队列
     * @return
     */
    @Bean
    public Queue delayDeleteQueue(){
        return QueueBuilder.durable(DELAY_DELETE_QUEUE)
                .ttl(500)
                .deadLetterExchange("cache.delete.exchange")
                .deadLetterRoutingKey("cache.delete")
                .build();
    }

    @Bean
    public DirectExchange delayDeleteExchange(){
        return new DirectExchange(DELAY_DELETE_EXCHANGE,true,false);
    }

    @Bean
    public Binding delayDeleteBinding(){
        return BindingBuilder
                .bind(delayDeleteQueue())
                .to(delayDeleteExchange())
                .with(RabbitMQConstant.DELAY_DELETE_ROUTING_KEY);
    }

    /**
     * 真正删除缓存的队列
     * @return
     */
    @Bean
    public DirectExchange cacheDeleteExchange() {
        return new DirectExchange("cache.delete.exchange");
    }

    @Bean
    public Queue cacheDeleteQueue() {
        return QueueBuilder.durable("cache.delete.queue")
                .build();
    }

    @Bean
    public Binding cacheDeleteBinding() {
        return BindingBuilder.bind(cacheDeleteQueue())
                .to(cacheDeleteExchange())
                .with("cache.delete");
    }
}
