package com.usst.thumbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usst.thumbs.model.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 22097
* @description 针对表【favorite】的数据库操作Mapper
* @createDate 2026-05-16 18:18:35
* @Entity com.usst.thumbs.model.Favorite
*/
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

}




