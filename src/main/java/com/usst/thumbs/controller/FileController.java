package com.usst.thumbs.controller;

import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.model.request.RemoteImageDownloadRequest;
import com.usst.thumbs.result.Result;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.result.ResultUtils;
import com.usst.thumbs.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestPart("file") MultipartFile file){
        if(file.isEmpty())
            throw new BusinessException(ResultType.PARAM_ERROR,"上传为空");
        return ResultUtils.success(fileService.uploadImage(file));
    }

    @PostMapping("/downloadRemoteImage")
    public Result<String> downloadRemoteImage(@RequestBody @Valid RemoteImageDownloadRequest request) {
        String imageUrl = fileService.downloadRemoteImage(request.getImageUrl());
        return ResultUtils.success(imageUrl);
    }
}
