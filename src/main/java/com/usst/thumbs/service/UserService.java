package com.usst.thumbs.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.thumbs.model.User;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author 22097
* @description 针对表【user】的数据库操作Service
* @createDate 2026-04-28 17:21:25
*/
public interface UserService extends IService<User> {

    Integer register(String account, String password, String checkPassword);

    User login(String account,String password,HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);


}
