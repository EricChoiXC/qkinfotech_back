package com.qkinfotech.core.task;

public class TaskWaitException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	long time;
	
	public TaskWaitException(long time) {
		this.time = time;
	}
	
	public long getTime() {
		return time;
	}

}
