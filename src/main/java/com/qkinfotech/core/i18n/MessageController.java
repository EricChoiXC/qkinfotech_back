package com.qkinfotech.core.i18n;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
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


@Controller
@RequestMapping("/message")
public class MessageController {

		SimpleResult result;
		
		private HttpServletResponse response;

		private MessageSource messageSource;
		
		protected LocaleResolver localResolver;

		public MessageController(SimpleResult result, HttpServletResponse response, MessageSource messageSource, LocaleResolver localResolver) {
			super();
			this.result = result;
			this.response = response;
			this.messageSource = messageSource;
			this.localResolver = localResolver;
		}

		@RequestMapping("/load")
		@ResponseBody
		public void load() throws Exception {
			/* 获取message */
			
			JSONObject json = new JSONObject();
			json.put("aaa", "我的应答");
			result.from(json);
		}

		@RequestMapping("/download")
		@ResponseBody
		public void download() throws Exception {

			/*
			 * 将 DBMessage 中的 messages （已合并数据库）导出到文件 message.zip
			 * zip 中 为 messages_XX.properties 文件，按照对应目录存放
			 * 
			 * 难点: 如何根据 key 获取 对应 文件位置。
			 * 需要改造 DBMessageSource， 将 key 对应 的 文件相对路径记录下来
			 * 
			 * 所有 message_xx.properties 包含所有语言的不重复 key
			 * 
			 */
		}

		@RequestMapping("/export")
		@ResponseBody
		public void exports() throws Exception {
			// 特殊格式：行=key，列=语言
		}

		@RequestMapping("/import")
		@ResponseBody
		public void imports() throws Exception {
			// 特殊格式：行=key，列=语言
		}

}
