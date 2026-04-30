package com.usst.thumbs.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName blog
 */
@TableName(value ="blog")
@Data
public class Blog {
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
    private Integer thumbCount;

    /**
     * 封面
     */
    private String coverImage;

//    @Override
//    public boolean equals(Object that) {
//        if (this == that) {
//            return true;
//        }
//        if (that == null) {
//            return false;
//        }
//        if (getClass() != that.getClass()) {
//            return false;
//        }
//        Blog other = (Blog) that;
//        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
//            && (this.getUserid() == null ? other.getUserid() == null : this.getUserid().equals(other.getUserid()))
//            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
//            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
//            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
//            && (this.getUpdatetime() == null ? other.getUpdatetime() == null : this.getUpdatetime().equals(other.getUpdatetime()))
//            && (this.getThumbCount() == null ? other.getThumbCount() == null : this.getThumbCount().equals(other.getThumbCount()))
//            && (this.getCoverImage() == null ? other.getCoverImage() == null : this.getCoverImage().equals(other.getCoverImage()));
//    }
//
//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
//        result = prime * result + ((getUserid() == null) ? 0 : getUserid().hashCode());
//        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
//        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
//        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
//        result = prime * result + ((getUpdatetime() == null) ? 0 : getUpdatetime().hashCode());
//        result = prime * result + ((getThumbCount() == null) ? 0 : getThumbCount().hashCode());
//        result = prime * result + ((getCoverImage() == null) ? 0 : getCoverImage().hashCode());
//        return result;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append(getClass().getSimpleName());
//        sb.append(" [");
//        sb.append("Hash = ").append(hashCode());
//        sb.append(", id=").append(id);
//        sb.append(", userid=").append(userId);
//        sb.append(", title=").append(title);
//        sb.append(", content=").append(content);
//        sb.append(", createtime=").append(createTime);
//        sb.append(", updatetime=").append(updatetime);
//        sb.append(", thumbcount=").append(thumbCount);
//        sb.append(", coverimage=").append(coverImage);
//        sb.append("]");
//        return sb.toString();
//    }
}