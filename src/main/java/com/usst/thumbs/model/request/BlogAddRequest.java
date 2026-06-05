package com.usst.thumbs.model.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class BlogAddRequest implements Serializable {

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 封面
     */
    private String coverImage;
    private String category;
    private String summary;
    private List<String> tags;
    private Integer status;
}
