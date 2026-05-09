package com.usst.thumbs.common;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

// 定义redis执行的lua脚本 定义为常量
public class RedisLuaScriptConstant {

    // 和队列相关的点赞脚本  不做临时记录的redis存储
    public static final RedisScript<Long> THUMB_SCRIPT_MQ  = new DefaultRedisScript<>("""
            local userThumbKey = KEYS[1]
            local blogid = ARGV[1]
            if redis.call("HEXISTS",userThumbKey,blogid)==1 then
                return -1
            end
            
            redis.call("HSET",userThumbKey,blogid,1)
            return 1
            """,Long.class);

    // 和队列相关的取消点赞脚本  不做临时记录的redis存储
    public static final RedisScript<Long> CANCLE_THUMB_SCRIPT_MQ = new DefaultRedisScript<>("""
            local userThumbKey = KEYS[1]
            local blogid = ARGV[1]
            if redis.call("HEXISTS",userThumbKey,blogid)==0 then
                return -1
            end
            
            redis.call("HDEL",userThumbKey,blogid)
            return 1
            
            
            """,Long.class);

    // 点赞操作
    public static final RedisScript<Long>  THUMB_SCRIPT = new DefaultRedisScript<>("""
            local tempThumbKey = KEYS[1]
            local userThumbKey = KEYS[2]
            local userid = ARGV[1]
            local blogid = ARGV[2]
            if redis.call("HEXISTS",userThumbKey,blogid)==1 then 
                return -1
            end
            
            local hashKey = userid..":"..blogid
            local oldNumber = tonumber(redis.call("HGet",tempThumbKey,hashKey) or 0)
            local newNumber = oldNumber + 1
            redis.call("HSET",tempThumbKey,hashKey,newNumber)
            redis.call("HSET",userThumbKey,blogid,1)
            return 1
            """, Long.class);

    // 取消点赞脚本
    public static final RedisScript<Long> CANCLE_THUMB_SCRIPT = new DefaultRedisScript<>("""
            local tempThumbKey = KEYS[1]
            local userThumbKey = KEYS[2]
            local userid = ARGV[1]
            local blogid = ARGV[2]
            if redis.call("HEXISTS",userThumbKey,blogid)==0 then 
                return -1
            end
            
            local hashKey = userid..":"..blogid
            local oldNumber = tonumber(redis.call("HGet",tempThumbKey,hashKey) or 0)
            local newNumber = oldNumber - 1
            redis.call("HSET",tempThumbKey,hashKey,newNumber)
            redis.call("HDEL",userThumbKey,blogid)
            return 1
            """,Long.class);
}
