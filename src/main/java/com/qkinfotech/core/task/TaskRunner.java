package com.qkinfotech.core.task;

import java.time.Instant;

import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.util.StringUtils;

import com.qkinfotech.core.task.event.TaskFailEvent;
import com.qkinfotech.core.task.event.TaskFinishEvent;
import com.qkinfotech.core.task.event.TaskStartEvent;
import com.qkinfotech.core.task.event.TaskSuccessEvent;
import com.qkinfotech.core.task.model.TaskMain;
import com.qkinfotech.core.task.service.TaskDispatchService;
import com.qkinfotech.util.SpringUtil;

public class TaskRunner implements Runnable {
	
	private TaskMain task;
	
	private TaskDispatcher dispatcher;

	public TaskRunner(TaskDispatcher dispatcher, TaskMain task) {
		this.task = task;
		this.dispatcher = dispatcher;
	}


	@Override
	public void run() {
		
		Instant lastScheduledExecution = Instant.now();
		Instant lastActualExecution = lastScheduledExecution.plusMillis(0);
		
		TaskDispatchService service = SpringUtil.getContext().getBean(TaskDispatchService.class);
		
		TaskLogger logger = new TaskLogger(task.getfId());

		SpringUtil.getContext().publishEvent(new TaskStartEvent(this.task));

		String beanName = task.getfTaskBeanName();
		if(StringUtils.hasText(beanName)) {
			try {
				ITask runnable = (ITask)SpringUtil.getContext().getBean(beanName);
				runnable.execute(logger, task.getfTaskParameter());
				logger.success();
				SpringUtil.getContext().publishEvent(new TaskSuccessEvent(this.task));
			}catch(Throwable e) {
				SpringUtil.getContext().publishEvent(new TaskFailEvent(this.task, e));
				logger.fail(e);
			}
		} else {
			Exception e = new RuntimeException("TaskBeanName not found: [" + beanName + "]"); 
			logger.fail(e);
			SpringUtil.getContext().publishEvent(new TaskFailEvent(this.task, e));
		}

		SpringUtil.getContext().publishEvent(new TaskFinishEvent(this.task));

		Instant lastCompletion = Instant.now();
		TriggerContext triggerContext = new SimpleTriggerContext(lastScheduledExecution, lastActualExecution, lastCompletion);
		service.finish(task.getfId(), triggerContext);
		
		(new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				dispatcher.dispatch();
			}
			
		})).start();
	}

}
