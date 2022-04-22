package com.quan.wewrite.controller;

import com.quan.wewrite.common.aop.LogAnnotation;
import com.quan.wewrite.service.LoginService;
import com.quan.wewrite.service.SendMsgService;
import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.params.CodeParam;
import com.quan.wewrite.vo.params.ResetPwdParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sendCode")
public class SendCodeController {
    @Autowired
    private SendMsgService sendMsgService;
    @Autowired
    private LoginService loginService;

    @PostMapping("send")
    @LogAnnotation(module = "发送",operation = "找回密码的邮件验证码")
    public Result sendCode(@RequestBody CodeParam codeParam){
        return sendMsgService.sendCodeToEmail(codeParam);
    }

    @PostMapping("reg")
    @LogAnnotation(module = "发送",operation = "注册的邮件验证码")
    public Result sendCodeREG(@RequestBody CodeParam codeParam){
        return sendMsgService.sendCodeRegister(codeParam);
    }

    @PostMapping("resetPwd")
    @LogAnnotation(module = "发送",operation = "重置密码")
    public Result resetPwd(@RequestBody ResetPwdParam resetPwdParam){
        return loginService.resetPassWord(resetPwdParam);
    }
}
