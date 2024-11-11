package com.qkinfotech.core.auth.token;

import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public abstract class AbstractToken {

	public abstract boolean expired();

	protected abstract String getUsername();

	public abstract AbstractToken renew();

	public abstract String getCookieName();

	public abstract String getRequestName();
	
	public abstract String getHeaderName();
	
	public static String getTokenString(HttpServletRequest request, AbstractToken token) {
		String tokenString = request.getParameter(token.getRequestName());
		if (tokenString != null) {
			return tokenString;
		}
		tokenString = request.getHeader(token.getHeaderName());
		if (tokenString != null) {
			return tokenString;
		}
		Cookie cookie = WebUtils.getCookie(request, token.getCookieName());
		if (cookie != null) {
			return cookie.getValue();
		}
		return null;
	}
}
