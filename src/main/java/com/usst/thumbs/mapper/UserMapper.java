package com.usst.thumbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usst.thumbs.model.User;
import org.apache.ibatis.annotations.Mapper;


/**
* @author 22097
* @description 针对表【user】的数据库操作Mapper
* @createDate 2026-04-28 17:21:25
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




