package com.qkinfotech.core.app.log;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AppLog {
	
	@Autowired
	private List<AppLogAppender> appenders;

	@Value("${application.node-name:server}") 
	private String server;
	
	public void log(AppLogData data) {
		data.setup(server, new Date(), Thread.currentThread().getName());
		for(AppLogAppender appender : appenders) {
			try {
				appender.write(data);
			}catch(Throwable e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
}
