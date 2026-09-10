package com.usst.thumbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usst.thumbs.model.Thumb;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author 22097
* @description 针对表【thumbs】的数据库操作Mapper
* @createDate 2026-04-28 21:04:00
* @Entity generator.domain.Thumbs
*/
@Mapper
public interface ThumbMapper extends BaseMapper<Thumb> {

    @Insert("""
    insert ignore into thumb(user_id, blog_id)
    values(#{userId}, #{blogId})
    """)
    int insertIgnore(@Param("userId") Long userId, @Param("blogId") Long blogId);

    @Delete("""
    delete from thumb
    where user_id = #{userId} and blog_id = #{blogId}
    """)
    int deleteByUserIdAndBlogId(@Param("userId") Long userId, @Param("blogId") Long blogId);
}




