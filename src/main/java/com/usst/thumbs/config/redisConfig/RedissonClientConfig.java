package com.usst.thumbs.config.redisConfig;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedissonClientConfig {

    private String host;
    private Integer port;
    private String password;
    private Integer database;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setPassword(password)
                .setDatabase(database)
                .setAddress("redis://"+host+":"+port)
                .setConnectionMinimumIdleSize(8)
                .setConnectionPoolSize(32)
                .setTimeout(3000)
                .setConnectTimeout(3000)
                .setClientName("singleRedisServer");
        return Redisson.create(config);
    }
}
