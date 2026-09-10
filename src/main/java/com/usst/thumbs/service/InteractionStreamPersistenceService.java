package com.usst.thumbs.service;

import com.usst.thumbs.common.InteractionEventConstant;
import com.usst.thumbs.common.redis.InteractionStreamConstant;
import com.usst.thumbs.mapper.BlogMapper;
import com.usst.thumbs.mapper.ConsumeRecordsMapper;
import com.usst.thumbs.mapper.FavoriteMapper;
import com.usst.thumbs.mapper.ThumbMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Persists Redis Stream interaction events. The event record, business change and
 * MySQL outbox row deliberately share one local transaction.
 */
@Service
@RequiredArgsConstructor
public class InteractionStreamPersistenceService {

    private final ConsumeRecordsMapper consumeRecordsMapper;
    private final ThumbMapper thumbMapper;
    private final FavoriteMapper favoriteMapper;
    private final BlogMapper blogMapper;
    private final InteractionEventService interactionEventService;

    @Transactional(rollbackFor = Exception.class)
    public void persist(Map<String,String> event) {
        String eventId = value(event, "eventId");
        Long userId = Long.valueOf(value(event, "userId"));
        Long blogId = Long.valueOf(value(event, "blogId"));
        Long targetUserId = Long.valueOf(value(event, "targetUserId"));
        int type = Integer.parseInt(value(event, "type"));
        int action = Integer.parseInt(value(event, "action"));

        int firstConsume = consumeRecordsMapper.insertConsumeRecords(
                eventId, InteractionStreamConstant.PERSIST_CONSUMER_NAME);
        if (firstConsume == 0) {
            return;
        }

        boolean changed = switch (type) {
            case InteractionEventConstant.THUMB_TYPE -> persistThumb(userId, blogId, action);
            case InteractionEventConstant.FAVORITE_TYPE -> persistFavorite(userId, blogId, action);
            default -> throw new IllegalArgumentException("不支持的互动类型: " + type);
        };

        // Stream retry、历史数据修复可能使 MySQL 已处于目标状态；此时无需重复分发副作用。
        if (!changed) {
            return;
        }

        // The relationship row and its denormalized blog counter must commit or
        // roll back together. RabbitMQ handles only secondary side effects.
        long delta = action == InteractionEventConstant.ACTION_ADD ? 1L : -1L;
        if (type == InteractionEventConstant.THUMB_TYPE) {
            blogMapper.batchUpdateThumbCount(Map.of(blogId, delta));
        } else {
            blogMapper.batchUpdateFavoriteCount(Map.of(blogId, delta));
        }

        if (type == InteractionEventConstant.THUMB_TYPE) {
            interactionEventService.saveThumbEvent(eventId, userId, blogId, targetUserId, action);
        } else {
            interactionEventService.saveFavoriteEvent(eventId, userId, blogId, targetUserId, action);
        }
    }

    private boolean persistThumb(Long userId, Long blogId, Integer action) {
        if (action == InteractionEventConstant.ACTION_ADD) {
            if (thumbMapper.insertIgnore(userId, blogId) == 0) {
                return false;
            }
            return true;
        }

        if (thumbMapper.deleteByUserIdAndBlogId(userId, blogId) == 0) {
            return false;
        }
        return true;
    }

    private boolean persistFavorite(Long userId, Long blogId, Integer action) {
        if (action == InteractionEventConstant.ACTION_ADD) {
            if (favoriteMapper.insertIgnore(userId, blogId) == 0) {
                return false;
            }
            return true;
        }

        if (favoriteMapper.deleteByUserIdAndBlogId(userId, blogId) == 0) {
            return false;
        }
        return true;
    }

    private String value(Map<String, String> event, String key) {
        String value = event.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Stream 事件缺少字段: " + key);
        }
        return value;
    }
}
