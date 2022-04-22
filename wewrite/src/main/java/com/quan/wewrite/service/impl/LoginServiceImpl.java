package com.quan.wewrite.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quan.wewrite.dao.mapper.SysUserMapper;
import com.quan.wewrite.dao.pojo.SysUser;
import com.quan.wewrite.service.LoginService;
import com.quan.wewrite.service.SysUserService;
import com.quan.wewrite.utils.JWTUtils;
import com.quan.wewrite.vo.ErrorCode;
import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.params.LoginEmailParam;
import com.quan.wewrite.vo.params.LoginParam;
import com.quan.wewrite.vo.params.ResetPwdParam;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service

public class LoginServiceImpl implements LoginService {
    //加密盐
    private static final String salt = "quan!@#";
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public Result login(LoginParam loginParam) {
        /**
         * 1. 检查参数合法性
         * 2. 用户名密码是否存在
         * 3. 不存在 登陆失败
         * 4. 存在 使用jwt生成token返回前端
         * 5. token放入redis中，redis 保存 token:user 的信息，设置过期时间
         *    所以登录时先去认证token合法性，去redis验证是否存在
         */
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }
        String pwd = DigestUtils.md5Hex(password + salt);
        //查询账号和对应md5密码是否存在
        SysUser sysUser = sysUserService.findUser(account,pwd);
        if (sysUser == null){
            return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(),ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }
        //登录成功，更新最近一次登录时间。
        sysUserService.updateLastLoginTime(new Date(),sysUser.getId());
        //登录成功，使用JWT生成token，返回token和redis中
        String token = JWTUtils.createToken(sysUser.getId());
        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1, TimeUnit.DAYS);
        System.out.println("login.");
        return Result.success(token);
    }
    @Override
    public Result logout(String token) {
        redisTemplate.delete("TOKEN_"+token);
        System.out.println("logout.");
        return Result.success(null);
    }

    @Override
    public Result register(LoginParam loginParam) {
        /**
         * 1. 检查参数合法性
         * 2. 检查账户是否已存在
         * 3. 注册，生成token，存入redis返回
         * 4. 事务，注册过程中出现问题回滚
         */
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        String nickname = loginParam.getNickname();
        if (StringUtils.isBlank(account)
                || StringUtils.isBlank(password)
                || StringUtils.isBlank(nickname)
        ){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }
        if(!checkEmail(loginParam.getEmail())){
            return Result.fail(ErrorCode.EMAIL_ERROR.getCode(),ErrorCode.EMAIL_ERROR.getMsg());
        }
        SysUser sysUser = this.sysUserService.findUserByAccount(account);
        SysUser NicknameUser = this.sysUserService.findUserByNickname(nickname);
        if (sysUser != null || NicknameUser !=null){
            return Result.fail(ErrorCode.ACCOUNT_EXIST.getCode(),ErrorCode.ACCOUNT_EXIST.getMsg());
        }
        sysUser = new SysUser();
        sysUser.setNickname(nickname);
        sysUser.setAccount(account);
        sysUser.setPassword(DigestUtils.md5Hex(password+ salt));
        sysUser.setCreateDate(System.currentTimeMillis());
        sysUser.setLastLogin(System.currentTimeMillis());
        sysUser.setAvatar("/static/user/user_default.png");
        sysUser.setAdmin(0); //是否管理员
        sysUser.setDeleted(0); //是否删除
        sysUser.setSalt("");
        sysUser.setStatus("");
        sysUser.setEmail(loginParam.getEmail());
        this.sysUserService.save(sysUser);

        //token
        String token = JWTUtils.createToken(sysUser.getId());

        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1, TimeUnit.DAYS);
        return Result.success(token);
    }


    @Override
    public Result resetPassWord(ResetPwdParam resetPwdParam) {
        SysUser sysUser = new SysUser();
        sysUser.setId(Long.parseLong(resetPwdParam.getId()));
        sysUser.setPassword( DigestUtils.md5Hex(resetPwdParam.getPwd() + salt));
        int update = sysUserMapper.updateById(sysUser);
        return update>0?Result.success(update):Result.fail(507,"重置密码失败");
    }

    @Override
    public String checkMail(String mail) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getEmail,mail);
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);
        if(sysUser == null) return "";
        return sysUser.getId().toString();
    }

    @Override
    public Result loginByEmail(LoginEmailParam loginEmailParam) {
        String email = loginEmailParam.getEmail();
        String password = loginEmailParam.getPassword();
        if (StringUtils.isBlank(email) || StringUtils.isBlank(password)){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }
        String pwd = DigestUtils.md5Hex(password + salt);
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getEmail,email);
        queryWrapper.eq(SysUser::getPassword,pwd);
        SysUser user = sysUserMapper.selectOne(queryWrapper);
        if (user == null){
            return Result.fail(ErrorCode.EMAIL_PWD_NOT_EXIST.getCode(),ErrorCode.EMAIL_PWD_NOT_EXIST.getMsg());
        }
        //登录成功，使用JWT生成token，返回token和redis中
        String token = JWTUtils.createToken(user.getId());
        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(user),1, TimeUnit.DAYS);
        return Result.success(token);
    }

    @Override
    public SysUser checkToken(String token) {
        if(StringUtils.isBlank(token)){
            return null;
        }
        Map<String,Object> stringObjectMap = JWTUtils.checkToken(token);
        if(stringObjectMap == null){
            return null;
        }
        String userJson = redisTemplate.opsForValue().get("TOKEN_"+token);
        if(StringUtils.isBlank(userJson)){
            return null;
        }
        return JSON.parseObject(userJson,SysUser.class);
    }
    public static boolean checkEmail(String email) {
        boolean flag = false;
        try {
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }
    ////生成密钥存到数据库里的
    //public static void main(String[] args) {
    //    System.out.println(DigestUtils.md5Hex("Administrator"));
    //}

}
