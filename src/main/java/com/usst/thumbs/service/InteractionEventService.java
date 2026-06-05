package com.usst.thumbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.thumbs.model.InteractionEvent;

/**
* @author 22097
* @description 针对表【interaction_event(消息事件 设置队列异步操作)】的数据库操作Service
* @createDate 2026-05-29 18:57:59
*/
public interface InteractionEventService extends IService<InteractionEvent> {

    void saveThumbEvent(Long userId, Long blogId, Long blogUserId, int action);
    void saveFavoriteEvent(Long userId, Long blogId, Long blogUserId, int action);
    void saveCommentEvent(Long userId, Long blogId, Long blogUserId, Long commentId);
    void saveReplyEvent(Long userId, Long blogId, Long blogUserId, Long commentId);
    void saveShareEvent(Long userId,Long blogId,Long targetUserId);

    void markSent(String eventId);
    void markConsumed(String eventId);
    void markSendFail(String eventId,String errorMsg);
    void markConsumeFail(String eventId,String errorMsg);
}
