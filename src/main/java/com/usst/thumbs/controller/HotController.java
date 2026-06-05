package com.usst.thumbs.controller;

import com.usst.thumbs.model.vo.BlogVO;
import com.usst.thumbs.result.Result;
import com.usst.thumbs.result.ResultUtils;
import com.usst.thumbs.service.HotService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/hot")
public class HotController {
    @Resource
    private HotService hotService;

    @GetMapping("/list")
    public Result<List<BlogVO>> hot(@RequestParam(defaultValue = "10") Integer limit,
                                    HttpServletRequest request) {
        return ResultUtils.success(hotService.listHot(limit, request));
    }

    @GetMapping("/category")
    public Result<List<BlogVO>> hotByCategory(@RequestParam String category,
                                              @RequestParam(defaultValue = "10") Integer limit,
                                              HttpServletRequest request) {
        return ResultUtils.success(hotService.listHotByCategory(category, limit, request));
    }
}
