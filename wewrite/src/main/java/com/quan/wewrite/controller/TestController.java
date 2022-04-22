package com.quan.wewrite.controller;

import com.quan.wewrite.dao.pojo.SysUser;
import com.quan.wewrite.utils.UserThreadLocal;
import com.quan.wewrite.vo.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @RequestMapping
    public Result test(){
        //使用ThreadLocal保存用户信息
        SysUser sysUser = UserThreadLocal.get();
        System.out.println(sysUser);
        return Result.success(null);
    }
}
