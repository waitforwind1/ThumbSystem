package com.usst.thumbs.controller;

import com.usst.thumbs.model.request.PostCommentRequest;
import com.usst.thumbs.model.request.ReplyCommentRequest;
import com.usst.thumbs.model.vo.CommentVO;
import com.usst.thumbs.result.Result;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.result.ResultUtils;
import com.usst.thumbs.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@Tag(name = "评论区")
public class CommentController {
    @Resource
    private CommentService commentService;

    @PostMapping("/add")
    public Result<Boolean> addComment(@RequestBody PostCommentRequest request,
                                      HttpServletRequest httpRequest) {
        return ResultUtils.success(ResultType.SUCCESS, commentService.postComment(request, httpRequest), "评论成功");
    }

    @PostMapping("/reply")
    public Result<Boolean> replyComment(@RequestBody ReplyCommentRequest request,
                                        HttpServletRequest httpRequest) {
        return ResultUtils.success(ResultType.SUCCESS, commentService.replyComment(request, httpRequest), "回复成功");
    }

    @GetMapping("/page")
    public Result<List<CommentVO>> pageComment(@RequestParam Long blogId,
                                               @RequestParam(defaultValue = "1") Integer pageNo,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResultUtils.success(ResultType.SUCCESS, commentService.pageComment(blogId, pageNo, pageSize), "查询成功");
    }

    @DeleteMapping("/{commentId}")
    public Result<Boolean> deleteComment(@PathVariable Long commentId,
                                         HttpServletRequest httpRequest) {
        return ResultUtils.success(ResultType.SUCCESS, commentService.deleteComment(commentId, httpRequest), "删除成功");
    }
}
