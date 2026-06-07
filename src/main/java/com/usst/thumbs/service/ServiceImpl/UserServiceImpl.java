package com.usst.thumbs.service.ServiceImpl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.common.UserConstant;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.mapper.BlogMapper;
import com.usst.thumbs.mapper.UserMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.User;
import com.usst.thumbs.model.request.UserUpdateRequest;
import com.usst.thumbs.model.vo.UserProfileVO;
import com.usst.thumbs.model.vo.UserVO;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.usst.thumbs.common.UserConstant.USER_IS_ADMIN;

/**
* @author 22097
* @description 针对表【user】的数据库操作Service实现
* @createDate 2026-04-28 17:21:25
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    private static final String USER_LOGIN_STATE = "USER_LOGIN_STATE";
    private static final String SALT = "kyrie";

    private final UserMapper userMapper;
    private final BlogMapper blogMapper;

    public UserServiceImpl(UserMapper userMapper, BlogMapper blogMapper) {
        this.userMapper = userMapper;
        this.blogMapper = blogMapper;
    }


    @Override
    public Integer register(String account, String password, String checkPassword) {
        if(StrUtil.isBlank(account) || StrUtil.isBlank(password))
            throw new BusinessException(ResultType.PARAM_ERROR,"账号密码不允许为空");
        if(!password.equals(checkPassword))
            throw new BusinessException(ResultType.PARAM_ERROR,"两次输入密码不一致");
        if(account.length()<6||account.length()>20)
            throw new BusinessException(ResultType.PARAM_ERROR,"账号长度不合法");
        if(password.length()<6||password.length()>20)
            throw new BusinessException(ResultType.PARAM_ERROR,"密码长度不合法");
        String illegalRegex = ".*[^A-Za-z0-9_].";
        if(account.matches(illegalRegex))
            throw new BusinessException(ResultType.PARAM_ERROR,"不允许包含特殊字符");
        String regex = "^[A-Za-z0-9_]+$";
        if(!account.matches(regex))
            throw new BusinessException(ResultType.PARAM_ERROR,"账号格式允许字母数字下划线");
        User one = this.lambdaQuery().eq(User::getAccount, account).one();
        if(one!=null)
            throw new BusinessException(ResultType.USER_ACCOUNT_EXISTS,"账号已存在");
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+password).getBytes(StandardCharsets.UTF_8));
        User user = new User();
        user.setAccount(account);
        user.setPassword(encryptPassword);
        return userMapper.insert(user);
    }


    @Override
    public User login(String account, String password,HttpServletRequest request) {
        if(StrUtil.isBlank(account) || StrUtil.isBlank(password))
            throw new BusinessException(ResultType.PARAM_ERROR,"账号密码不允许为空");
        if(account.length()<6||account.length()>20)
            throw new BusinessException(ResultType.PARAM_ERROR,"账号长度不合法");
        if(password.length()<6||password.length()>20)
            throw new BusinessException(ResultType.PARAM_ERROR,"密码长度不合法");
        String illegalRegex = ".*[^A-Za-z0-9_].";
        if(account.matches(illegalRegex))
            throw new BusinessException(ResultType.PARAM_ERROR,"不允许包含特殊字符");
        String regex = "^[A-Za-z0-9_]+$";
        if(!account.matches(regex))
            throw new BusinessException(ResultType.PARAM_ERROR,"账号格式允许字母数字下划线");

        String newPassword = DigestUtils.md5DigestAsHex((SALT+password).getBytes(StandardCharsets.UTF_8));
        User user = this.lambdaQuery().eq(User::getAccount, account).eq(User::getPassword, newPassword).one();
        if(user==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"用户不存在");
        if(user.getStatus().equals(UserConstant.USER_IS_BAN))
            throw new BusinessException(ResultType.NO_AUTH,"用户已被封禁,找管理员解封");
        User safeUser = getsafeUser(user);
        request.getSession().setAttribute(USER_LOGIN_STATE,safeUser);
        return safeUser;
    }


    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        Object object = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(object instanceof User user){
            return user;
        }
        throw new BusinessException(ResultType.NOT_LOGIN,"用户未登录");
    }

    @Override
    public Boolean logout(HttpServletRequest request) {
        if(request==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        request.getSession().invalidate();
        return true;
    }

    @Override
    public Boolean updateUser(UserUpdateRequest request, HttpServletRequest httpServletRequest) {
        User loginUser = getLoginUser(httpServletRequest);
        String newUserName = request.getNewUserName()==null?loginUser.getUsername():request.getNewUserName();
        String newAvatar = request.getNewAvatar()==null? loginUser.getAvatar() : request.getNewAvatar();
        boolean updated = this.lambdaUpdate()
                .eq(User::getId, loginUser.getId())
                .set(User::getUsername, newUserName)
                .set(User::getAvatar, newAvatar)
                .update();
        if(!updated)
            throw new BusinessException(ResultType.DATABASE_ERROR,"更新失败");
        User newUser = this.getById(loginUser.getId());
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE,getsafeUser(newUser));
        return true;
    }

    /**
     * 获取用户信息
     * @param userId
     * @return
     */
    @Override
    public UserProfileVO getUserProfile(Long userId) {
        if(userId==null || userId<=0)
            throw new BusinessException(ResultType.PARAM_ERROR,"用户ID不合法");
        User user = this.getById(userId);
        if(user==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"用户不存在");
        Long blogCount = blogMapper.selectCount(new LambdaQueryWrapper<Blog>()
                .eq(Blog::getUserId, userId));
        return UserProfileVO.builder()
                .id(user.getId())
                .userName(user.getUsername())
                .avatar(user.getAvatar())
                .account(user.getAccount())
                .blogCount(blogCount)
                .createTime(user.getCreateTime())
                .build();
    }

    @Override
    public Boolean banUser(Long userId, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        if(!loginUser.getIsAdmin().equals(UserConstant.USER_IS_ADMIN))
            throw new BusinessException(ResultType.NO_AUTH,"无权限");
        if(userId==null || userId<=0)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数不合法");
        return this.lambdaUpdate().eq(User::getId, userId).set(User::getStatus, UserConstant.USER_IS_BAN).update();
    }

    @Override
    public Boolean unBanUser(Long userId, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        if(!loginUser.getIsAdmin().equals(UserConstant.USER_IS_ADMIN))
            throw new BusinessException(ResultType.NO_AUTH,"无权限");
        if(userId==null || userId<=0)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数不合法");
        return this.lambdaUpdate().eq(User::getId,userId).set(User::getStatus,UserConstant.USER_NOT_BAN).update();
    }

    @Override
    public Page<UserVO> userList(Integer pageNo,Integer pageSize,HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        if (!loginUser.getIsAdmin().equals(USER_IS_ADMIN)) {
            throw new BusinessException(ResultType.NO_AUTH, "无管理员权限");
        }
        int current = pageNo==null||pageNo<1?2:pageNo;
        int size = pageSize==null || pageSize<1?7:Math.max(pageSize,50);
        Page<User> page = new Page<>(current,size);
        Page<User> userPage = this.page(page);
        Page<UserVO> userVOPage = new Page<>(
                userPage.getCurrent(),
                userPage.getSize(),
                userPage.getTotal()
        );
        List<UserVO> userVOList = userPage.getRecords().stream()
                .map(this::convertToUservo)
                .toList();
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

    private UserVO convertToUservo(User user){
        if(user==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"参数错误");
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setStatus(user.getStatus());
        userVO.setAvatar(user.getAvatar());
        userVO.setAccount(user.getAccount());
        userVO.setUsername(user.getUsername());
        userVO.setIsAdmin(user.getIsAdmin());
        userVO.setCreateTime(user.getCreateTime());
        userVO.setUpdateTime(user.getUpdateTime());
        return userVO;
    }

    public User getsafeUser(User user){
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setAccount(user.getAccount());
        safeUser.setUsername(user.getUsername());
        safeUser.setAvatar(user.getAvatar());
        safeUser.setIsAdmin(user.getIsAdmin());
        safeUser.setStatus(user.getStatus());
        safeUser.setCreateTime(user.getCreateTime());
        safeUser.setUpdateTime(user.getUpdateTime());
        return safeUser;
    }


}




