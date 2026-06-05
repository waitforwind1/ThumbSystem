package com.usst.thumbs.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentVO {
    private Long id;

    /**
     * 评论用户ID
     */
    private Long userId;
    private String username;
    private String avatar;
    private String replyUsername;


    /**
     * 博客ID
     */
    private Long blogId;

    /**
     * 被回复的用户的ID，用于快速查找当前用户的评论回复，主要是查询方便
     */
    private Long replyUserId;

    /**
     * 评论树的根部ID 最顶部  0-表示根部
     */
    private Long rootId;

    /**
     * 直系父亲ID
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 创建评论时间
     */
    private Date crteateTime;

    /**
     * 存储孩子数据 也就是每个评论的子评论 必须要嵌套关系
     */
    @Builder.Default
    private List<CommentVO> children  = new ArrayList<>();

}
