package com.usst.thumbs.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import com.usst.thumbs.common.*;
import com.usst.thumbs.mapper.BlogMapper;
import com.usst.thumbs.model.DTO.InteractionEventDTO;
import com.usst.thumbs.model.Favorite;
import com.usst.thumbs.model.Thumb;
import com.usst.thumbs.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

import static com.usst.thumbs.common.InteractionEventConstant.ACTION_ADD;

@Slf4j
@Component
public class InteractionEventConsumer {

    @Resource
    private ThumbService thumbsService;

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private BlogMapper blogMapper;

    @Resource
    private MessageService messageService;

    @Resource
    private HotService hotService;

    @Resource
    private InteractionEventService interactionEventService;
    @Autowired
    private ShareService shareService;

    @RabbitListener(queues = RabbitMQConstant.INTERACTION_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void consume(InteractionEventDTO event, Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            Integer type = event.getType();
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
            }
            interactionEventService.markConsumed(event.getEventId());
            channel.basicAck(tag,false);
        } catch (IOException e) {
            log.info("消息消费失败 消息ID = {}",event.getEventId());
            interactionEventService.markConsumeFail(event.getEventId(),e.getMessage());
            channel.basicNack(tag,false,false);
        }
    }

    private void handleShare(InteractionEventDTO event){
        messageService.createShareMessage(event);
        hotService.incrHotScore(event.getBlogId(), ShareConstant.HOT_SCORE);
    }
    /**
     * 处理点赞的异步事件 插入数据库记录+对应博客点赞数+1
     * @param event
     */
    private void handleThumb(InteractionEventDTO event){
        if(event.getAction() == ACTION_ADD){
            try {
                thumbsService.save(Thumb.builder()
                                .userId(event.getUserId())
                                .blogId(event.getBlogId())
                        .build());
                blogMapper.batchUpdateThumbCount(Map.of(event.getBlogId(),1L));
                // todo:热榜和站内消息
            messageService.createThumbMessage(event);
            hotService.incrHotScore(event.getBlogId(), ThumbConstant.HOT_SCORE_THUMB);
            } catch (DuplicateKeyException ignored) {
            }
        }else {
            thumbsService.remove(new LambdaQueryWrapper<Thumb>().eq(Thumb::getId,event.getBlogId()).eq(Thumb::getUserId,event.getUserId()));
            blogMapper.batchUpdateThumbCount(Map.of(event.getBlogId(),-1L));
            //todo:热榜消息
            hotService.incrHotScore(event.getBlogId(), -ThumbConstant.HOT_SCORE_THUMB);
        }
    }
    private void handleFavorite(InteractionEventDTO event){
        if(event.getAction() == ACTION_ADD){
            try {
                favoriteService.save(Favorite.builder().userId(event.getUserId()).blogId(event.getBlogId()).build());
                blogMapper.batchUpdateFavoriteCount(Map.of(event.getBlogId(),1L));
                //todo:热榜消息
                messageService.createFavoriteMessage(event);
                hotService.incrHotScore(event.getBlogId(), FavoriteConstant.HOT_SCORE_FAVORITE);
            } catch (DuplicateKeyException ignored) {
            }
        }else {
            favoriteService.remove(new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId,event.getUserId()).eq(Favorite::getBlogId,event.getBlogId()));
            blogMapper.batchUpdateFavoriteCount(Map.of(event.getBlogId(),-1L));
            // todo:热榜异步处理
            hotService.incrHotScore(event.getBlogId(), -FavoriteConstant.HOT_SCORE_FAVORITE);
        }
    }
    private void handleComment(InteractionEventDTO event){
        //todo:
        messageService.createCommentMessage(event);
        hotService.incrHotScore(event.getBlogId(), FavoriteConstant.HOT_SCORE_FAVORITE);
    }
    private void handleReply(InteractionEventDTO event){
        // todo:
        messageService.createReplyMessage(event);
        hotService.incrHotScore(event.getBlogId(),FavoriteConstant.HOT_SCORE_FAVORITE);
    }
}
