package com.usst.thumbs.aop;

import com.usst.thumbs.common.enums.RateLimitType;
import com.usst.thumbs.common.exception.BusinessException;
import com.usst.thumbs.model.User;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor {

    private final RedissonClient redissonClient;
    private final UserService userService;

    @Before("@annotation(rateLimit)")
    public void interceptor(RateLimit rateLimit) {

        String key = buildKey(rateLimit);

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        // 初始化限流规则
        rateLimiter.trySetRate(
                RateType.OVERALL,
                rateLimit.rate(),
                Duration.ofSeconds(rateLimit.interval())
        );

        // USER / IP 动态 key 可以续期
        if (rateLimit.type() != RateLimitType.GLOBAL) {
            rateLimiter.expire(Duration.ofHours(1));
        }

        // 获取令牌
        if (!rateLimiter.tryAcquire()) {
            throw new BusinessException(
                    ResultType.SYSTEM_ERROR,
                    "请求过于频繁"
            );
        }
    }


    private String buildKey(RateLimit rateLimit) {

        String prefix =
                "rateLimit:" + rateLimit.key();

        return switch (rateLimit.type()) {
            case USER -> {
                HttpServletRequest request =
                        getRequest();
                User loginUser =
                        userService.getLoginUser(request);
                yield prefix
                        + ":user:"
                        + loginUser.getId();
            }
            case IP -> {
                HttpServletRequest request =
                        getRequest();
                String ip =
                        getClientIp(request);
                yield prefix
                        + ":ip:"
                        + ip;
            }
            case GLOBAL ->
                    prefix + ":global";
        };
    }


    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes)
                        RequestContextHolder
                                .getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(
                    ResultType.SYSTEM_ERROR,
                    "无法获取请求信息"
            );
        }
        return attributes.getRequest();
    }


    private String getClientIp(
            HttpServletRequest request) {
        String ip =
                request.getHeader(
                        "X-Forwarded-For"
                );
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }
        ip = request.getHeader(
                "X-Real-IP"
        );
        if (ip != null
                && !ip.isBlank()
                && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}