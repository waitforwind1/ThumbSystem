package com.usst.thumbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usst.thumbs.model.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

}




