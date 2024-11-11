package com.qkinfotech.app.demo;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.app.config.EkpConfig;
import com.qkinfotech.core.task.ITask;
import com.qkinfotech.core.task.Task;
import com.qkinfotech.core.task.TaskLogger;

//@Task(trigger="interval:5")
public class TestTask implements ITask{
	
	@Override
	public void execute(TaskLogger logger, JSONObject parameter) {
//		System.out.println("task begin:" + TestTask.class.getName());
		try {
			logger.write("hello world");
			Thread.sleep(1*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		System.out.println("task finished:" + TestTask.class.getName());
	}

}
