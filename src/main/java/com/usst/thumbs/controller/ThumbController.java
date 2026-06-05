package com.usst.thumbs.controller;

import com.usst.thumbs.result.Result;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.result.ResultUtils;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.model.request.DoThumbRequest;
import com.usst.thumbs.service.ThumbService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/thumb")
@Tag(name = "点赞接口")
public class ThumbController {

    @Resource
    private ThumbService thumbService;

    @PostMapping("/do")
    public Result<Boolean> doThumb(@RequestBody DoThumbRequest request,
                                   HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ResultType.PARAM_ERROR, "参数为空");
        }
        return ResultUtils.success(ResultType.SUCCESS, thumbService.doThumb(request, httpRequest), "点赞成功");
    }

    @PostMapping("/undo")
    public Result<Boolean> undoThumb(@RequestBody DoThumbRequest request,
                                     HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ResultType.PARAM_ERROR, "参数为空");
        }
        return ResultUtils.success(ResultType.SUCCESS, thumbService.undoThumb(request, httpRequest), "取消点赞成功");
    }

}
