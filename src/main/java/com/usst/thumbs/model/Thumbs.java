package com.usst.thumbs.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName thumbs
 */
@TableName(value ="thumbs")
@Data
public class Thumbs {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 点赞用户id
     */
    private Long userId;

    /**
     * 文章id
     */
    private Long blogId;

    /**
     * 点赞时间
     */
    private Date createTime;

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
//        Thumbs other = (Thumbs) that;
//        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
//            && (this.getUserid() == null ? other.getUserid() == null : this.getUserid().equals(other.getUserid()))
//            && (this.getBlogid() == null ? other.getBlogid() == null : this.getBlogid().equals(other.getBlogid()))
//            && (this.getCreatetime() == null ? other.getCreatetime() == null : this.getCreatetime().equals(other.getCreatetime()));
//    }
//
//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
//        result = prime * result + ((getUserid() == null) ? 0 : getUserid().hashCode());
//        result = prime * result + ((getBlogid() == null) ? 0 : getBlogid().hashCode());
//        result = prime * result + ((getCreatetime() == null) ? 0 : getCreatetime().hashCode());
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
//        sb.append(", userid=").append(userid);
//        sb.append(", blogid=").append(blogid);
//        sb.append(", createtime=").append(createtime);
//        sb.append("]");
//        return sb.toString();
//    }
}