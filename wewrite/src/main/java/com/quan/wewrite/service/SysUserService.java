package com.quan.wewrite.service;

import com.quan.wewrite.dao.pojo.SysUser;
import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.UserVo;

import java.util.Date;

public interface SysUserService {
    SysUser findSysUserById(Long id);

    SysUser findUser(String account, String pwd);

    /**
     * 根据token查询用户信息，去redis找
     * @param token
     * @return
     */
    Result getUserInfoByToken(String token);

    /**
     * 查找用户名，注册用
     * @param account
     * @return
     */
    SysUser findUserByAccount(String account);

    /**
     * 查询nickname是否已存在，注册用
     * @param nickname
     * @return
     */
    SysUser findUserByNickname(String nickname);
    /**
     * 保存用户名信息，注册用
     * @param sysUser
     */
    void save(SysUser sysUser);

    /**
     * 评论 通过id获取用户
     * @param id
     * @return
     */
    UserVo findUserVoById(Long id);

    Integer updateLastLoginTime(Date date,Long id);

}
