package com.usst.thumbs.service.ServiceImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.thumbs.common.ResultType;
import com.usst.thumbs.common.UserConstant;
import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.model.User;
import com.usst.thumbs.mapper.UserMapper;
import com.usst.thumbs.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.swing.plaf.synth.SynthLookAndFeel;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    @Override
    public Integer register(String account, String password, String checkPassword) {
        if(account ==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"账号不允许为空");
        if(password ==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"密码不允许为空");
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
        QueryWrapper queryWrapper= new QueryWrapper();
        queryWrapper.eq("account",account);
        if(userMapper.selectOne(queryWrapper)!=null)
            throw new BusinessException(ResultType.USER_ACCOUNT_EXISTS,"账号已存在");
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+password).getBytes(StandardCharsets.UTF_8));
        User user = new User();
        user.setAccount(account);
        user.setPassword(encryptPassword);
        return userMapper.insert(user);
    }


    @Override
    public User login(String account, String password,HttpServletRequest request) {
        if(account ==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"账号不允许为空");
        if(password ==null)
            throw new BusinessException(ResultType.PARAM_ERROR,"密码不允许为空");
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
        QueryWrapper queryWrapper= new QueryWrapper();
        queryWrapper.eq("account",account);
        queryWrapper.eq("password",newPassword);
        User user = userMapper.selectOne(queryWrapper);
        if(user==null)
            throw new BusinessException(ResultType.USER_NOT_EXIST,"用户不存在");
        User safeUser = getsafeUser(user);
        request.getSession().setAttribute(USER_LOGIN_STATE,safeUser);
        return safeUser;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        return user;
    }


    public User getsafeUser(User user){
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setAccount(user.getAccount());
        safeUser.setUsername(user.getUsername());
        safeUser.setAvatar(user.getAvatar());
        safeUser.setIsAdmin(user.getIsAdmin());
        safeUser.setCreateTime(user.getCreateTime());
        safeUser.setUpdateTime(user.getUpdateTime());
        return safeUser;
    }


}




