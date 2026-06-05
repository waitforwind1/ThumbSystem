package com.usst.thumbs.common;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

// 定义redis执行的lua脚本 定义为常量
public interface RedisLuaScriptConstant {

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
