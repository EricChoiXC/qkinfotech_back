package com.qkinfotech.core.task.event;

import org.springframework.context.ApplicationEvent;

import com.qkinfotech.core.task.model.TaskMain;

public class TaskFailEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	Throwable e;
	
	public TaskFailEvent(TaskMain source, Throwable e) {
		super(source);
		this.e = e;
		
	}
	
	public TaskMain getTask() {
		return (TaskMain)getSource();
	}

	public Throwable getException() {
		return e;
	}

}
