package com.usst.thumbs.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShareRequest implements Serializable {
    private Long blogId;
    private String url;
    private String path;
}