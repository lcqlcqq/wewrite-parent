package com.quan.wewrite.handler;

import com.alibaba.fastjson.JSON;
import com.quan.wewrite.dao.pojo.SysUser;
import com.quan.wewrite.service.LoginService;
import com.quan.wewrite.utils.UserThreadLocal;
import com.quan.wewrite.vo.ErrorCode;
import com.quan.wewrite.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private LoginService loginService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        /**
         * 1. 判断请求的接口路径是否为 HandlerMethod（Controller方法）
         * 2. 判断token是否为空，空则未登录
         * 3. token不为空，登录验证 loginService checkToken
         * 4. 认证成功 放行
         */
        if (!(handler instanceof HandlerMethod)){
            //handler 可能是 RequestResourceHandler  SpringBoot程序访问静态资源默认去classpath下的static目录查询
            return true;
        }
        String token = request.getHeader("Authorization");
        log.info("=================request start===========================");
        String requestURI = request.getRequestURI();
        log.info("request uri:{}",requestURI);
        log.info("request method:{}",request.getMethod());
        log.info("token:{}", token);
        log.info("=================request end===========================");
        //StringUtils.isBlank(token)
        if (token == null){
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登录");
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }
        SysUser sysUser = loginService.checkToken(token);
        if (sysUser == null){
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登录");
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }
        //是登录状态，放行
        //希望在controller中 直接获取用户信息  用ThreadLocal保存用户
        UserThreadLocal.put(sysUser);
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //删除ThreadLocal中的信息 防止内存泄露
        UserThreadLocal.remove();

    }
}
