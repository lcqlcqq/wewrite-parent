package com.quan.wewrite.service.impl;

import com.quan.wewrite.service.LoginService;
import com.quan.wewrite.service.SendMsgService;
import com.quan.wewrite.vo.ErrorCode;
import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.params.CodeParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class sendMsgServiceImpl implements SendMsgService {
    @Autowired
    private JavaMailSenderImpl javaMailSender;
    @Autowired
    private LoginService loginService;

    @Override
    public Result sendCodeRegister(CodeParam codeParam) {
        String findSysUserAcc = loginService.checkMail(codeParam.getEmail());
        if(!findSysUserAcc.isEmpty()){
            return Result.fail(ErrorCode.EMAIL_EXIST.getCode(), ErrorCode.EMAIL_EXIST.getMsg());
        }
        try{
            sendRegisterMail(codeParam.getEmail(),codeParam.getCode());
        }catch (Exception e){
            e.printStackTrace();
        }
        return Result.success("验证码发送成功");
    }

    @Override
    public Result sendCodeToEmail(CodeParam codeParam) {
        String findSysUserAcc = loginService.checkMail(codeParam.getEmail());
        if(findSysUserAcc.isEmpty()){
            return Result.fail(ErrorCode.EMAIL_NOTEXIST.getCode(), ErrorCode.EMAIL_NOTEXIST.getMsg());
        }
        try{
            sendResetPwdMail(codeParam.getEmail(),codeParam.getCode());
        }catch (Exception e){
            e.printStackTrace();
        }
        return Result.success("验证码发送成功");
    }


    public void sendResetPwdMail(String receiver, String code) {
        System.out.println(receiver+code);
        // 构建一个邮件对象
        SimpleMailMessage message = new SimpleMailMessage();
        // 设置邮件主题
        message.setSubject("WeWrite");
        // 设置邮件发送者，这个跟application.yml中设置的要一致
        message.setFrom("wewr1te@163.com");
        // 设置邮件接收者，可以有多个接收者，用逗号隔开
        message.setTo(receiver);
        // 设置邮件抄送人，可以有多个抄送人
        //message.setCc("WeWrite");
        // 设置隐秘抄送人，可以有多个
        //message.setBcc("7******9@qq.com");
        // 设置邮件发送日期
        message.setSentDate(new Date());
        // 设置邮件的正文
        message.setText("WeWrite账号密码找回\n您正在申请找回密码，验证码为: "+code+"。有效期5分钟。如非本人操作，请忽略。");
        // 发送邮件
        javaMailSender.send(message);
    }
    public void sendRegisterMail(String receiver, String code) {
        System.out.println(receiver+code);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("WeWrite");
        message.setFrom("wewr1te@163.com");
        message.setTo(receiver);
        message.setSentDate(new Date());
        message.setText("WeWrite账号注册\n您正在注册新账号，验证码为: "+code+"。有效期5分钟。如非本人操作，请忽略。");
        javaMailSender.send(message);
    }


}
