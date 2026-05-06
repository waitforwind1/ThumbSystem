package com.usst.thumbs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import jdk.dynalink.linker.support.CompositeGuardingDynamicLinker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisTemplateConfig {
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        // 设置连接器 链接redis服务
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        //使用Jackjson2jsonRedisSerializer序列化值
        // ObjectMapper是Jackson2JsonRedisSerializer的一个重要参数，主要作用是进行Object和Json数据格式之间的转换
        ObjectMapper objectMapper=new ObjectMapper();
        // LaissezFaireSubTypeValidator.instance允许进行转换的类型
        //DefaultTyping.NON_FINAL对于默认的非Final类的对象进行转换
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,ObjectMapper.DefaultTyping.NON_FINAL);
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper,Object.class);

        // 使用String序列化key
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);

        // 提醒springboot redis配置完了，根据配置进行初始化
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }

}
