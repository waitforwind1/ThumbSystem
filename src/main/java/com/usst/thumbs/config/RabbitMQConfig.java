package com.usst.thumbs.config;

import com.usst.thumbs.common.RabbitMQConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

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
        args.put("s-dead-letter-routing-key",RabbitMQConstant.INTERACTION_DLX_ROUTING_KEY);
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
                .with(RabbitMQConstant.INTERACTION_ROUTING_KEY);

    }
}
