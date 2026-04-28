package com.usst.thumbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usst.thumbs.model.Blog;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 22097
* @description 针对表【blog】的数据库操作Mapper
* @createDate 2026-04-28 20:26:51
* @Entity generator.domain.Blog
*/
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {

}




