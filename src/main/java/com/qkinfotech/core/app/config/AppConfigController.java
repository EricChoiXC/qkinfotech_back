package com.qkinfotech.core.app.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.app.model.SysConfig;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.IEntityExtension;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.mvc.util.Json2Bean;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@CrossOrigin("http://localhost:3000")
public class AppConfigController<T extends BaseEntity> {
	
	@Autowired
	protected SimpleService<SysConfig> sysConfigService;

	@Autowired
	protected Bean2Json bean2json;

	@Autowired
	protected Json2Bean json2bean;

	@RequestMapping("/sysConfig/init")
	@ResponseBody
	public String init(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String bean = request.getParameter("beanName");
		JSONObject queryJson = new JSONObject();
		JSONObject equal = new JSONObject();
		equal.put("fModelName", bean);
		queryJson.put("equals", equal);
		
		List<SysConfig> configs = sysConfigService.findAll(JSONQuerySpecification.getSpecification(queryJson));
		JSONObject result = new JSONObject();
		for (SysConfig config : configs) {
			result.put(config.getfPropertyName(), config.getfPropertyValue());
		}
		return JSON.toJSONString(result);
	}

	@RequestMapping("/sysConfig/commit")
	@ResponseBody
	public String commit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject json = getPostData(request);
		String beanName = json.getString("beanName");
		Map map = json.getJSONObject("values");
		JSONObject queryJson = new JSONObject();
		JSONObject equal = new JSONObject();
		equal.put("fModelName", beanName);
		queryJson.put("equals", equal);

		List<SysConfig> configs = sysConfigService.findAll(JSONQuerySpecification.getSpecification(queryJson));
		Map<String, SysConfig> configsMap = new HashMap<String, SysConfig>();
		for (SysConfig config : configs) {
			configsMap.put(config.getfPropertyName(), config);
		}
		
		SysConfig config;
		for (Object key : map.keySet()) {
			if (key instanceof String) {
				if (configsMap.containsKey(key)) {
					config = configsMap.get(key);
				} else {
					config = new SysConfig();
					config.setfModelName(beanName);
					config.setfPropertyName((String) key);
				}
				config.setfPropertyValue((String) map.get(key));
				sysConfigService.save(config);
			}
		}
		return "";
	}
	
	private JSONObject getPostData(HttpServletRequest request) {
		JSONObject data = new JSONObject();
		try {
			InputStream in = request.getInputStream();
			byte[] b = FileUtil.readAsByteArray(in);
			String enc = request.getCharacterEncoding();
			if (!StringUtils.hasText(enc)) {
				enc = "UTF-8";
			}
			String txt = new String(b, enc);
			if (!StringUtils.hasText(txt)) {
				return new JSONObject();
			}
			data = JSONObject.parseObject(txt);

		} catch (IOException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		return data;
	}
}
