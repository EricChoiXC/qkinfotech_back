package com.qkinfotech.core.mvc;

import java.io.IOException;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.util.ExceptionUtil;
import com.qkinfotech.util.HttpServletRequestUtil;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class ErrorControler implements org.springframework.boot.web.servlet.error.ErrorController {

	protected HttpServletRequest request;

	protected HttpServletResponse response;

	public ErrorControler(HttpServletRequest request, HttpServletResponse response) {
		super();
		this.request = request;
		this.response = response;
	}

	@RequestMapping("/error")
	@ResponseBody
	public void error() throws IOException, ServletException {

		Throwable ex = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
		
		if(ex != null) {
			if (ex instanceof NoResourceFoundException e) {
				response.setStatus(404);
			} else if (ex instanceof AccessDeniedException e) {
				response.setStatus(403);
			} else {
				response.setStatus(500);
			}
		}
			
		if (HttpServletRequestUtil.isAjax(request)) {
			JSONObject data = new JSONObject();
			data.put("status", response.getStatus());
			if(ex != null) {
				data.put("message", ex.getMessage());
				data.put("trace", ExceptionUtil.toString(ex));
			}
			response.setContentType("application/json");
			response.getWriter().print(data.toJSONString());
		} else {
			if (response.getStatus() == 401) {
				request.getRequestDispatcher("/login").forward(request, response);
			} else {
				if(ex != null) {
					response.getWriter().print(ExceptionUtil.toString(ex));
					response.flushBuffer();
				}
			}
		}
	}
}
