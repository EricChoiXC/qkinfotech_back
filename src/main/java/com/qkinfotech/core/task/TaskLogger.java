package com.qkinfotech.core.task;

import java.text.MessageFormat;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.task.model.TaskLog;
import com.qkinfotech.core.task.model.TaskLogDetail;
import com.qkinfotech.core.task.model.TaskStatus;
import com.qkinfotech.core.task.service.TaskDispatchService;
import com.qkinfotech.util.ExceptionUtil;
import com.qkinfotech.util.IDGenerate;
import com.qkinfotech.util.SpringUtil;

public class TaskLogger {

	private String fTaskLogId;

	private TaskDispatchService taskDispatchService;

	private SimpleService<TaskLog> taskLogService;

	private SimpleService<TaskLogDetail> taskLogDetailService;

	private TaskLog taskLog;

	private String taskId;

	public TaskLogger(String taskId) {

		this.taskId = taskId;

		taskLogService = (SimpleService<TaskLog>) SpringUtil.getContext().getBean("taskLogService");

		taskLogDetailService = (SimpleService<TaskLogDetail>) SpringUtil.getContext().getBean("taskLogDetailService");

		taskDispatchService = SpringUtil.getContext().getBean(TaskDispatchService.class);

		taskLog = new TaskLog();
		taskLog.getfId();
		taskLog.setfTaskId(taskId);
		taskLog.setfStartTime(new Date());
		taskLog.setfExecutionNode(SpringUtil.getNode());
		taskLog.setfStatus(TaskStatus.RUNNING);

		taskLogService.save(taskLog);
		
		fTaskLogId = taskLog.getfId();

		taskDispatchService.start(taskId);

	}

	public void write(String msg) {
		if (StringUtils.hasText(msg)) {
			TaskLogDetail taskLogDetail = new TaskLogDetail();
			taskLogDetail.setfTaskLogId(fTaskLogId);
			taskLogDetail.setfTime(new Date());
			taskLogDetail.setfContent(msg);

			taskLogDetailService.save(taskLogDetail);
		}
	}

	public void write(String pattern, Object... args) {
		if (StringUtils.hasText(pattern)) {
			TaskLogDetail taskLogDetail = new TaskLogDetail();
			taskLogDetail.setfTaskLogId(fTaskLogId);
			taskLogDetail.setfTime(new Date());
			taskLogDetail.setfContent(MessageFormat.format(pattern, args));

			taskLogDetailService.save(taskLogDetail);
		}
	}

	public void write(Throwable e) {
		if (e != null) {
			write(ExceptionUtil.toString(e));
		}
	}

	public void write(String msg, Throwable e) {
		if (e != null && msg != null) {
			write(msg + "\r\n" + ExceptionUtil.toString(e));
		} else if (e != null) {
			write(ExceptionUtil.toString(e));
		} else if (msg != null) {
			write(msg);
		}
	}

	void success() {
		taskLog.setfEndTime(new Date());
		taskLog.setfStatus(TaskStatus.SUCCESS);
		taskLogService.save(taskLog);
		taskDispatchService.success(taskId);
	}

	void fail(Throwable e) {
		write(e);
		taskLog.setfEndTime(new Date());
		taskLog.setfStatus(TaskStatus.ERROR);
		taskLogService.save(taskLog);
		taskDispatchService.fail(taskId);
	}

}
