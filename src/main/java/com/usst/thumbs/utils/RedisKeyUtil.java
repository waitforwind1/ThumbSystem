package com.usst.thumbs.utils;

import com.usst.thumbs.common.ThumbsConstant;

// 封装 易用
public class RedisKeyUtil {
    // 获取点赞记录的Key(redis中的key)
    public static String getUserThumbsKey(Long userid){
        return ThumbsConstant.USER_THUMB_KEY_PREFIX+userid;
    }

    // 获取临时点赞记录Key
    public static String getTempThumbsKey(String time){
        return ThumbsConstant.TEMP_THUMB_KEY_PREFIX.formatted(time);
    }
}
