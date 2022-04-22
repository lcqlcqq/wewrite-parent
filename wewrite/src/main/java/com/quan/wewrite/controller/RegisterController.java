package com.quan.wewrite.controller;

import com.quan.wewrite.common.aop.LogAnnotation;
import com.quan.wewrite.service.LoginService;
import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.params.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("register")
public class RegisterController {

    @Autowired
    private LoginService loginService;

    /**
     * 注册
     * @param loginParam
     * @return
     */
    @PostMapping
    @LogAnnotation(module = "注册",operation = "用户注册")
    public Result register(@RequestBody LoginParam loginParam){
        return loginService.register(loginParam);
    }
}
