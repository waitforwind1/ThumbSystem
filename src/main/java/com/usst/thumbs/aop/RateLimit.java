package com.usst.thumbs.aop;

import com.usst.thumbs.common.enums.RateLimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface  RateLimit {
    String key() default "";
    int rate() default 10;
    int interval() default 5;
    RateLimitType type() default RateLimitType.USER;
}
