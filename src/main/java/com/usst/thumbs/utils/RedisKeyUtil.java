package com.usst.thumbs.utils;

import com.usst.thumbs.common.ThumbConstant;

// 封装 易用
public class RedisKeyUtil {
    // 获取点赞记录的Key(redis中的key)
    public static String getUserThumbsKey(Long userid){
        return ThumbConstant.USER_THUMB_KEY.formatted(userid);
    }

    // 获取临时点赞记录Key
    public static String getTempThumbsKey(String time){
        return ThumbConstant.TEMP_THUMB_KEY_PREFIX.formatted(time);
    }
}
