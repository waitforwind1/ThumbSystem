package com.usst.thumbs.controller;

import com.usst.thumbs.common.Result;
import com.usst.thumbs.common.ResultType;
import com.usst.thumbs.common.ResultUtils;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Thumbs;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.BlogAddRequest;
import com.usst.thumbs.model.vo.BlogVO;
import com.usst.thumbs.service.BlogService;
import com.usst.thumbs.service.ThumbsService;
import com.usst.thumbs.service.UserService;
import io.micrometer.common.util.StringUtils;
import io.netty.util.internal.StringUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/blog")
@Tag(name = "博客控制接口")
public class BlogController {

    private final BlogService blogService;
    private final UserService userService;
    private final ThumbsService thumbsService;

    public BlogController(BlogService blogService, UserService userService, ThumbsService thumbsService) {
        this.blogService = blogService;
        this.userService = userService;
        this.thumbsService = thumbsService;
    }

    @PostMapping("writeBlog")
    public Result<Boolean> writeBlog(@RequestBody BlogAddRequest blogAddRequest,HttpServletRequest request){
        if(blogAddRequest==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        return ResultUtils.success(ResultType.SUCCESS,blogService.writeBlog(blogAddRequest,request),"保存成功");
    }

    @PostMapping("/getBlog")
    public BlogVO getBlog(Long blogId,HttpServletRequest request){
        if(blogId==null)
            throw new BusinessException(ResultType.PARAMS_EMPTY,"博客id为空");
        return blogService.getBlogvoByid(blogId,request);
    }


    @PostMapping("/getBlogLists")
    public List<BlogVO> getblogLists(HttpServletRequest request){
        List<Blog> blogList = blogService.list();
        if(blogList==null)
            return new ArrayList<>();
        return blogService.getblogLists(blogList,request);
    }
}
