package com.qkinfotech.core.web.thymeleaf;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ThymeleafTemplateFilter implements Filter {
 
	public ThymeleafTemplateFilter(String suffix) {
		super();
		this.suffix = suffix;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}
	
	@Override
	public void destroy() {

	}
	
	
	private String suffix;
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(StringUtils.hasText(suffix)) {
			if(request instanceof HttpServletRequest req) {
				if(req.getRequestURI().endsWith(suffix)) {
					((HttpServletResponse)response).sendError(403);
					throw  new AccessDeniedException("proetected resource can not be accessed");
				}
			}
		}
		chain.doFilter(request, response);
	}
 
}