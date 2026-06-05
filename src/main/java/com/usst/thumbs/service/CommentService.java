package com.usst.thumbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.thumbs.model.Comment;
import com.usst.thumbs.model.request.PostCommentRequest;
import com.usst.thumbs.model.request.ReplyCommentRequest;
import com.usst.thumbs.model.vo.CommentVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 22097
* @description 针对表【comment(保存用户评论)】的数据库操作Service
* @createDate 2026-05-16 18:18:31
*/
@Service
public interface CommentService extends IService<Comment> {

    /**
     * 在博客下直接发表评论（一级评论）
     * @param postCommentRequest
     * @param request
     * @return
     */
    Boolean postComment(PostCommentRequest postCommentRequest, HttpServletRequest request);

    /**
     * 回复别人的评论
     * @param request
     * @param httpRequest
     * @return
     */
    Boolean replyComment(ReplyCommentRequest request, HttpServletRequest httpRequest);

    /**
     * 分页查询
     * @param blogId
     * @param pageNo
     * @param pageSize
     * @return
     */
    List<CommentVO> pageComment(Long blogId, Integer pageNo, Integer pageSize);

    /**
     * 删除评论
     * @param commentId
     * @param httpRequest
     * @return
     */
    Boolean deleteComment(Long commentId, HttpServletRequest httpRequest);

    List<CommentVO> pageRootComments(Long blogId,Integer pageNo,Integer pageSize);
    List<CommentVO> listReplies(Long blogId,Integer pageNo,Integer pageSize);
}
