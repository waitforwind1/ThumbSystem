package com.usst.thumbs.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息事件 设置队列异步操作
 * @TableName interaction_event
 */
@TableName(value ="interaction_event")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InteractionEvent implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 事件的ID
     */
    private String eventId;

    /**
     * 哪个用户触发的该事件
     */
    private Long userId;

    /**
     * 和哪个博客关联  应为几乎每个事件都和博客相关
     */
    private Long blogId;

    /**
     * 通知发给谁 这个主要用于生成本地消息
     */
    private Long targetUserId;

    /**
     * 如果是评论时间 记录评论ID
     */
    private Long commentId;

    /**
     * 事件类型 1-点赞 2-评论 3-收藏 4-回复
     */
    private Integer type;

    /**
     * 具体动作 比如新增还是删减 1-增加 0-取消
     */
    private Integer action;

    /**
     * 事件执行到哪一步了 0-待发送 1-已发送  2-已消费 3-发送失败 4-消费失败
     */
    private Integer status;

    /**
     * 尝试发送的次数
     */
    private Integer retryCount;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

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
        com.usst.thumbs.model.InteractionEvent other = (com.usst.thumbs.model.InteractionEvent) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getEventId() == null ? other.getEventId() == null : this.getEventId().equals(other.getEventId()))
                && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
                && (this.getBlogId() == null ? other.getBlogId() == null : this.getBlogId().equals(other.getBlogId()))
                && (this.getTargetUserId() == null ? other.getTargetUserId() == null : this.getTargetUserId().equals(other.getTargetUserId()))
                && (this.getCommentId() == null ? other.getCommentId() == null : this.getCommentId().equals(other.getCommentId()))
                && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
                && (this.getAction() == null ? other.getAction() == null : this.getAction().equals(other.getAction()))
                && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
                && (this.getRetryCount() == null ? other.getRetryCount() == null : this.getRetryCount().equals(other.getRetryCount()))
                && (this.getErrorMsg() == null ? other.getErrorMsg() == null : this.getErrorMsg().equals(other.getErrorMsg()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getEventId() == null) ? 0 : getEventId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getBlogId() == null) ? 0 : getBlogId().hashCode());
        result = prime * result + ((getTargetUserId() == null) ? 0 : getTargetUserId().hashCode());
        result = prime * result + ((getCommentId() == null) ? 0 : getCommentId().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getAction() == null) ? 0 : getAction().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getRetryCount() == null) ? 0 : getRetryCount().hashCode());
        result = prime * result + ((getErrorMsg() == null) ? 0 : getErrorMsg().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", eventId=").append(eventId);
        sb.append(", userId=").append(userId);
        sb.append(", blogId=").append(blogId);
        sb.append(", targetUserId=").append(targetUserId);
        sb.append(", commentId=").append(commentId);
        sb.append(", type=").append(type);
        sb.append(", action=").append(action);
        sb.append(", status=").append(status);
        sb.append(", retryCount=").append(retryCount);
        sb.append(", errorMsg=").append(errorMsg);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}