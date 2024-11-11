package com.qkinfotech.core.log.appender;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.qkinfotech.core.log.model.SysLog;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class SysLogAppender extends AppenderBase<ILoggingEvent> {

	private static final String excludePackage = SysLogAppender.class.getPackage().getName() + ".";

	private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

	private LinkedBlockingQueue<SysLog> logs = new LinkedBlockingQueue<SysLog>();

	private String driver = null;
	
	private String url = null;
	
	private String username = null;
	
	private String password = null;

	private int batchSize = 200;

	private String nodeName = "default";

	private boolean detail = false;

	private ScheduledFuture<?> scheduled = null;
	
	private Connection connection = null;

	@Override
	protected void append(ILoggingEvent eventObject) {
		String loggerName = eventObject.getLoggerName();
		if (loggerName != null && loggerName.startsWith(excludePackage)) {
			return;
		}
		SysLog sysLog = SysLog.from(eventObject, detail);
		logs.add(sysLog);
	}

	@Override
	public void start() {
		super.start();
		if (scheduled != null) {
			scheduled.cancel(true);
			scheduled = null;
		}
		if(connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			connection = null;
		}
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(url, username, password);
			connection.setAutoCommit(false);
			scheduled = scheduledExecutorService.scheduleAtFixedRate(new SysLogAppenderFlusher(driver, url, username, password, logs, nodeName, batchSize), 1, 2, TimeUnit.SECONDS);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		super.stop();
		if (scheduled != null) {
			scheduled.cancel(true);
			scheduled = null;
		}
		if(connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			connection = null;
		}
	}

	public boolean isDetail() {
		return detail;
	}

	public void setBatchSize(int batchSize) {
		if (batchSize > 0 && batchSize <= 1000) {
			this.batchSize = batchSize;
		}
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public void setDetail(boolean detail) {
		this.detail = detail;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}


}
