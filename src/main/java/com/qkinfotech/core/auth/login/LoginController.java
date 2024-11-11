package com.qkinfotech.core.auth.login;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qkinfotech.core.mvc.SimpleResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

	@Autowired
	HttpServletRequest request;

	@Autowired
	SimpleResult result;
	
	@GetMapping("/login")
	@ResponseBody
	public void login() throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("errmsg", getLoginErrorMessage());
		result.vue(request.getRequestURL().toString().replace(request.getRequestURI(), "") + "/login", params);
	}

	@RequestMapping("/")
	@ResponseBody
	public void home() throws Exception {
		result.vue("/sys/portal/main.vue");
	}
	
	private String getLoginErrorMessage() {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return "";
		}
		if(session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) == null) {
			return "";
		}
		if (session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) instanceof AuthenticationException) {
			return "Invalid credentials";
		}
		if (session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) instanceof Throwable exception) {
			if (!StringUtils.hasText(exception.getMessage())) {
				return "Invalid credentials";
			}
		}
		return "";
	}
}
