package com.quan.wewrite.controller;

import com.quan.wewrite.common.aop.LogAnnotation;
import com.quan.wewrite.service.LoginService;
import com.quan.wewrite.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("logout")
public class LogoutController {

    @Autowired
    private LoginService loginService;

    @GetMapping
    @LogAnnotation(module = "登出",operation = "用户登出")
    public Result logout(@RequestHeader("Authorization") String token){
        return loginService.logout(token);
    }
}
