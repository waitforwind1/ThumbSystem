package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.common.InteractionEventConstant;
import com.usst.thumbs.mapper.InteractionEventMapper;
import com.usst.thumbs.model.InteractionEvent;
import com.usst.thumbs.service.InteractionEventService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.usst.thumbs.common.InteractionEventConstant.DELETE_HOT_TYPE;

/**
* @author 22097
* @description 针对表【interaction_event(消息事件 设置队列异步操作)】的数据库操作Service实现
* @createDate 2026-05-29 18:57:59
*/
@Service
public class InteractionEventServiceImpl extends ServiceImpl<InteractionEventMapper, InteractionEvent>
    implements InteractionEventService{

    private final InteractionEventMapper interactionEventMapper;

    public InteractionEventServiceImpl(InteractionEventMapper interactionEventMapper) {
        this.interactionEventMapper = interactionEventMapper;
    }

    @Override
    public void saveDelayDeleteEvent(String eventId, Long blogId, int type) {
        saveEvent(eventId,null,blogId,null,null,DELETE_HOT_TYPE,type);
    }

    @Override
    public void saveDeleteHotEvent(String eventId, Long blogId, int type) {
        saveEvent(eventId,null,blogId,null,null, null, DELETE_HOT_TYPE);
    }

    @Override
    public void saveInitHotEvent(String eventId, Long blogId, int type) {
        saveEvent(eventId,null,blogId,null,null, null,InteractionEventConstant.INIT_HOT_TYPE);
    }

    @Override
    public void  saveThumbEvent(String eventId,Long userId, Long blogId, Long blogUserId, int action) {
        saveEvent(eventId,userId, blogId, blogUserId, null, action, InteractionEventConstant.THUMB_TYPE);
    }
    public void saveFavoriteEvent(String eventId,Long userId, Long blogId, Long blogUserId, int action) {
        saveEvent(eventId,userId, blogId, blogUserId, null, action, InteractionEventConstant.FAVORITE_TYPE);
    }

    @Override
    public void saveCommentEvent(String eventId,Long userId, Long blogId, Long blogUserId, Long commentId) {
        saveEvent(eventId,userId,blogId,blogUserId,commentId,InteractionEventConstant.ACTION_ADD,InteractionEventConstant.COMMENT_TYPE);
    }

    @Override
    public void saveCommentDeleteEvent(String eventId, Long blogId) {
        saveEvent(eventId, null, blogId, null, null,
                InteractionEventConstant.ACTION_CANCEL, InteractionEventConstant.COMMENT_TYPE);
    }

    @Override
    public void saveReplyEvent(String eventId,Long userId, Long blogId, Long blogUserId, Long commentId) {
        saveEvent(eventId,userId,blogId,blogUserId,commentId,InteractionEventConstant.ACTION_ADD,InteractionEventConstant.REPLY_TYPE);
    }

    @Override
    public void saveShareEvent(String eventId,Long userId, Long blogId, Long targetUserId) {
        saveEvent(eventId,userId,blogId,targetUserId,null,InteractionEventConstant.ACTION_ADD,InteractionEventConstant.SHARE_TYPE);
    }



    private void saveEvent(String eventId,Long userId, Long blogId, Long blogUserId, Long commentId, Integer action, Integer type){
        InteractionEvent interactionEvent = InteractionEvent.builder()
                .eventId(eventId)
                .action(action)
                .type(type)
                .userId(userId)
                .blogId(blogId)
                .targetUserId(blogUserId)
                .commentId(commentId)
                .status(InteractionEventConstant.STATUS_WAIT_SEND)
                .retryCount(0)
                .build();
        this.save(interactionEvent);
    }

    @Override
    public List<InteractionEvent> listPublishedEvents(int limit) {
        return this.lambdaQuery()
                .in(InteractionEvent::getStatus,
                        InteractionEventConstant.STATUS_WAIT_SEND,
                        InteractionEventConstant.STATUS_RETRY)
                .lt(InteractionEvent::getRetryCount,
                        InteractionEventConstant.MAX_RETRY_COUNT)
                .orderByAsc(InteractionEvent::getCreateTime)
                .last("limit " + limit)
                .list();
    }

    @Override
    public boolean tryMarkSending(String eventId) {
        return lambdaUpdate()
                .eq(InteractionEvent::getEventId, eventId)
                .in(
                        InteractionEvent::getStatus,
                        InteractionEventConstant.STATUS_WAIT_SEND,
                        InteractionEventConstant.STATUS_RETRY
                )
                .set(InteractionEvent::getStatus,
                        InteractionEventConstant.STATUS_SENDING)
                .set(InteractionEvent::getErrorMsg, null)
                .set(InteractionEvent::getUpdateTime, new Date())
                .update();
    }

    @Override
    public void markSent(String eventId) {
        interactionEventMapper.markSent(eventId);
    }

    @Override
    public void markRetry(String eventId) {
        interactionEventMapper.markRetry(eventId);
    }

    @Override
    public void markDead(String eventId, String errorMsg) {
        this.lambdaUpdate()
                .eq(InteractionEvent::getEventId,eventId)
                .setSql("retry_count = retry_count + 1")
                .set(InteractionEvent::getErrorMsg,errorMsg)
                .set(InteractionEvent::getStatus,InteractionEventConstant.STATUS_DEAD)
                .update();
    }

    @Override
    public void recoverTimeoutSending() {
        interactionEventMapper.recoverTimeoutSending();
    }
}




