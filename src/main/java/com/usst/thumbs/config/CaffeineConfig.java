package com.usst.thumbs.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.usst.thumbs.common.BlogConstant;
import com.usst.thumbs.common.HotConstant;
import com.usst.thumbs.model.vo.BlogVO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfig {

    @Bean
    public Cache<String, BlogVO> blogDetailCache(){
        return Caffeine.newBuilder()
                .expireAfterWrite(BlogConstant.BLOG_DETAIL_TTL, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
    }

    @Bean
    public Cache<String, List<BlogVO>> blogPageCache(){
        return Caffeine.newBuilder()
                .expireAfterWrite(BlogConstant.BLOG_PAGE_TTL,TimeUnit.MINUTES)
                .maximumSize(200)
                .build();
    }

    @Bean
    public Cache<String,List<BlogVO>> hotListLocalCache(){
        return Caffeine.newBuilder()
                .expireAfterWrite(HotConstant.LOCAL_CACHE_TTL,TimeUnit.MINUTES)
                .maximumSize(HotConstant.MAX_LIMIT)
                .build();

    }
}
