package com.qkinfotech.core.org.sercurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import com.qkinfotech.core.security.BaseModule;

@Component("module:org")
public class ModuleSecurity extends BaseModule {

	@Override
	public void configAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
		requests.requestMatchers("/org/person/delete?**").hasAuthority("AUTH_ADMIN");
		requests.requestMatchers("/org/element/delete?**").hasAuthority("AUTH_ADMIN");
		requests.requestMatchers("/org/company/delete?**").hasAuthority("AUTH_ADMIN");
		requests.requestMatchers("/org/dept/delete?**").hasAuthority("AUTH_ADMIN");
		requests.requestMatchers("/org/post/delete?**").hasAuthority("AUTH_ADMIN");
		requests.requestMatchers("/org/group/delete?**").hasAuthority("AUTH_ADMIN");
		requests.requestMatchers("/org/post/member/delete?**").hasAuthority("AUTH_ADMIN");
		requests.requestMatchers("/org/group/member/delete?**").hasAuthority("AUTH_ADMIN");
	}
}