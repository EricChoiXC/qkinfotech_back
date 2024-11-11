package com.qkinfotech.core.log;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import com.qkinfotech.core.security.BaseModule;

@Component("module:sys.log")
public class ModuleSecurity extends BaseModule {

	@Override
	public void configAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
		requests.requestMatchers("/sys/log/*").hasAuthority("AUTH_ADMIN");

	}
}
