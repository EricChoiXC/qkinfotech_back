package com.qkinfotech.core.auth.token;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.http.Cookie;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("*");
	
	private Collection<IToken> tokens;
	
	private UserDetailsService userDetailsService;

	public TokenAuthenticationFilter(Collection<IToken> tokens, UserDetailsService userDetailsService) {
		super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
		setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login?error"));
		setSecurityContextRepository(new HttpSessionSecurityContextRepository());
		setSessionAuthenticationStrategy(new ChangeSessionIdAuthenticationStrategy());
		
		this.securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
		this.userDetailsService = userDetailsService; 
		this.tokens = tokens;
	}
	
	public TokenAuthenticationFilter(Collection<IToken> tokens,UserDetailsService userDetailsService, AuthenticationManager authenticationManager) {
		super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
		setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login?error"));
		setSecurityContextRepository(new HttpSessionSecurityContextRepository());
		setSessionAuthenticationStrategy(new ChangeSessionIdAuthenticationStrategy());
		this.userDetailsService = userDetailsService; 
		this.tokens = tokens;
	}

	
	@Override
	protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
		for(IToken token : tokens) {
			if(!token.validate(request)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)throws AuthenticationException {
		for(IToken token : tokens) {
			String tokenString = token.getTokenString(request);
			if(tokenString != null) {
				String username = token.getUsername(tokenString);

				if(username == null) {
					response.setStatus(603);
					NonceExpiredException e = new NonceExpiredException("Token expired");
					e.printStackTrace();
					return null;
					//throw new NonceExpiredException("Token expired");
				}
				
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);
				
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());

				logger.info("attempt success");
				token.setTokenString(request, response, tokenString);

				logger.info("do attemt");
				/*Cookie cookie = new Cookie("username", username);
				cookie.setPath("/");
				response.addCookie(cookie);*/

				return authentication;
			}
		}
		throw new BadCredentialsException("Unkonw token");
	}


	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
		super.unsuccessfulAuthentication(request, response, failed);
	}
	
	private SecurityContextHolderStrategy securityContextHolderStrategy;
	
	public void setSecurityContextHolderStrategy(SecurityContextHolderStrategy securityContextHolderStrategy) {
		Assert.notNull(securityContextHolderStrategy, "securityContextHolderStrategy cannot be null");
		this.securityContextHolderStrategy = securityContextHolderStrategy;
		super.setSecurityContextHolderStrategy(securityContextHolderStrategy);
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
		SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
		context.setAuthentication(authResult);
		SecurityContextHolder.setContext(context);
		request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
		chain.doFilter(request, response);
	}

}
