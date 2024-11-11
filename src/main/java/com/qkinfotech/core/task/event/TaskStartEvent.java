package com.qkinfotech.core.task.event;

import org.springframework.context.ApplicationEvent;

import com.qkinfotech.core.task.model.TaskMain;

public class TaskStartEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public TaskStartEvent(TaskMain source) {
		super(source);
	}
	
	public TaskMain getTask() {
		return (TaskMain)getSource();
	}

}
