package com.qkinfotech;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.qkinfotech.util.SpringUtil;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

@SpringBootApplication
@EnableMethodCache(basePackages = "com.qkinfotech")
@EnableJpaRepositories
@ServletComponentScan
@EnableAsync
public class QTaskApplication {
	
	public static void main(String[] args) {
		
		SpringUtil.start(QTaskApplication.class, args);

	}
	
}