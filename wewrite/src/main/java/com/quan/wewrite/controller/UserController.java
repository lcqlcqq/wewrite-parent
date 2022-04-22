package com.quan.wewrite.controller;

import com.quan.wewrite.common.aop.LogAnnotation;
import com.quan.wewrite.service.SysUserService;
import com.quan.wewrite.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("users")
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("currentUser")
    @LogAnnotation(module = "用户",operation = "获取当前用户")
    public Result currentUser(@RequestHeader("Authorization") String token){
        return sysUserService.getUserInfoByToken(token);
    }
}
