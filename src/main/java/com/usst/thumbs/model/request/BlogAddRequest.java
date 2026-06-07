package com.usst.thumbs.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 文章内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 封面
     */
    @Size(max = 512,message = "封面长度过长")
    private String coverImage;
    @Size(max = 64,message = "分类字段过长")
    private String category;
    @Size(max = 256,message = "摘要长度过长")
    private String summary;
    private List<String> tags;
    private Integer status;
}
