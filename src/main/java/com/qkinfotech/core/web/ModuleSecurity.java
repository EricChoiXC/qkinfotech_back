package com.qkinfotech.core.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;

import com.qkinfotech.core.security.BaseModule;
import com.qkinfotech.core.web.thymeleaf.ThymeleafTemplateFilter;
import com.qkinfotech.core.web.vue.VueFilter;

@Configuration ("module:core.web")
public class ModuleSecurity extends BaseModule {

	@Value("${spring.thymeleaf.suffix:null}") 
	String thymeleafTemplateSuffix;

	@Override
	public void configAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
		/* 未启用方法过滤 */
		requests.requestMatchers("/web/approveProcessNode").denyAll();
		requests.requestMatchers("/web/getCalendar").denyAll();
		requests.requestMatchers("/web/todoWebService").denyAll();
		requests.requestMatchers("/web/updateReviewInfo").denyAll();

		requests.requestMatchers("*.vue").permitAll();
		requests.requestMatchers("/vue", "/sys/login/**", "/lib/**", "/assets/**").permitAll();
		requests.requestMatchers("/web/**").permitAll();
	}

	@Override
	public void configSecurityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, UserDetailsService userDetailsService) {
		http.addFilterBefore(new ThymeleafTemplateFilter(thymeleafTemplateSuffix), ChannelProcessingFilter.class);
		http.addFilterBefore(new VueFilter(), ChannelProcessingFilter.class);
		super.configSecurityFilterChain(http, authenticationManager, userDetailsService);
	}
}
