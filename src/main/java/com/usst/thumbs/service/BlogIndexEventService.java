package com.usst.thumbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.thumbs.model.BlogIndexEvent;

import java.util.List;

/**
* @author 22097
* @description 针对表【blog_index_event】的数据库操作Service
* @createDate 2026-09-03 21:18:58
*/
public interface BlogIndexEventService extends IService<BlogIndexEvent> {

    void saveBlogIndexEvent(Long blogId,String action);
    List<BlogIndexEvent> listPublishedEvents(int limit);

    boolean tryMarkSending(String eventId);
    void markSent(String eventId);
    void markRetry(String eventId);
    void markDead(String eventId,String errorMsg);
    void recoverTimeoutSending();
}
