package com.qkinfotech.core.task;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Task {

	@AliasFor(annotation = Component.class)
	String value() default "";

	String trigger() default "";

	String group() default "";
	
	String name() default "";
	
	
}
