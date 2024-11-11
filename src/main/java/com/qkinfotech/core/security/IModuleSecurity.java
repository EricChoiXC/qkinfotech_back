package com.qkinfotech.core.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.qkinfotech.core.sentinel.SentinelRequestRuleBuilder;

public interface IModuleSecurity {
	
	void configAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests);

	void configSecurityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, UserDetailsService userDetailsService);

	void configSentinel(SentinelRequestRuleBuilder sentinelRequestRuleBuilder);
}
