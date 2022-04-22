package com.quan.wewrite.vo;

public enum  ErrorCode {

    PARAMS_ERROR(10001,"参数有误"),
    ACCOUNT_PWD_NOT_EXIST(10002,"用户名不存在或密码错误"),
    EMAIL_PWD_NOT_EXIST(10002,"邮箱账号不存在或密码错误"),
    EMAIL_ERROR(10003,"邮箱格式错误"),
    TOKEN_ERROR(10003,"token不合法"),
    ACCOUNT_EXIST(10004,"用户名或昵称已存在"),
    EMAIL_EXIST(10004,"该邮箱已被注册"),
    EMAIL_NOTEXIST(10004,"该邮箱未注册，请检查填写的邮箱"),
    NO_PERMISSION(70001,"无访问权限"),
    SESSION_TIME_OUT(90001,"会话超时"),
    NO_LOGIN(90002,"未登录"),
    CATEGORY_NAME_DUPLICATED(90003,"类别名称重复!"),
    CATEGORY_NOT_EXIST(90003,"类别不存在!"),
    TAG_NAME_DUPLICATED(90003,"标签名称重复!"),
    TAG_NOT_EXIST(90003,"标签不存在!");
    private int code;
    private String msg;

    ErrorCode(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
