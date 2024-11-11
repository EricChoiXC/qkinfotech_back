package com.qkinfotech.core.task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.SimpleTriggerContext;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.task.datatype.TaskTrigger;
import com.qkinfotech.core.task.model.TaskMain;
import com.qkinfotech.util.SpringUtil;

import lombok.Builder;

@Builder
public class TaskBuilder {
	
	private String name;

	private String trigger;

	private String group;
	
	private String beanName;
	
	private JSONObject parameter;
	
	@Builder.Default
	private int maxNumOfRetries = 5;

	@Builder.Default
	private boolean autoRetry = false;
	
	@Builder.Default
	private int limit = 0;
	
	public TaskMain add() throws ParseException {
		
		if(!SpringUtil.getContext().containsBean(beanName)) {
			throw new IllegalArgumentException("Task bean name:" + beanName);
		}
		if(!(SpringUtil.getContext().getBean(beanName) instanceof ITask)) {
			throw new IllegalArgumentException("Not instanceof ITask. bean name:" + beanName);
		}
		
		TaskMain taskMain = new TaskMain();
		
		taskMain.setfName(name);
		taskMain.setfAutoRetry(autoRetry);
		taskMain.setfCreateTime(new Date());
		taskMain.setfMaxNumOfRetries(maxNumOfRetries);
		taskMain.setfTaskBeanName(beanName);
		taskMain.setfTaskTrigger(new TaskTrigger(trigger));
		taskMain.setfGroup(group);
		taskMain.setfLimit(limit);
		taskMain.setfTaskParameter(parameter);
		
		if(trigger == null) {
			taskMain.setfScheduledTime(new Date());
		} else if(trigger.startsWith("onetime:")){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			taskMain.setfScheduledTime(sdf.parse(trigger.substring(8)));
		} else {
			TriggerContext triggerContext = new SimpleTriggerContext(Instant.now(),Instant.now(),Instant.now());
			taskMain.setfScheduledTime(taskMain.getfTaskTrigger().next(triggerContext));
		}
		
		SimpleService<TaskMain> service = (SimpleService<TaskMain>)SpringUtil.getContext().getBean("taskMainService");
		
		service.save(taskMain);
		
		TaskDispatcher dispatcher = SpringUtil.getContext().getBean(TaskDispatcher.class);
		
		dispatcher.dispatch();
		
		return taskMain;
	}
}
