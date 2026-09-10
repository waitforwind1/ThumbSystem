package com.usst.thumbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usst.thumbs.model.Favorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author 22097
* @description 针对表【favorite】的数据库操作Mapper
* @createDate 2026-05-16 18:18:35
* @Entity com.usst.thumbs.model.Favorite
*/
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Insert("""
    insert ignore into favorite(user_id, blog_id)
    values(#{userId}, #{blogId})
    """)
    int insertIgnore(@Param("userId") Long userId, @Param("blogId") Long blogId);

    @Delete("""
    delete from favorite
    where user_id = #{userId} and blog_id = #{blogId}
    """)
    int deleteByUserIdAndBlogId(@Param("userId") Long userId, @Param("blogId") Long blogId);
}




