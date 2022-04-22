package com.quan.wewrite.common.aop;

import com.alibaba.fastjson.JSON;
import com.quan.wewrite.utils.HttpContextUtils;
import com.quan.wewrite.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 日志切面：定义通知和切点的关系
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

    //切点
    @Pointcut("@annotation(com.quan.wewrite.common.aop.LogAnnotation)")
    public void logPointCut() {
    }
    //环绕通知
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        //执行方法
        Object result = point.proceed();
        //执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        //保存日志
        recordLog(point, time);
        return result;
    }

    private void recordLog(ProceedingJoinPoint joinPoint, long time) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogAnnotation logAnnotation = method.getAnnotation(LogAnnotation.class);
        log.info("=====================log start================================");
        log.info("module:{}, operation:{}, execute time : {} ms",logAnnotation.module(),logAnnotation.operation(),time);
        //请求的方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        log.info("request method:{}",className + "." + methodName + "()");

        //请求的参数
        Object[] args = joinPoint.getArgs();
        if(args.length>0) {
            String params = JSON.toJSONString(args[0]);
            log.info("request params:{}", params);
        }
        //获取request 设置IP地址
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        log.info("request ip:{}", IpUtils.getIpAddr(request));
        log.info("=====================log end================================");
    }

}
