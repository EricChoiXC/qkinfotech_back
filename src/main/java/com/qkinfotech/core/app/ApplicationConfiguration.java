package com.qkinfotech.core.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alicp.jetcache.autoconfigure.CaffeineAutoConfiguration;
import com.alicp.jetcache.autoconfigure.LinkedHashMapAutoConfiguration;

//@Configuration
public class ApplicationConfiguration {

	@Bean
	 CaffeineAutoConfiguration  caffeineAutoConfiguration() {
		 return new CaffeineAutoConfiguration();
	 }
	@Bean
	LinkedHashMapAutoConfiguration  linkedHashMapAutoConfiguration() {
		 return new LinkedHashMapAutoConfiguration();
	 }
}
