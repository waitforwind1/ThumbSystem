package com.usst.thumbs.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class PostCommentRequest implements Serializable {
    private Long blogId;
    private String content;

}
