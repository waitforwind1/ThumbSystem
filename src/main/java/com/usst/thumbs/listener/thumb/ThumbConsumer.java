package com.usst.thumbs.listener.thumb;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.usst.thumbs.listener.thumb.msg.ThumbEvent;
import com.usst.thumbs.mapper.BlogMapper;
import com.usst.thumbs.model.Thumbs;
import com.usst.thumbs.service.ThumbsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.common.schema.SchemaType;
import org.apache.pulsar.shade.org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ThumbConsumer {

    @Resource(name = "thumbService")
    private ThumbsService thumbsService;

    @Autowired
    private BlogMapper blogMapper;

    @PulsarListener(
            subscriptionName = "thumb-subscription",
            topics = "thumb-topic",
            schemaType = SchemaType.JSON,
            batch = true,
            ackTimeoutRedeliveryBackoff = "ackTimeoutRedeliveryBackoff",
            negativeAckRedeliveryBackoff = "negativeAckRedeliveryBackoff",
            // 配置此"thumb-subscription"组里的消费者共享消费队列 在配置多个消费者的情况下共享
            subscriptionType = SubscriptionType.Shared,
            deadLetterPolicy = "deadLetterPolicy",
            // 这个是一个对此消费者的额外配置  不仅在此注解里能配置 在另一个bean里还可额外配置此消费者
            consumerCustomizer = "thumbConsumerConfig"
    )
    @Transactional(rollbackFor = Exception.class)
    public void processBatch(List<Message<ThumbEvent>> messages){
        Map<Long,Long> countMap = new ConcurrentHashMap<>();
        List<Thumbs> thumbsList = new ArrayList<>();

        LambdaQueryWrapper<Thumbs> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        AtomicReference<Boolean> needMove = new AtomicReference<>(false);

        List<ThumbEvent> thumbEvents = messages.stream()
                .map(Message::getValue)
                .filter(Objects::nonNull)
                .toList();
        Map<Pair<Long,Long>,ThumbEvent> lastestEvent = thumbEvents.stream()
                        .collect(Collectors.groupingBy(
                                e->Pair.of(e.getUserId(),e.getBlogId()),
                                        Collectors.collectingAndThen(
                                                Collectors.toList(),
                                                list->{
                                                    list.sort(Comparator.comparing(ThumbEvent::getEventTime));
                                                    if(list.size()%2==0)
                                                        return null;
                                                    return list.get(list.size()-1);
                                                }
                                        )));
        lastestEvent.forEach((userBlogPair,event)->{
            if(event==null){
                return;
            }
            log.info("间隔时间{}ms", Duration.between(event.getEventTime(), LocalDateTime.now()).toMillis());
            ThumbEvent.EventType finalAction = event.getEventType();
            if(finalAction == ThumbEvent.EventType.INCR){
                countMap.merge(event.getBlogId(),1L,Long::sum);
                Thumbs thumbs = new Thumbs();
                thumbs.setUserId(event.getUserId());
                thumbs.setBlogId(event.getBlogId());
                thumbsList.add(thumbs);
            }else {
                needMove.set(true);
                lambdaQueryWrapper.or().eq(Thumbs::getUserId,event.getUserId()).eq(Thumbs::getBlogId,event.getBlogId());
                countMap.merge(event.getBlogId(),-1L,Long::sum);
            }
        });
        if(needMove.get())
            thumbsService.remove(lambdaQueryWrapper);
        if(countMap!=null){
            blogMapper.batchUpdateThumbCount(countMap);
        }
        batchInsertThumb(thumbsList);
    }


    // 分批次插入
    private void batchInsertThumb(List<Thumbs> thumbsList) {
        if(!thumbsList.isEmpty()){
            thumbsService.saveBatch(thumbsList,500);
        }
    }



    // 配置死信消息队列的处理
    @PulsarListener(
            subscriptionName = "thumb-dlq-subscription",
            topics = "thumb-dlq-topic",
            schemaType = SchemaType.JSON
    )
    public void consumerDlq(Message<ThumbEvent> message){
        MessageId messageId = message.getMessageId();
        log.info("消息{}已入库",messageId);
        log.info("已通知相关技术人员{}处理该消息{}","郭涛",messageId);
    }
}
