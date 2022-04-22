package com.quan.wewrite.service;

import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.params.CodeParam;

public interface SendMsgService {
    /**
     * 发送邮件验证码
     * @param codeParam
     * @return
     */
    Result sendCodeToEmail(CodeParam codeParam);

    /**
     * 发送注册验证用的验证码
     * @param codeParam
     * @return
     */
    Result sendCodeRegister(CodeParam codeParam);
}
