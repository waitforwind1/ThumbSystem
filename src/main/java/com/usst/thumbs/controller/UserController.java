package com.usst.thumbs.controller;

import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.UserUpdateRequest;
import com.usst.thumbs.model.vo.UserProfileVO;
import com.usst.thumbs.model.vo.UserVO;
import com.usst.thumbs.result.Result;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.result.ResultUtils;
import com.usst.thumbs.service.FavoriteService;
import com.usst.thumbs.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.usst.thumbs.common.UserConstant.USER_IS_ADMIN;

@RestController
@RequestMapping("/user")
@Tag(name = "用户接口")
public class UserController {

    private final UserService userService;
    private final FavoriteService favoriteService;

    public UserController(UserService userService, FavoriteService favoriteService) {
        this.userService = userService;
        this.favoriteService = favoriteService;
    }

    @PostMapping("/login")
    public Result<UserVO> login(String account, String password, HttpServletRequest request){
        if(account==null||password==null)
            throw new BusinessException(ResultType.PARAMS_EMPTY,"请输入参数");
        User user = userService.login(account,password,request);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        return ResultUtils.success(ResultType.SUCCESS,userVO,"登录成功");
    }

    @PostMapping("/register")
    public Result<Integer> register(String account, String password, String checkPassword){
        if(account==null || password==null||checkPassword==null)
            throw new BusinessException(ResultType.PARAMS_EMPTY,"输入不允许为空");
        if(!password.equals(checkPassword))
            throw new BusinessException(ResultType.PARAM_ERROR,"两次密码输入不一致");
        Integer register = userService.register(account, password, checkPassword);
        return ResultUtils.success(ResultType.SUCCESS,register,"注册成功");
    }

    @PostMapping("/logout")
    public Result<Boolean> logout(HttpServletRequest request){
        return ResultUtils.success(userService.logout(request));
    }

    @GetMapping("/current")
    public Result<UserVO> current(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    @GetMapping("/getFav")
    public Result<List<Blog>> getFav(HttpServletRequest request){
        if(request==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        List<Blog> blogList = favoriteService.listMyFavorite(request);
        return ResultUtils.success(blogList);
    }

    @PostMapping("/profile/update")
    public Result<Boolean> updateProfile(@RequestBody UserUpdateRequest updateRequest,
                                         HttpServletRequest request) {
        return ResultUtils.success(userService.updateUser(updateRequest, request));
    }

    @GetMapping("/{userId}/profile")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        return ResultUtils.success(userService.getUserProfile(userId));
    }

    @GetMapping("/admin/list")
    public Result<List<UserVO>> listUsers(HttpServletRequest request) {
        assertAdmin(request);
        List<UserVO> users = userService.list().stream()
                .map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                })
                .collect(Collectors.toList());
        return ResultUtils.success(users);
    }

    @PostMapping("/admin/ban/{userId}")
    public Result<Boolean> banUser(@PathVariable Long userId, HttpServletRequest request) {
        return ResultUtils.success(userService.banUser(userId, request));
    }

    @PostMapping("/admin/unban/{userId}")
    public Result<Boolean> unbanUser(@PathVariable Long userId, HttpServletRequest request) {
        return ResultUtils.success(userService.unBanUser(userId, request));
    }

    private void assertAdmin(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser.getIsAdmin() == null || !loginUser.getIsAdmin().equals(USER_IS_ADMIN)) {
            throw new BusinessException(ResultType.NO_AUTH, "无管理员权限");
        }
    }
}
