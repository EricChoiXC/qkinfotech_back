package com.qkinfotech.core.auth.token;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.util.DESUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*@Component*/
public class SimpleToken implements IToken {
	
	public static final String SESSION_NAME = "SIMPLE_AUTH_TOKEN";

	public static final String REQUEST_PARAMETER_NAME = "sso_token";

	public static final String HEADER_NAME = "x-sso-token";

	public static final String COOKIE_NAME = "sso-token";

	protected long expire = 0;

	private String getTokenFromCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for(int i =0; i < cookies.length; ++ i) {
				if(cookies[i].getName().equals(COOKIE_NAME)) {
					return cookies[i].getValue();
				}
			}
		}
		return null;
	}
	
	@Override
	public boolean validate(HttpServletRequest request) {
		String tokenInSession = (String)request.getSession().getAttribute(SESSION_NAME);
		String tokenInRequest = getTokenString(request);
		if(tokenInSession == null && tokenInRequest == null) {
			return true;
		}
		if(tokenInSession != null && tokenInRequest == null) {
			request.getSession().removeAttribute(SESSION_NAME);
			return true;
		}
		if(tokenInSession == null && tokenInRequest != null) {
			return false;
		}
		return tokenInSession.equals(tokenInRequest);
	}

	@Override
	public String getTokenString(HttpServletRequest request) {
		String token = request.getParameter(REQUEST_PARAMETER_NAME);
		if(token == null) {
			token = request.getHeader(HEADER_NAME);
		}
		if(token == null) {
			token = getTokenFromCookie(request);
		}
		return token;
	}

	@Override
	public void setTokenString(HttpServletRequest request, HttpServletResponse response, String tokenString) {
		request.getSession().setAttribute(SESSION_NAME, tokenString);
		String t = getTokenFromCookie(request);
		if(t == null || !t.equals(tokenString)) {
			response.addCookie(new Cookie(COOKIE_NAME, tokenString));
		}
	}

	@Override
	public String generateTokenString(String username, long expire) {
		if (username == null) {
			return null;
		}
		JSONObject o = new JSONObject();
		o.put("username", username);
		o.put("expire", expire);
		o.put("sault", Math.random());
		return DESUtil.encrypt(o.toString());
	}

	@Override
	public String getUsername(String tokenString) {
		try {
			JSONObject o = JSON.parseObject(DESUtil.decrypt(tokenString));
			String username = o.getString("username");
			long expire = o.getLongValue("expire", 0);
			if(expire == 0 || expire > System.currentTimeMillis()) {
				return username;
			} else {
				return null;
			}
		}catch(Exception e) {
			throw new BadCredentialsException("Invalid token", e);
		}
	}

}
