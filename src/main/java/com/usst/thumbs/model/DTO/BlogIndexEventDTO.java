package com.usst.thumbs.model.DTO;

import lombok.Data;

import java.util.Date;

@Data
public class BlogIndexEventDTO {
    private Long id;

    private String eventId;

    private Long blogId;

    private String operation;

    private Long version;

    private Integer status;

    private Integer retryCount;

    private String errorMsg;

    private Date createTime;

    private Date updateTime;

}
