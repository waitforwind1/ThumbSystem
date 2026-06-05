package com.usst.thumbs.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
public class MessageVO implements Serializable {
    private Long id;
    private Long senderId;
    private String senderName;
    private String sendAvatar;
    private String content;
    private Long blogId;
    private Long commentId;
    private Integer type;
    private String title;
    private Integer isRead;
    private Date createTime;
}
