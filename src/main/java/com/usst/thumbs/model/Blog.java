package com.usst.thumbs.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @TableName blog
 */
@TableName(value ="blog")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Blog implements Serializable {
    /**
     * 文章id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 作者id
     */
    private Long userId;

    /**
     * 文章名称
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 点赞数
     */
    private Long thumbCount;

    /**
     * 封面
     */
    private String coverImage;

    /**
     * 被评论数
     */
    private Long commentCount;

    /**
     * 被收藏数
     */
    private Long favoriteCount;

    /**
     * 被分享次数
     */
    private Long shareCount;

    /**
     *
     */
    private Double hotScore;

    /**
     *
     */
    private Long viewCount;

    /**
     * 业务状态 0-未发布  1-已发布 2-审核中  3-审核不通过 4-已下架
     */
    private Integer status;

    /**
     * 逻辑删除字段
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private String tag;

    /**
     * 摘要
     */
    private String summary;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Blog other = (Blog) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
                && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
                && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
                && (this.getThumbCount() == null ? other.getThumbCount() == null : this.getThumbCount().equals(other.getThumbCount()))
                && (this.getCoverImage() == null ? other.getCoverImage() == null : this.getCoverImage().equals(other.getCoverImage()))
                && (this.getCommentCount() == null ? other.getCommentCount() == null : this.getCommentCount().equals(other.getCommentCount()))
                && (this.getFavoriteCount() == null ? other.getFavoriteCount() == null : this.getFavoriteCount().equals(other.getFavoriteCount()))
                && (this.getShareCount() == null ? other.getShareCount() == null : this.getShareCount().equals(other.getShareCount()))
                && (this.getHotScore() == null ? other.getHotScore() == null : this.getHotScore().equals(other.getHotScore()))
                && (this.getViewCount() == null ? other.getViewCount() == null : this.getViewCount().equals(other.getViewCount()))
                && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
                && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()))
                && (this.getCategory() == null ? other.getCategory() == null : this.getCategory().equals(other.getCategory()))
                && (this.getTag() == null ? other.getTag() == null : this.getTag().equals(other.getTag()))
                && (this.getSummary() == null ? other.getSummary() == null : this.getSummary().equals(other.getSummary()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getThumbCount() == null) ? 0 : getThumbCount().hashCode());
        result = prime * result + ((getCoverImage() == null) ? 0 : getCoverImage().hashCode());
        result = prime * result + ((getCommentCount() == null) ? 0 : getCommentCount().hashCode());
        result = prime * result + ((getFavoriteCount() == null) ? 0 : getFavoriteCount().hashCode());
        result = prime * result + ((getShareCount() == null) ? 0 : getShareCount().hashCode());
        result = prime * result + ((getHotScore() == null) ? 0 : getHotScore().hashCode());
        result = prime * result + ((getViewCount() == null) ? 0 : getViewCount().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        result = prime * result + ((getCategory() == null) ? 0 : getCategory().hashCode());
        result = prime * result + ((getTag() == null) ? 0 : getTag().hashCode());
        result = prime * result + ((getSummary() == null) ? 0 : getSummary().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", title=").append(title);
        sb.append(", content=").append(content);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", thumbCount=").append(thumbCount);
        sb.append(", coverImage=").append(coverImage);
        sb.append(", commentCount=").append(commentCount);
        sb.append(", favoriteCount=").append(favoriteCount);
        sb.append(", shareCount=").append(shareCount);
        sb.append(", hotScore=").append(hotScore);
        sb.append(", viewCount=").append(viewCount);
        sb.append(", status=").append(status);
        sb.append(", isDelete=").append(isDelete);
        sb.append(", category=").append(category);
        sb.append(", tag=").append(tag);
        sb.append(", summary=").append(summary);
        sb.append("]");
        return sb.toString();
    }
}