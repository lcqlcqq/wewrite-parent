package com.quan.wewrite.controller;

import com.quan.wewrite.common.aop.LogAnnotation;
import com.quan.wewrite.service.LoginService;
import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.params.LoginEmailParam;
import com.quan.wewrite.vo.params.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping
    @LogAnnotation(module = "登录",operation = "用户账号登录")
    public Result login(@RequestBody LoginParam loginParam){

        return loginService.login(loginParam);
    }
    @PostMapping("mail")
    @LogAnnotation(module = "登录",operation = "用户邮箱登录")
    public Result loginByEmail(@RequestBody LoginEmailParam loginEmailParam){
        return loginService.loginByEmail(loginEmailParam);
    }


}
