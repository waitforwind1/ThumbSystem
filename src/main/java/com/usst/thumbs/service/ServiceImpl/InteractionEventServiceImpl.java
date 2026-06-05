package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.common.InteractionEventConstant;
import com.usst.thumbs.mapper.InteractionEventMapper;
import com.usst.thumbs.model.InteractionEvent;
import com.usst.thumbs.service.InteractionEventService;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
* @author 22097
* @description 针对表【interaction_event(消息事件 设置队列异步操作)】的数据库操作Service实现
* @createDate 2026-05-29 18:57:59
*/
@Service
public class InteractionEventServiceImpl extends ServiceImpl<InteractionEventMapper, InteractionEvent>
    implements InteractionEventService{

    @Override
    public void saveThumbEvent(Long userId, Long blogId, Long blogUserId, int action) {
        saveEvent(userId, blogId, blogUserId, null, action, InteractionEventConstant.THUMB_TYPE);
    }
    public void saveFavoriteEvent(Long userId, Long blogId, Long blogUserId, int action) {
        saveEvent(userId, blogId, blogUserId, null, action, InteractionEventConstant.FAVORITE_TYPE);
    }

    @Override
    public void saveCommentEvent(Long userId, Long blogId, Long blogUserId, Long commentId) {
        saveEvent(userId,blogId,blogUserId,commentId,InteractionEventConstant.ACTION_ADD,InteractionEventConstant.COMMENT_TYPE);
    }

    @Override
    public void saveReplyEvent(Long userId, Long blogId, Long blogUserId, Long commentId) {
        saveEvent(userId,blogId,blogUserId,commentId,InteractionEventConstant.ACTION_ADD,InteractionEventConstant.REPLY_TYPE);
    }

    @Override
    public void saveShareEvent(Long userId, Long blogId, Long targetUserId) {
        saveEvent(userId,blogId,targetUserId,null,InteractionEventConstant.ACTION_ADD,InteractionEventConstant.SHARE_TYPE);
    }


    private void saveEvent(Long userId, Long blogId, Long blogUserId, Long commentId, Integer action, Integer type){
        InteractionEvent interactionEvent = InteractionEvent.builder()
                .eventId(UUID.randomUUID().toString().replace("-", ""))
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
    public void markSent(String eventId) {
        this.lambdaUpdate()
                .eq(InteractionEvent::getEventId,eventId)
                .set(InteractionEvent::getStatus,InteractionEventConstant.STATUS_SENT)
                .update();
    }

    @Override
    public void markConsumed(String eventId) {
        this.lambdaUpdate()
                .eq(InteractionEvent::getEventId,eventId)
                .set(InteractionEvent::getStatus,InteractionEventConstant.STATUS_CONSUMED)
                .update();
    }

    @Override
    public void markSendFail(String eventId, String errorMsg) {
        this.lambdaUpdate()
                .eq(InteractionEvent::getEventId,eventId)
                .setSql("retry_count = retry_count + 1")
                .set(InteractionEvent::getErrorMsg,errorMsg)
                .set(InteractionEvent::getStatus,InteractionEventConstant.STATUS_SNED_FAIL)
                .update();
    }

    @Override
    public void markConsumeFail(String eventId, String errorMsg) {
        this.lambdaUpdate()
                .eq(InteractionEvent::getEventId,eventId)
                .setSql("retry_count = retry_count + 1")
                .set(InteractionEvent::getStatus,InteractionEventConstant.STATUS_CONSUMED_FAIL)
                .set(InteractionEvent::getErrorMsg,errorMsg)
                .update();
    }

}




