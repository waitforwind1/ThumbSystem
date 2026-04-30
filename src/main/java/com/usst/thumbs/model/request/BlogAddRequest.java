package com.usst.thumbs.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class BlogAddRequest {
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createtime;

    /**
     * 更新时间
     */
    private Date updatetime;

    /**
     * 点赞数
     */
    private Integer thumbcount;

    /**
     * 封面
     */
    private String coverImage;
}
