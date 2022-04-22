package com.quan.wewrite.utils;

import com.quan.wewrite.dao.pojo.SysUser;

public class UserThreadLocal {

    private UserThreadLocal(){}
    //线程变量隔离
    //每个线程存储特定信息，互不干扰
    private static final ThreadLocal<SysUser> LOCAL = new ThreadLocal<>();

    public static void put(SysUser sysUser){
        LOCAL.set(sysUser);
    }
    public static SysUser get(){
        return LOCAL.get();
    }
    public static void remove(){
        LOCAL.remove();
    }
}
