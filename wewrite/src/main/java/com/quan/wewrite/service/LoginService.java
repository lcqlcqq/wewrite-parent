package com.quan.wewrite.service;

import com.quan.wewrite.dao.pojo.SysUser;
import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.params.LoginEmailParam;
import com.quan.wewrite.vo.params.LoginParam;
import com.quan.wewrite.vo.params.ResetPwdParam;
import org.springframework.transaction.annotation.Transactional;

@Transactional  //事务回滚
public interface LoginService {
    /**
     * 登录
     * @param loginParam
     * @return
     */
    Result login(LoginParam loginParam);

    /**
     * 登出
     * @param token
     * @return
     */
    Result logout(String token);

    /**
     * 注册
     * @param loginParam
     * @return
     */
    Result register(LoginParam loginParam);

    SysUser checkToken(String token);

    /**
     * 用户邮箱登录
     * @param loginEmailParam
     * @return
     */
    Result loginByEmail(LoginEmailParam loginEmailParam);

    /**
     * 验证邮箱是否存在
     */
    String checkMail(String mail);

    /**
     * 重置密码
     * @param resetPwdParam
     * @return
     */
    Result resetPassWord(ResetPwdParam resetPwdParam);
}
