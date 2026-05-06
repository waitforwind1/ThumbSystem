package com.usst.thumbs.controller;

import com.usst.thumbs.result.Result;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.result.ResultUtils;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.vo.UserVO;
import com.usst.thumbs.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Tag(name = "用户接口")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<UserVO> userLogin(String account, String password, HttpServletRequest request){
        if(account==null||password==null)
            throw new BusinessException(ResultType.PARAMS_EMPTY,"请输入参数");
        User user = userService.login(account,password,request);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        return ResultUtils.success(ResultType.SUCCESS,userVO,"登录成功");
    }

    @PostMapping("/register")
    public Result userRegister(String account, String password,String checkPassword){
        if(account==null || password==null||checkPassword==null)
            throw new BusinessException(ResultType.PARAMS_EMPTY,"输入不允许为空");
        if(!password.equals(checkPassword))
            throw new BusinessException(ResultType.PARAM_ERROR,"两次密码输入不一致");
        Integer register = userService.register(account, password, checkPassword);
        return ResultUtils.success(ResultType.SUCCESS,register,"注册成功");
    }
}
