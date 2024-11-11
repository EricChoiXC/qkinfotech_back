package com.qkinfotech.core.mvc;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ctc.wstx.util.StringUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SimpleResult implements InitializingBean {

	@Autowired
	protected HttpServletRequest request;

	@Autowired
	@Getter
	protected HttpServletResponse response;

	private void output(JSONObject data) throws Exception {
		output(data, 200);
	}

	private void output(JSONObject data, int code) throws Exception {
		data.put("status", code);
		response.setStatus(code);

		String callback = request.getParameter("callback");
		if (StringUtils.hasText(callback)) {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/javascript");
			String script = data.toJSONString();
			String result = callback + "(" + script + ")";
			response.getWriter().print(result);
		} else {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");
			response.getWriter().print(data.toJSONString());
		}
	}

	public void ok() throws Exception {
		JSONObject result = new JSONObject();
		output(result);
	}

	public void from(JSONObject json) throws Exception {
		JSONObject result = new JSONObject();
		result.put("data", json);
		output(result);
	}

	public void from(JSONArray json) throws Exception {
		JSONObject result = new JSONObject();
		result.put("data", json);
		output(result);
	}

	public void from(Page<JSONObject> page) throws Exception {
		JSONObject result = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("size", page.getSize());
		data.put("total", page.getTotalElements());
		data.put("page", page.getNumber());
		data.put("list", page.getContent());
		result.put("data", data);
		output(result);
	}

	public void redirect(String url) throws Exception {
		redirect(url, null);
	}

	public void redirect(String url, Map<String, String> params) throws Exception {
		String location = url;
		if (params != null && params.size() > 0) {
			StringBuilder sb = new StringBuilder();

			for (Map.Entry<String, String> e : params.entrySet()) {
				String key = URLEncoder.encode(e.getKey(), "UTF_8");
				String value = URLEncoder.encode(e.getValue(), "UTF_8");
				sb.append("&" + key + "=" + value);
			}
			sb.setCharAt(0, '?');
			
			if(location.indexOf("?") > 0) {
				location = location.replaceFirst("\\?", sb.append('&').toString());
			} else 	if(location.indexOf("#") > 0) {
				location = location.replaceFirst("#", sb.append('#').toString());
			} else {
				location += sb.toString(); 
			}
		}
		response.sendRedirect(location);
	}

	String applicationHtml;
	Map<String, String> emptyParams = new HashMap<>();
	public void vue(String url) throws Exception {
		vue(url, null);
	}
	public void vue(String url, Object params) throws Exception {
		String request = (params == null)? "{}" : JSONObject.toJSONString(params);
		
		String output = org.apache.commons.lang3.StringUtils.replaceEach(applicationHtml, new String[] {
			"{request}", "{url}"
		}, new String[] {
			request, url	
		});
		
		response.setContentType("text/html");
		response.getWriter().print(output);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		applicationHtml = new String(this.getClass().getResourceAsStream("/application.html").readAllBytes(), Charset.defaultCharset());
	}



//	public void from(Throwable e) throws Exception{
//		JSONObject result = new JSONObject();
//		result.put("message", e.getMessage());
//		result.put("trace", ExceptionUtil.toString(e));
//		output(result, 500);
//	}

}
