package com.usst.thumbs.common.redis;

public interface RedissonConstant {

    String REDISSON_LOCK_KEY = "lock:%s";

    String REDISSON_PAGE_LOCK_KEY = "redis:lock:%s:%s";
}
