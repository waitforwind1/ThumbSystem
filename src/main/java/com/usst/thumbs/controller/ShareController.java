package com.usst.thumbs.controller;

import com.usst.thumbs.model.request.ShareRequest;
import com.usst.thumbs.result.Result;
import com.usst.thumbs.result.ResultUtils;
import com.usst.thumbs.service.ShareService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/share")
@Tag(name = "分享")
public class ShareController {

    @Resource
    private ShareService shareService;

    @PostMapping("/share")
    public Result<Boolean> share(@RequestBody ShareRequest shareRequest,
                                 HttpServletRequest request) {
        return ResultUtils.success(shareService.shareBlog(shareRequest, request));
    }
}
