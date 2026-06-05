package com.usst.thumbs.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.usst.thumbs.common.BlogConstant;
import com.usst.thumbs.common.UserConstant;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.BlogAddRequest;
import com.usst.thumbs.model.request.BlogSearchRequest;
import com.usst.thumbs.model.vo.BlogInteractionVO;
import com.usst.thumbs.model.vo.BlogVO;
import com.usst.thumbs.result.Result;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.result.ResultUtils;
import com.usst.thumbs.service.BlogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.usst.thumbs.common.UserState.USER_LOGIN_STATE;

@RestController
@RequestMapping("/blog")
@Tag(name = "博客控制接口")
public class BlogController {

    @Resource
    private  BlogService blogService;

    @PostMapping("add")
    public Result<Boolean> addBlog(@RequestBody BlogAddRequest blogAddRequest,HttpServletRequest request){
        if(blogAddRequest==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        if(getLoginUser(request)==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"请登录后再查看");
        return ResultUtils.success(ResultType.SUCCESS,blogService.writeBlog(blogAddRequest,request),"发布成功");
    }

    @GetMapping("/getBlog")
    public Result<List<BlogVO>> pageBlog(@RequestParam(defaultValue = "1")Integer pageNo,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         HttpServletRequest request){
        return ResultUtils.success(ResultType.SUCCESS,blogService.pageGetBlog(pageNo,pageSize,request),"查询成功");
    }

    @PostMapping("/{blogId}")
    public Result<Boolean> deleteBlog(@PathVariable("blogId") Long blogId,HttpServletRequest request){
        return ResultUtils.success(ResultType.SUCCESS,blogService.deleteBlog(blogId,request),"删除成功");
    }

    @PostMapping("/{blogId}/update")
    public Result<Boolean> updateBlog(@PathVariable("blogId") Long blogId,
                                      @RequestBody BlogAddRequest blogAddRequest,
                                      HttpServletRequest request) {
        if (blogAddRequest == null) {
            throw new BusinessException(ResultType.PARAM_ERROR, "参数错误");
        }
        return ResultUtils.success(ResultType.SUCCESS, blogService.updateBlog(blogId, blogAddRequest, request), "修改成功");
    }


    public User getLoginUser(HttpServletRequest request){
        return (User) request.getSession().getAttribute(USER_LOGIN_STATE);
    }

    @GetMapping("/{blogId}")
    public Result<BlogVO> detail(@PathVariable Long blogId, HttpServletRequest request) {
        return ResultUtils.success(ResultType.SUCCESS, blogService.blogDetail(blogId, request), "查询成功");
    }

    @PostMapping("/search")
    public Result<List<BlogVO>> search(@RequestBody BlogSearchRequest searchRequest,
                                       HttpServletRequest request) {
        return ResultUtils.success(blogService.searchBlog(searchRequest, request));
    }

    @GetMapping("/author/{userId}")
    public Result<List<BlogVO>> listByAuthor(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "1") Integer pageNo,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             HttpServletRequest request) {
        return ResultUtils.success(blogService.listByAuthor(userId, pageNo, pageSize, request));
    }

    @GetMapping("/blog/{blogId}/status")
    public Result<BlogInteractionVO> getStatus(@PathVariable Long blogId,
                                               HttpServletRequest request) {
        return ResultUtils.success(blogService.getInteractionStatus(blogId, request));
    }

    @GetMapping("/admin/list")
    public Result<List<BlogVO>> adminList(@RequestParam(defaultValue = "1") Integer pageNo,
                                          @RequestParam(defaultValue = "50") Integer pageSize,
                                          HttpServletRequest request) {
        assertAdmin(request);
        int current = pageNo == null || pageNo <= 0 ? 1 : pageNo;
        int size = pageSize == null || pageSize <= 0 ? 50 : Math.min(pageSize, 100);
        List<BlogVO> blogs = blogService.page(new Page<>(current, size), new LambdaQueryWrapper<Blog>()
                        .orderByDesc(Blog::getCreateTime))
                .getRecords()
                .stream()
                .map(blog -> blogService.convertToBlogVO(blog, request))
                .collect(Collectors.toList());
        return ResultUtils.success(blogs);
    }

    @PostMapping("/admin/{blogId}/offline")
    public Result<Boolean> offlineBlog(@PathVariable Long blogId, HttpServletRequest request) {
        return ResultUtils.success(updateBlogStatus(blogId, BlogConstant.BLOG_STATUS_OFFLINE, request));
    }

    @PostMapping("/admin/{blogId}/publish")
    public Result<Boolean> publishBlog(@PathVariable Long blogId, HttpServletRequest request) {
        return ResultUtils.success(updateBlogStatus(blogId, BlogConstant.BLOG_STATUS_PUBLISHED, request));
    }

    private Boolean updateBlogStatus(Long blogId, Integer status, HttpServletRequest request) {
        if (blogId == null || blogId <= 0) {
            throw new BusinessException(ResultType.PARAM_ERROR, "文章 id 不合法");
        }
        return blogService.updateBlogStatus(blogId, status, request);
    }

    private void assertAdmin(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        if (loginUser == null || loginUser.getIsAdmin() == null || !loginUser.getIsAdmin().equals(UserConstant.USER_IS_ADMIN)) {
            throw new BusinessException(ResultType.NO_AUTH, "无管理员权限");
        }
    }
}
