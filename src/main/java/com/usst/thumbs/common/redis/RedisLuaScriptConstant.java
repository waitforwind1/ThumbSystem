package com.usst.thumbs.common.redis;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

// 定义redis执行的lua脚本 定义为常量
public interface RedisLuaScriptConstant {

    // XADD stream  事件
    public static final RedisScript<Long> INTERACTION_STREAM_SCRIPT =
            new DefaultRedisScript<>("""
            local userStateKey = KEYS[1]
            local countKey = KEYS[2]
            local streamKey = KEYS[3]

            local eventId = ARGV[1]
            local userId = ARGV[2]
            local blogId = ARGV[3]
            local targetUserId = ARGV[4]
            local eventType = ARGV[5]
            local action = ARGV[6]
            local occurredAt = ARGV[7]
            local initialCount = ARGV[8]

            redis.call('SETNX', countKey, initialCount)

            if action == '1' then
                if redis.call('HEXISTS', userStateKey, blogId) == 1 then
                    return -1
                end
                redis.call('HSET', userStateKey, blogId, 1)
                redis.call('INCR', countKey)
            else
                if redis.call('HEXISTS', userStateKey, blogId) == 0 then
                    return -1
                end
                redis.call('HDEL', userStateKey, blogId)

                local current = tonumber(redis.call('GET', countKey) or '0')
                if current > 0 then
                    redis.call('DECR', countKey)
                end
            end

            redis.call('XADD', streamKey, '*',
                'eventId', eventId,
                'userId', userId,
                'blogId', blogId,
                'targetUserId', targetUserId,
                'type', eventType,
                'action', action,
                'occurredAt', occurredAt
            )

            return 1
            """, Long.class);

}
