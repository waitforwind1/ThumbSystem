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

    public static final RedisScript<Long> DO_THUMB_SCRIPT  = new DefaultRedisScript<>("""
            local userThumbKey = KEYS[1]
            local countKey = KEYS[2]
            local blogId = ARGV[1]
            if redis.call("HEXISTS",userThumbKey,blogId)==1 then
                return -1
            end
            redis.call("HSET",userThumbKey,blogId,1)
            redis.call("INCR",countKey)
            return 1
            """,Long.class);

    public static final RedisScript<Long> UNDO_THUMB_SCRIPT = new DefaultRedisScript<>("""
            local userThumbKey = KEYS[1]
            local countKey = KEYS[2]
            local blogId = ARGV[1]
            if redis.call("HEXISTS",userThumbKey,blogId)==0 then
                return -1
            end
            redis.call("HDEL",userThumbKey,blogId)
            
            local number = tonumber(redis.call("GET",countKey) or 0)
            if(number>0) then
                redis.call("DECR",countKey);
            end
            return 1
            """,Long.class);

    public static final RedisScript<Long> DO_FAVORITE_SCRIPT = new DefaultRedisScript<>("""
            local userFavoriteKey = KEYS[1]
            local countKey = KEYS[2]
            local blogId = ARGV[1]
            if redis.call("HEXISTS",userFavoriteKey,blogId)==1 then
                return -1;
            end
            redis.call("HSET",userFavoriteKey,blogId,1)
            redis.call("INCR",countKey)

            return 1
            """,Long.class);

    public static final RedisScript<Long> UNDO_FAVORITE_SCRIPT = new DefaultRedisScript<>("""
            local userFavoriteKey = KEYS[1]
            local countKey = KEYS[2]
            local blogId = ARGV[1]
            if redis.call("HEXISTS",userFavoriteKey,blogId)==0 then
                return -1;
            end
            redis.call("HDEL",userFavoriteKey,blogId)
            local number = tonumber(redis.call("GET",countKey) or 0)
            if(number>0) then
                redis.call("DECR",countKey)
            end
            return 1
            """,Long.class);

    // 点赞操作
    public static final RedisScript<Long>  THUMB_SCRIPT = new DefaultRedisScript<>("""
            local tempThumbKey = KEYS[1]
            local userThumbKey = KEYS[2]
            local userid = ARGV[1]
            local blogId = ARGV[2]
            if redis.call("HEXISTS",userThumbKey,blogId)==1 then 
                return -1
            end
            
            local hashKey = userid..":"..blogId
            local oldNumber = tonumber(redis.call("HGet",tempThumbKey,hashKey) or 0)
            local newNumber = oldNumber + 1
            redis.call("HSET",tempThumbKey,hashKey,newNumber)
            redis.call("HSET",userThumbKey,blogId,1)
            return 1
            """, Long.class);

    // 取消点赞脚本
    public static final RedisScript<Long> CANCLE_THUMB_SCRIPT = new DefaultRedisScript<>("""
            local tempThumbKey = KEYS[1]
            local userThumbKey = KEYS[2]
            local userid = ARGV[1]
            local blogId = ARGV[2]
            if redis.call("HEXISTS",userThumbKey,blogId)==0 then 
                return -1
            end
            
            local hashKey = userid..":"..blogId
            local oldNumber = tonumber(redis.call("HGet",tempThumbKey,hashKey) or 0)
            local newNumber = oldNumber - 1
            redis.call("HSET",tempThumbKey,hashKey,newNumber)
            redis.call("HDEL",userThumbKey,blogId)
            return 1
            """,Long.class);
}
