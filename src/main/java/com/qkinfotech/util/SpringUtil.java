package com.qkinfotech.util;

import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.qkinfotech.QTaskApplication;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SpringUtil implements ApplicationContextAware, EnvironmentAware {
	
	private static ConfigurableApplicationContext context;
	
	private static SpringApplication application;
	
	private static Environment environment;
	
	private static String node;
	
	public static ConfigurableApplicationContext getContext() {
		return SpringUtil.context;
	}

	public static SpringApplication getApplication() {
		return SpringUtil.application;
	}
	
	public static Environment getEnvironment() {
		return SpringUtil.environment;
	}

	public static String getNode() {
		return SpringUtil.node;
	}

	public static void start(Class<QTaskApplication> clazz, String[] args) {

		long start = System.currentTimeMillis();
		SpringUtil.application = new SpringApplication(clazz);
		SpringUtil.application.setBannerMode(Mode.OFF);
		try {
			SpringUtil.application.run(args);
		}catch(Exception e) {
			LoggerFactory.getLogger(SpringUtil.class).error(e.getMessage(), e);
		}
		
		logger.info("Spring started. cost " + (System.currentTimeMillis() - start) + " ms");
	}

	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringUtil.context = (ConfigurableApplicationContext)applicationContext;
	}

	@Override
	public void setEnvironment(Environment environment) {
		SpringUtil.environment = environment;
		SpringUtil.node = environment.getProperty("application.node-name");
		
	}

}
