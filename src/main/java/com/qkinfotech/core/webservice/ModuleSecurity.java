package com.qkinfotech.core.webservice;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.qkinfotech.core.security.BaseModule;

import jakarta.servlet.http.HttpServletRequest;

@Configuration("module:core.webservice")
public class ModuleSecurity extends BaseModule {

	@Override
	public void configAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
		requests.requestMatchers(new RequestMatcher(){
			@Override
			public boolean matches(HttpServletRequest request) {
				String servletPath = request.getServletPath();
				return "/webservice".equals(servletPath);
			}
			
		}).permitAll();
	}
	
}
