package com.qkinfotech.core.task;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import com.qkinfotech.core.security.BaseModule;

@Component("module:task")
public class ModuleSecurity extends BaseModule {

	@Override
	public void configAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
		requests.requestMatchers("/taskAction/**").hasAuthority("AUTH_ADMIN");
		requests.requestMatchers("/task/list").permitAll();
		requests.requestMatchers("/task/delete?**").hasAuthority("AUTH_ADMIN");
		requests.requestMatchers("/task/log/delete?**").hasAuthority("AUTH_ADMIN");
		requests.requestMatchers("/task/log/detail/delete?**").hasAuthority("AUTH_ADMIN");
		requests.requestMatchers("/task/history/delete?**").hasAuthority("AUTH_ADMIN");
	}
}