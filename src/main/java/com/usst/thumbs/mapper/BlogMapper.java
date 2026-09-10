package com.usst.thumbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usst.thumbs.model.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

/**
* @author 22097
* @description 针对表【blog】的数据库操作Mapper
* @createDate 2026-04-28 20:26:51
* @Entity generator.domain.Blog
*/
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {
    void batchUpdateThumbCount(@Param("countMap")Map<Long,Long> countMap);
    void batchUpdateFavoriteCount(@Param("countMap")Map<Long,Long> countMap);
    void batchUpdateCommentCount(@Param("countMap")Map<Long,Long> countMap);

    @Update("""
        update blog
        set hot_score = greatest(hot_score + #{delta}, 0)
        where id = #{blogId}
        """)
    int incrementHotScore(@Param("blogId") Long blogId, @Param("delta") double delta);

    @Select("""
        select b.id
        from blog b
        left join (select blog_id, count(*) as count_value from thumb group by blog_id) t
            on t.blog_id = b.id
        left join (select blog_id, count(*) as count_value from favorite group by blog_id) f
            on f.blog_id = b.id
        where b.is_delete = 0
          and (b.thumb_count <> coalesce(t.count_value, 0)
               or b.favorite_count <> coalesce(f.count_value, 0))
        order by b.id
        limit #{limit}
        """)
    java.util.List<Long> selectInteractionCountMismatchIds(@Param("limit") int limit);

    @Update("""
        update blog b
        left join (select blog_id, count(*) as count_value from thumb group by blog_id) t
            on t.blog_id = b.id
        left join (select blog_id, count(*) as count_value from favorite group by blog_id) f
            on f.blog_id = b.id
        set b.thumb_count = coalesce(t.count_value, 0),
            b.favorite_count = coalesce(f.count_value, 0)
        where b.id = #{blogId}
          and b.is_delete = 0
        """)
    int reconcileInteractionCounts(@Param("blogId") Long blogId);

}




