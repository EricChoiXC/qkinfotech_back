package com.qkinfotech.core.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.qkinfotech.core.security.filter.DiagnosisFilter;
import com.qkinfotech.core.security.filter.DiagnosisHeaderWriter;

import jakarta.servlet.Filter;

@Configuration
@EnableWebSecurity(debug=false)
public class WebSecurityConfig {
	
	private static String[] STATIC_RESOURCE_PATTERN = {
		"*.html", "*.css", "*.js",
		"*.ico", "*.png", "*.gif", "*.jpg", 
		"*.ttf", "*.fon"
	};
	
	@Bean
	public SecurityFilterChain configure(ApplicationContext context, List<IModuleSecurity> moduleSecurityConfig, HttpSecurity http, AuthenticationManager authenticationManager, UserDetailsService userDetailsService) throws Exception {
		http.authorizeHttpRequests((requests) -> {
			requests.requestMatchers(STATIC_RESOURCE_PATTERN).permitAll();
			moduleSecurityConfig.stream().forEach(o -> o.configAuthorization(requests));
			//context.publishEvent(new HttpSecurityConfigEvent(requests));
			requests.anyRequest().authenticated();
		});

		/*http.formLogin(f -> {
			f.loginPage("/login");
			//f.passwordParameter("password");
			//f.usernameParameter("username");
			//f.failureHandler(simpleUrlAuthenticationFailureHandler());
			//f.failureUrl("/login");
			//f.failureForwardUrl("/login");
			f.loginProcessingUrl("/login");
			f.successForwardUrl("/");
			//f.authenticationDetailsSource(null);
		});*/

		http.formLogin(f -> f.disable());

		http.csrf(csrf -> csrf.disable());

		http.cors(cors-> cors.configurationSource(corsConfigurationSource()));	

		moduleSecurityConfig.stream().forEach(o -> o.configSecurityFilterChain(http, authenticationManager, userDetailsService));
	
		// 启用 session
		http.sessionManagement(session -> {
			session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
			session.sessionAuthenticationErrorUrl("/login?error");
		});
		
		http.addFilterAfter(new DiagnosisFilter(), HeaderWriterFilter.class);
		
		http.headers(header -> {
			header.addHeaderWriter(diagnosisHeaderWriter());
		}); 
		
		
//		http.exceptionHandling(ex -> {
//			ex.accessDeniedHandler(new CustomAccessDeniedHandler());
//		});


		//http.addFilterAt(customAuthenticationFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Value("${application.node-name:server}") 
	private String nodeName;

	private HeaderWriter diagnosisHeaderWriter() {
		return new DiagnosisHeaderWriter(nodeName);
	}
	

	public AuthenticationFailureHandler simpleUrlAuthenticationFailureHandler() {
		return new SimpleUrlAuthenticationFailureHandler("/login?error");
	}

	
	public Filter customAuthenticationFilter(AuthenticationManager authenticationManager) {
		CustomAuthenticationFilter filter = new CustomAuthenticationFilter(authenticationManager);
		filter.setAuthenticationFailureHandler(simpleUrlAuthenticationFailureHandler());
		filter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());
		filter.setSessionAuthenticationStrategy(new ChangeSessionIdAuthenticationStrategy());
		return filter;
		
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.setAllowedOrigins(Arrays.asList("*"));
		corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST"));
		corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfiguration);
		return source;
	}
}
