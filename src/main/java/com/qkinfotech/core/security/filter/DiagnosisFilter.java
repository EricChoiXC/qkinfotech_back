package com.qkinfotech.core.security.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DiagnosisFilter extends OncePerRequestFilter {
 
	public DiagnosisFilter() {
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if(request.getAttribute("diag-start-time") == null) {
			request.setAttribute("diag-start-time", System.currentTimeMillis());
		}
		filterChain.doFilter(request, response);
	}

}