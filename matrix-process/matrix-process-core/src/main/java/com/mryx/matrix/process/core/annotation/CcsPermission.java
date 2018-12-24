package com.mryx.matrix.process.core.annotation;

import java.lang.annotation.*;

/**
 * CCS 权限
 *
 * @author supeng
 * @date 2018/10/26
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CcsPermission {
    String[] value() default {};
}
