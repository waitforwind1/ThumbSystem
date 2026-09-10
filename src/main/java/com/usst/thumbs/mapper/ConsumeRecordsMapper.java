package com.usst.thumbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usst.thumbs.model.ConsumeRecords;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author 22097
* @description 针对表【consume_records】的数据库操作Mapper
* @createDate 2026-09-01 21:19:57
* @Entity com.usst.thumbs.model.ConsumeRecords
*/
@Mapper
public interface ConsumeRecordsMapper extends BaseMapper<ConsumeRecords> {

    @Insert("""
        insert ignore into consume_records(event_id, consumer_name) values(#{eventId},#{consumerName})
        """)
    int insertConsumeRecords(@Param("eventId") String eventId,@Param("consumerName") String consumeName);
}




