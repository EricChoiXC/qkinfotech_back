package com.qkinfotech.core.task.controller;

import java.io.IOException;
import java.io.InputStream;

import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.task.TaskBuilder;
import com.qkinfotech.core.task.TaskDispatcher;
import com.qkinfotech.core.task.model.TaskMain;
import com.qkinfotech.util.SpringUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@CrossOrigin("http://localhost:3000")
public class TaskController {
	
	@Autowired
	protected SimpleService<TaskMain> taskMainService;

	@Autowired
	protected Bean2Json bean2json;

	@Autowired
	protected Json2Bean json2bean;
	
	
	@RequestMapping("/taskAction/handle")
	@ResponseBody
	public String handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		System.out.println("/taskAction/handle");

		JSONObject body = getPostData(request);
		if (body == null) {
			throw new IllegalArgumentException("illegal request body");
		}

		TaskMain target = json2bean.toBean(body, taskMainService.getEntityClass());

		taskMainService.save(target);

		
		TaskDispatcher dispatcher = SpringUtil.getContext().getBean(TaskDispatcher.class);
		
		dispatcher.dispatch();
		
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
