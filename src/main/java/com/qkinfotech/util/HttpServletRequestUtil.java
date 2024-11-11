package com.qkinfotech.util;

import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

public class HttpServletRequestUtil {

	public static boolean isAjax(HttpServletRequest request) {
		String header;
		header = request.getHeader("x-requested-with");
		if (StringUtils.hasText(header)) {
			if (header.contains("XMLHttpRequest")) {
				return true;
			}
		}
		return false;
	}

	public static String getRemoteIP(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (!StringUtils.hasText(ip)) {
			return request.getRemoteAddr();
		}
		return ip;
	}

}
