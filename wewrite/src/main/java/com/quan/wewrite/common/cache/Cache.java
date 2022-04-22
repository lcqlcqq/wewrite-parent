package com.quan.wewrite.common.cache;


import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

    long expire() default 60 * 1000;

    //缓存标识
    String name() default "";

}
