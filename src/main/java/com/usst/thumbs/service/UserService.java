package com.usst.thumbs.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.UserUpdateRequest;
import com.usst.thumbs.model.vo.UserProfileVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
* @author 22097
* @description 针对表【user】的数据库操作Service
* @createDate 2026-04-28 17:21:25
*/
@Service
public interface UserService extends IService<User> {

    Integer register(String account, String password, String checkPassword);

    User login(String account,String password,HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    Boolean logout(HttpServletRequest request);

    Boolean updateUser(UserUpdateRequest request, HttpServletRequest httpServletRequest);

    UserProfileVO getUserProfile(Long userId);

    Boolean banUser(Long userId, HttpServletRequest request);

    Boolean unBanUser(Long userId,HttpServletRequest request);
}
