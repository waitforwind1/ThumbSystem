package com.usst.thumbs.job;

import com.usst.thumbs.common.constant.BlogConstant;
import com.usst.thumbs.mapper.BlogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/** Rebuilds denormalized counters from the authoritative interaction relations. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "thumbs.jobs.interaction-count-reconcile.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class InteractionCountReconcileJob {

    private static final String LOCK_KEY = "lock:interaction-count-reconcile";
    private static final int BATCH_SIZE = 500;

    private final BlogMapper blogMapper;
    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void reconcileOnStartup() {
        reconcile();
    }

    @Scheduled(cron = "${thumbs.jobs.interaction-count-reconcile.cron:0 30 3 * * *}")
    public void reconcile() {
        RLock lock = redissonClient.getLock(LOCK_KEY);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 30, TimeUnit.MINUTES);
            if (!locked) {
                return;
            }

            List<Long> mismatchIds = blogMapper.selectInteractionCountMismatchIds(BATCH_SIZE);
            for (Long blogId : mismatchIds) {
                blogMapper.reconcileInteractionCounts(blogId);
                redisTemplate.delete(BlogConstant.BLOG_DETAIL_KEY.formatted(blogId));
            }
            if (!mismatchIds.isEmpty()) {
                for (int size : List.of(10, 20, 50)) {
                    redisTemplate.delete(BlogConstant.BLOG_PAGE_KEY.formatted(1, size));
                    redisTemplate.delete(BlogConstant.BLOG_PAGE_KEY.formatted(2, size));
                }
                log.warn("Reconciled interaction counters for {} blog(s): {}", mismatchIds.size(), mismatchIds);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Interaction count reconciliation was interrupted", exception);
        } catch (Exception exception) {
            log.error("Interaction count reconciliation failed", exception);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
