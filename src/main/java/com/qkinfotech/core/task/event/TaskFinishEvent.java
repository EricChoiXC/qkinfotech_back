package com.qkinfotech.core.task.event;

import org.springframework.context.ApplicationEvent;

import com.qkinfotech.core.task.model.TaskMain;

public class TaskFinishEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public TaskFinishEvent(TaskMain source) {
		super(source);
	}
	
	public TaskMain getTask() {
		return (TaskMain)getSource();
	}

}