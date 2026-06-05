package com.usst.thumbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usst.thumbs.model.Share;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 22097
* @description 针对表【share(保存分享内容数据)】的数据库操作Mapper
* @createDate 2026-05-16 18:18:38
* @Entity com.usst.thumbs.model.Share
*/
@Mapper
public interface ShareMapper extends BaseMapper<Share> {

}




