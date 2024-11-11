package com.qkinfotech.app.demo;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

import com.qkinfotech.core.security.BaseModule;

//@Configuration("module:app.demo")
public class ModuleSecurity extends BaseModule {

	@Override
	public void configAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
		requests.requestMatchers("/hi").hasAuthority("AUTH_USER");
		requests.requestMatchers("/hello").hasAuthority("AUTH_USER");
		requests.requestMatchers("/hoo").authenticated();
		requests.requestMatchers("/download/*").hasAuthority("AUTH_USER");
		
		
		
	}
	
	public static final List<String> AUTHORITIES = List.of(
			"AUTH_APP_DEMO_ADD",
			"AUTH_APP_DEMO_UPD",
			"AUTH_APP_DEMO_DEL",
			"AUTH_APP_DEMO_EXP",
			"AUTH_APP_DEMO_IMP"
	);
	
	@Override
	public String group() {
		return "app";
	}

	@Override
	public String module() {
		return "demo";
	}

	@Override
	public List<String> authorities() {
		return AUTHORITIES;
	}
	
}
