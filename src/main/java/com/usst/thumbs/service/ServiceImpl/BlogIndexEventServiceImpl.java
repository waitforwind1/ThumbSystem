package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.common.constant.BlogIndexEventConstant;
import com.usst.thumbs.mapper.BlogIndexEventMapper;
import com.usst.thumbs.model.BlogIndexEvent;
import com.usst.thumbs.service.BlogIndexEventService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
* @author 22097
* @description 针对表【blog_index_event】的数据库操作Service实现
* @createDate 2026-09-03 21:18:58
*/
@Service
public class BlogIndexEventServiceImpl extends ServiceImpl<BlogIndexEventMapper, BlogIndexEvent>
    implements BlogIndexEventService{

    @Override
    public void saveBlogIndexEvent(Long blogId, String action) {
        BlogIndexEvent blogIndexEvent = new BlogIndexEvent();
        String eventId = UUID.randomUUID().toString();
        blogIndexEvent.setEventId(eventId);
        blogIndexEvent.setBlogId(blogId);
        blogIndexEvent.setStatus(BlogIndexEventConstant.STATUS_WAIT_SEND);
        blogIndexEvent.setCreateTime(Date.from(Instant.now()));
        blogIndexEvent.setOperation(action);
        this.save(blogIndexEvent);
    }

    @Override
    public List<BlogIndexEvent> listPublishedEvents(int limit) {
        return this.lambdaQuery()
                .in(BlogIndexEvent::getStatus, BlogIndexEventConstant.STATUS_RETRY,BlogIndexEventConstant.STATUS_WAIT_SEND)
                .lt(BlogIndexEvent::getRetryCount, BlogIndexEventConstant.MAX_RETRY_COUNT)
                .orderByAsc(BlogIndexEvent::getCreateTime)
                .last("limit " + limit)
                .list();
    }

    @Override
    public boolean tryMarkSending(String eventId) {
        return this.lambdaUpdate()
                .eq(BlogIndexEvent::getEventId, eventId)
                .in(BlogIndexEvent::getStatus, BlogIndexEventConstant.STATUS_WAIT_SEND,BlogIndexEventConstant.STATUS_RETRY)
                .set(BlogIndexEvent::getStatus, BlogIndexEventConstant.STATUS_SENDING)
                .set(BlogIndexEvent::getUpdateTime,new Date())
                .set(BlogIndexEvent::getErrorMsg,null)
                .update();
    }

    @Override
    public void markSent(String eventId) {
        this.lambdaUpdate()
                .eq(BlogIndexEvent::getEventId, eventId)
                .in(BlogIndexEvent::getStatus, BlogIndexEventConstant.STATUS_SENDING)
                .lt(BlogIndexEvent::getRetryCount, BlogIndexEventConstant.MAX_RETRY_COUNT)
                .set(BlogIndexEvent::getStatus, BlogIndexEventConstant.STATUS_SENT)
                .update();
    }

    @Override
    public void markRetry(String eventId) {
        this.lambdaUpdate()
                .eq(BlogIndexEvent::getEventId, eventId)
                .in(BlogIndexEvent::getStatus, BlogIndexEventConstant.STATUS_SENDING)
                .setSql("retry_count = retry_count + 1")
                .set(BlogIndexEvent::getStatus, BlogIndexEventConstant.STATUS_RETRY)
                .update();
    }

    @Override
    public void markDead(String eventId, String errorMsg) {
        this.lambdaUpdate()
                .eq(BlogIndexEvent::getEventId, eventId)
                .gt(BlogIndexEvent::getRetryCount, BlogIndexEventConstant.MAX_RETRY_COUNT)
                .set(BlogIndexEvent::getStatus, BlogIndexEventConstant.STATUS_DEAD)
                .set(BlogIndexEvent::getErrorMsg,errorMsg)
                .update();
    }

    @Override
    public void recoverTimeoutSending() {
        Date date = Date.from(
                LocalDateTime.now()
                        .minusSeconds(60)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
        this.lambdaUpdate()
                .set(BlogIndexEvent::getErrorMsg,"发布超时，重试")
                .setSql("retry_count = retry_count + 1")
                .eq(BlogIndexEvent::getStatus, BlogIndexEventConstant.STATUS_SENDING)
                .lt(BlogIndexEvent::getUpdateTime,date)
                .set(BlogIndexEvent::getStatus,BlogIndexEventConstant.STATUS_RETRY)
                .update();
    }
}




