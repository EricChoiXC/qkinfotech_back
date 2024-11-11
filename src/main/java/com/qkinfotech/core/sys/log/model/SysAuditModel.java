package com.qkinfotech.core.sys.log.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SysAuditModel {

    String modelName() default "";

}
