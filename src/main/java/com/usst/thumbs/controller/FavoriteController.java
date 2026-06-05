package com.usst.thumbs.controller;

import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.model.request.DoFavoriteRequest;
import com.usst.thumbs.result.Result;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.result.ResultUtils;
import com.usst.thumbs.service.FavoriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/favorite")
@Tag(name = "收藏")
public class FavoriteController {
    @Resource
    private FavoriteService favoriteService;

    @PostMapping("/do")
    public Result<Boolean> doFavorite(@RequestBody DoFavoriteRequest request,
                                      HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ResultType.PARAM_ERROR, "参数为空");
        }
        return ResultUtils.success(ResultType.SUCCESS, favoriteService.doFavorite(request, httpRequest), "收藏成功");
    }

    @PostMapping("/undo")
    public Result<Boolean> undoFavorite(@RequestBody DoFavoriteRequest request,
                                        HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ResultType.PARAM_ERROR, "参数为空");
        }
        return ResultUtils.success(ResultType.SUCCESS, favoriteService.undoFavorite(request, httpRequest), "取消收藏成功");
    }

}
