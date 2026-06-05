package com.usst.thumbs.mapper;

import com.usst.thumbs.model.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 22097
* @description 针对表【message(message站内消息表)】的数据库操作Mapper
* @createDate 2026-05-28 20:19:05
* @Entity com.usst.thumbs.model.Message
*/
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}




