package com.qkinfotech.core.web.vue;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class VueFilter implements Filter {
 
	public VueFilter() {
		super();
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}
	
	@Override
	public void destroy() {

	}
	
	private VueProcessor processor = new VueProcessor();
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(request instanceof HttpServletRequest req && response instanceof HttpServletResponse res) {
			doFilter(req, res, chain);
			return;
		}
		chain.doFilter(request, response);
	}

	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(request.getRequestURI().endsWith(".vue")) {
			processor.vueJs(request, response);
			return;
		}
		if(request.getRequestURI().endsWith(".vue.css")) {
			processor.vueCss(request, response);
			return;
		}
		chain.doFilter(request, response);
	}
}