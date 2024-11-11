package com.qkinfotech.core.app;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import com.qkinfotech.core.security.BaseModule;

@Component("module:app")
public class ModuleSecurity extends BaseModule {

	@Override
	public void configAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
		requests.requestMatchers("/sys/config/save").hasAuthority("AUTH_APP_OPERATION_MANAGER");
		requests.requestMatchers("/sys/config/delete?**").hasAuthority("AUTH_APP_OPERATION_MANAGER");
	}
}
