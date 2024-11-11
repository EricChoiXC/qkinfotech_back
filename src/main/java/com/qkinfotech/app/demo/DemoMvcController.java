package com.qkinfotech.app.demo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;

import com.alibaba.fastjson2.JSONObject;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.qkinfotech.core.mvc.SimpleResult;
//import com.qkinfotech.core.redis.RedisCache;

import jakarta.servlet.http.HttpServletResponse;

public class DemoMvcController {

	SimpleResult result;
	
	private HttpServletResponse response;

	//private RedisCache redisCache;
	
	private MessageSource messageSource;
	
	protected LocaleResolver localResolver;

	public DemoMvcController(SimpleResult result, HttpServletResponse response, /*RedisCache redisCache, */MessageSource messageSource, LocaleResolver localResolver) {
		super();
		this.result = result;
		this.response = response;
	//	this.redisCache = redisCache;
		this.messageSource = messageSource;
		this.localResolver = localResolver;
	}

	@GetMapping("/hi")
	//@Cacheable(cacheNames="default")
	//@Cached(expire = 3600, cacheType=CacheType.BOTH)
	@ResponseBody
	public void hi() throws Exception {
		System.out.println("TestController.hi");
		//throw new RuntimeException("oo");
		//return "我的应答信息";
		
		JSONObject json = new JSONObject();
		json.put("aaa", "我的应答");
		result.from(json);
	}

}
