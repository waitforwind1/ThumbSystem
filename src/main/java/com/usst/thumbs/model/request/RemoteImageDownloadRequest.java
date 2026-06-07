package com.usst.thumbs.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RemoteImageDownloadRequest {

    @NotBlank(message = "上传图片地址为空")
    private String imageUrl;
}
