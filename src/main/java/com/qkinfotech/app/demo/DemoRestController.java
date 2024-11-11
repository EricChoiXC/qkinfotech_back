package com.qkinfotech.app.demo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.MessageSource;
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

//@RestController
public class DemoRestController {

	SimpleResult result;
	
	private HttpServletResponse response;

	//private RedisCache redisCache;
	
	private MessageSource messageSource;
	
	protected LocaleResolver localResolver;

	public DemoRestController(SimpleResult result, HttpServletResponse response, /*RedisCache redisCache, */MessageSource messageSource, LocaleResolver localResolver) {
		super();
		this.result = result;
		this.response = response;
	//	this.redisCache = redisCache;
		this.messageSource = messageSource;
		this.localResolver = localResolver;
	}

	@RequestMapping("/hello")
	public void hello() throws Exception {
		System.out.println("hello");
		result.ok();
	}

//	@GetMapping("/hi")
//	//@Cacheable(cacheNames="default")
//	@Cached(expire = 3600, cacheType=CacheType.BOTH)
//	public String hi() {
//		System.out.println("TestController.hi");
//		//throw new RuntimeException("oo");
//		return "我的应答信息";
//	}

	@GetMapping("/hoo")
	public void hoo() {
		response.setStatus(403);
		// return "hoo";
	}

	@RequestMapping("/vue")
	@ResponseBody
	public void vue() throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("name", "john smith");
		result.vue("/sys/login/login2.vue", params);
	}

}
