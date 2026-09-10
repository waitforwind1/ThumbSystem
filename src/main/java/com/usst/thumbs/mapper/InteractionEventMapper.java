package com.usst.thumbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usst.thumbs.model.InteractionEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
* @author 22097
* @description 针对表【interaction_event(消息事件 设置队列异步操作)】的数据库操作Mapper
* @createDate 2026-05-28 20:19:02
* @Entity com.usst.thumbs.model.InteractionEvent
*/
@Mapper
public interface InteractionEventMapper extends BaseMapper<InteractionEvent> {

    @Update("""
        update interaction_event
        set status = 2
        where event_id = #{eventId}
        and status = 1
        """)
    void markSent(@Param("eventId") String eventId);

    @Update("""
        update interaction_event
        set retry_count = retry_count + 1,
            status = 3
        where status = 1
            and event_id = #{eventId}
        """)
    void markRetry(@Param("eventId") String eventId);

    @Update("""
        update interaction_event
        set status = 3,
            error_msg = '发布超时，重试'
        where status = 1
            and update_time < DATE_SUB(NOW(),INTERVAL 60 SECOND);
        """)
    int recoverTimeoutSending();
}




