package com.usst.thumbs.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("dead_message")
public class DeadMessage {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String eventId;
    private String originalMessageId;
    private String deadMessageId;
    private String messageType;
    private Integer deliveryCount;
    private String deadReason;
    private String payload;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
