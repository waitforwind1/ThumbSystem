package com.usst.thumbs.mapper;

import com.usst.thumbs.model.InteractionEvent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 22097
* @description 针对表【interaction_event(消息事件 设置队列异步操作)】的数据库操作Mapper
* @createDate 2026-05-28 20:19:02
* @Entity com.usst.thumbs.model.InteractionEvent
*/
@Mapper
public interface InteractionEventMapper extends BaseMapper<InteractionEvent> {

}




