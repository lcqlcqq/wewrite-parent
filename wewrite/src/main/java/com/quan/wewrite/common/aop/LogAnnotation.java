package com.quan.wewrite.common.aop;

import java.lang.annotation.*;

/**
 * 日志注解
 */
@Target(ElementType.METHOD)  //Method代表可以放在方法上，Type代表放在类上
@Retention(RetentionPolicy.RUNTIME)  //Runtime代表jvm层面的注解
@Documented
public @interface LogAnnotation {

    String module() default "";

    String operation() default "";
}
