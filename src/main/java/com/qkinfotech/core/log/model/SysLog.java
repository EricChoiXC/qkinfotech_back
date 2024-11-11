package com.qkinfotech.core.log.model;

import java.util.Date;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "sys_log")
@Entity
@SimpleModel(url="sys/log")
public class SysLog extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "f_node_name", length = 32)
	private String fNodeName;

	@Column(name = "f_timestamp")
	private Date fTimestamp;

	@Column(name = "f_class_name", length = 256)
	private String fClassName;

	@Column(name = "f_method_name", length = 128)
	private String fMethodName;

	@Column(name = "f_line_no")
	private int fLineNo;

	@Column(name = "f_thread_name", length = 128)
	private String fThreadName;

	@Column(name = "f_level", length = 16)
	private String fLevel;

	@Column(name = "f_logger_name", length = 256)
	private String fLoggerName;

	@Column(name = "f_message", length = Integer.MAX_VALUE)
	@Lob
	private String fMessage;

	@Column(name = "f_exception", length = Integer.MAX_VALUE)
	@Lob
	private String fException;

	public static SysLog from(ILoggingEvent eventObject, boolean detail) {

		SysLog sysLog = new SysLog();

		sysLog.setfLoggerName(eventObject.getLoggerName());
		sysLog.setfMessage(eventObject.getFormattedMessage());
		sysLog.setfLevel(eventObject.getLevel().toString());
		sysLog.setfTimestamp(new Date(eventObject.getTimeStamp()));
		sysLog.setfThreadName(eventObject.getThreadName());
		if (detail) {
			StackTraceElement[] stes = eventObject.getCallerData();
			if (eventObject.hasCallerData()) {
				StackTraceElement ste = stes[0];
				sysLog.setfClassName(ste.getClassName());
				sysLog.setfMethodName(ste.getMethodName());
				sysLog.setfLineNo(ste.getLineNumber());
			}
		}
		if (eventObject.getThrowableProxy() != null) {
			sysLog.setfException(buildException(eventObject.getThrowableProxy()));
		}

		return sysLog;

	}

	private static String buildException(IThrowableProxy throwableProxy) {
		StringBuilder sb = new StringBuilder();

		while (throwableProxy != null) {
			sb.append(throwableProxy.getClassName()).append(": ").append(throwableProxy.getMessage()).append(System.lineSeparator());
			StackTraceElementProxy[] steps = throwableProxy.getStackTraceElementProxyArray();
			for (int i = 0; i < steps.length; ++i) {
				sb.append("\t").append(steps[i].toString()).append(System.lineSeparator());
			}
			throwableProxy = throwableProxy.getCause();
			if (throwableProxy != null) {
				sb.append("Caused by ");
			}
		}

		return sb.toString();
	}
}
