package com.usst.thumbs.listener;

import com.rabbitmq.client.Channel;
import com.usst.thumbs.common.constant.*;
import com.usst.thumbs.mapper.ConsumeRecordsMapper;
import com.usst.thumbs.model.DTO.InteractionEventDTO;
import com.usst.thumbs.service.HotService;
import com.usst.thumbs.service.MessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static com.usst.thumbs.common.constant.InteractionEventConstant.*;

@Slf4j
@Component
public class InteractionEventConsumer {

    @Resource
    private MessageService messageService;

    @Resource
    private HotService hotService;


    @Resource
    private ConsumeRecordsMapper consumeRecordsMapper;
    
    private final String CONSUMER_NAME = "interaction_event_consumer";
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = RabbitMQConstant.INTERACTION_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void consume(InteractionEventDTO event, Message message, Channel channel) throws IOException {
        int records = consumeRecordsMapper.insertConsumeRecords(event.getEventId(), CONSUMER_NAME);
        if(records == 0)
            return;
        Integer type = event.getType();
        try {
            if(type == InteractionEventConstant.THUMB_TYPE){
                handleThumb(event);
            }else if(type == InteractionEventConstant.FAVORITE_TYPE){
                handleFavorite(event);
            }else if(type==InteractionEventConstant.COMMENT_TYPE){
                handleComment(event);
            }else if(type==InteractionEventConstant.REPLY_TYPE){
                handleReply(event);
            }else if(type==InteractionEventConstant.SHARE_TYPE){
                handleShare(event);
            }else if (type == InteractionEventConstant.INIT_HOT_TYPE){
                handleInitHot(event);
            }else if (type == DELETE_HOT_TYPE){
                handleDeleteHot(event);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleDelayDelete(InteractionEventDTO eventDTO){
        redisTemplate.delete(BlogConstant.BLOG_DETAIL_KEY.formatted(eventDTO.getBlogId()));
    }

    private void handleDeleteHot(InteractionEventDTO event) {
        hotService.deleteHotScore(event.getBlogId());
    }

    private void handleInitHot(InteractionEventDTO event) {
        hotService.initBlogHotScore(event.getBlogId());
    }

    private void handleShare(InteractionEventDTO event){
        messageService.createShareMessage(event);
        hotService.incrHotScore(event.getBlogId(), ShareConstant.HOT_SCORE);
    }
    private void handleThumb(InteractionEventDTO event){
        if(event.getAction() == ACTION_ADD){
            messageService.createThumbMessage(event);
            hotService.incrHotScore(event.getBlogId(), ThumbConstant.HOT_SCORE_THUMB);
        }else {
            hotService.incrHotScore(event.getBlogId(), -ThumbConstant.HOT_SCORE_THUMB);
        }
    }
    private void handleFavorite(InteractionEventDTO event){
        if(event.getAction() == ACTION_ADD){
            messageService.createFavoriteMessage(event);
            hotService.incrHotScore(event.getBlogId(), FavoriteConstant.HOT_SCORE_FAVORITE);
        }else {
            hotService.incrHotScore(event.getBlogId(), -FavoriteConstant.HOT_SCORE_FAVORITE);
        }
    }
    private void handleComment(InteractionEventDTO event){
        if (event.getAction() == ACTION_ADD) {
            messageService.createCommentMessage(event);
            hotService.incrHotScore(event.getBlogId(), CommentConstant.HOT_SCORE_COMMENT);
        } else {
            hotService.incrHotScore(event.getBlogId(), -CommentConstant.HOT_SCORE_COMMENT);
        }
    }
    private void handleReply(InteractionEventDTO event){
        if (event.getAction() == ACTION_ADD) {
            messageService.createReplyMessage(event);
            hotService.incrHotScore(event.getBlogId(), CommentConstant.HOT_SCORE_COMMENT);
        }
    }
}
