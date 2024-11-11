package com.qkinfotech;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * 替换Bean的写法
 *
 */
public class TestReplaceBean implements ApplicationContextAware, BeanPostProcessor {

	protected ApplicationContext applicationContext;
	
	protected String beanName;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;	
	}
	
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if(beanName.equals(this.beanName)) {
			return this;
		}
		return bean;
	}

	
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if(beanName.equals(this.beanName)) {
			return this;
		}
		return bean;
	}
}
