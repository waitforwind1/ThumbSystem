package com.usst.thumbs.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReplyCommentRequest implements Serializable {
    private Long blogId;
    private Long parentId;
    private String content;
}
