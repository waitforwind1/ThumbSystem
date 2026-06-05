package com.usst.thumbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.thumbs.model.DTO.InteractionEventDTO;
import com.usst.thumbs.model.Message;
import com.usst.thumbs.model.vo.MessageVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author 22097
* @description 针对表【message(message站内消息表)】的数据库操作Service
* @createDate 2026-05-29 18:58:04
*/
public interface MessageService extends IService<Message> {
    void createThumbMessage(InteractionEventDTO eventDTO);
    void createCommentMessage(InteractionEventDTO eventDTO);
    void createReplyMessage(InteractionEventDTO eventDTO);
    void createFavoriteMessage(InteractionEventDTO eventDTO);
    void createShareMessage(InteractionEventDTO eventDTO);

    Long getUnReadCount(HttpServletRequest request);
    List<MessageVO> pageMessage(Integer pageNo,Integer pageSize,HttpServletRequest request);
    Boolean readMessage(HttpServletRequest request,Long messageId);
    Boolean readAll(HttpServletRequest request);
    Boolean deleteMessage(Long messageId,HttpServletRequest httpServletRequest);
    Boolean createSystemMessage(Long receiverId, String title, String content);


}
