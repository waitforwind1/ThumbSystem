package com.usst.thumbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.thumbs.model.InteractionEvent;

import java.util.List;

/**
* @author 22097
* @description 针对表【interaction_event(消息事件 设置队列异步操作)】的数据库操作Service
* @createDate 2026-05-29 18:57:59
*/
public interface InteractionEventService extends IService<InteractionEvent> {

    void saveDelayDeleteEvent(String eventId,Long blogId,int type);
    void saveDeleteHotEvent(String eventId,Long blogId,int type);
    void saveInitHotEvent(String eventId,Long blogId,int type);
    void saveThumbEvent(String eventId,Long userId, Long blogId, Long blogUserId, int action);
    void saveFavoriteEvent(String eventId,Long userId, Long blogId, Long blogUserId, int action);
    void saveCommentEvent(String eventId,Long userId, Long blogId, Long blogUserId, Long commentId);
    void saveCommentDeleteEvent(String eventId, Long blogId);
    void saveReplyEvent(String eventId,Long userId, Long blogId, Long blogUserId, Long commentId);
    void saveShareEvent(String eventId,Long userId,Long blogId,Long targetUserId);


    List<InteractionEvent> listPublishedEvents(int limit);

    boolean tryMarkSending(String eventId);
    void markSent(String eventId);
    void markRetry(String eventId);
    void markDead(String eventId,String errorMsg);
    void recoverTimeoutSending();
}
