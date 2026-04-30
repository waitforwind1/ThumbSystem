package com.usst.thumbs.controller;

import com.usst.thumbs.common.Result;
import com.usst.thumbs.common.ResultType;
import com.usst.thumbs.common.ResultUtils;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.model.request.DoThumbRequest;
import com.usst.thumbs.service.ThumbsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/thumbs")
@Tag(name = "点赞接口")
public class ThumbsController {

    private final ThumbsService thumbsService;

    public ThumbsController(ThumbsService thumbsService) {
        this.thumbsService = thumbsService;
    }

    @PostMapping("/addThumb")
    public Result<Boolean> addThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request){
        if(doThumbRequest==null|| request==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数为空");
        return ResultUtils.success(ResultType.SUCCESS,thumbsService.addThumbs(doThumbRequest,request),"点赞成功");
    }

    @PostMapping("/rmThumb")
    public Result<Boolean> rmThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request){
        if(doThumbRequest==null|| request==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数为空");
        return ResultUtils.success(ResultType.SUCCESS,thumbsService.rmThumbs(doThumbRequest,request),"已取消");
    }
}
