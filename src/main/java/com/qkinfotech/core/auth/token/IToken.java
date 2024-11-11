package com.qkinfotech.core.auth.token;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface IToken {

	String getUsername(String tokenString);

	boolean validate(HttpServletRequest request);

	String getTokenString(HttpServletRequest request);

	void setTokenString(HttpServletRequest request, HttpServletResponse response, String tokenString);

	String generateTokenString(String account, long expire);

}
