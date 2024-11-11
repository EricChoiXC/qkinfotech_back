package com.qkinfotech.core.log.appender;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.qkinfotech.core.log.model.SysLog;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SysLogAppenderFlusher implements Runnable {

	public SysLogAppenderFlusher(String driver, String url, String username, String password, LinkedBlockingQueue<SysLog> logs, String nodeName, int batchSize) {
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.logs = logs;
		this.nodeName = nodeName;
		this.batchSize = batchSize;
	}

	private static final String INSERT_SQL = "INSERT INTO sys_log (f_id, f_class_name, f_exception, f_level, f_line_no, f_logger_name, f_message, f_method_name, f_node_name, f_thread_name, f_timestamp) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

	private Connection connection = null;

	private LinkedBlockingQueue<SysLog> logs = null;

	private String driver = null;

	private String url = null;

	private String username = null;

	private String password = null;

	private int batchSize = 0;

	private String nodeName = null;

	private void connect() {
		if(connection != null) {
			try {
				if(connection.isClosed()) {
					connection = null;
				}
			} catch (Exception e) {
				connection = null;
			}
		}
		if (connection == null) {
			try {
				Class.forName(driver);
				connection = DriverManager.getConnection(url, username, password);
				connection.setAutoCommit(false);
			} catch (Exception e) {
				connection = null;
			}
		}
	}

	@Override
	public void run() {
		if (logs.size() > 0) {
			connect();
			List<SysLog> logs2write = new ArrayList<>();
			logs.drainTo(logs2write);
			try {
				batchInsert(connection, logs2write);
				connection.commit();
			} catch (Throwable e) {
				// logger.error("log commit error.", e);
				e.printStackTrace();
				try {
					connection.rollback();
				} catch (SQLException e1) {
					// logger.error("rollback error.", e);
				}
				try {
					connection.close();
				} catch (SQLException e1) {
					// logger.error("close error.", e);
				}
			} finally {
				
			}
		}
	}

	private void batchInsert(Connection conn, List<SysLog> logs) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(INSERT_SQL);
		int i;
		SysLog log;
		for (i = 1; i <= logs.size(); ++i) {
			log = logs.get(i - 1);

			stmt.setString(1, log.getfId());
			stmt.setString(2, log.getfClassName());
			stmt.setString(3, log.getfException());
			stmt.setString(4, log.getfLevel());
			stmt.setInt(5, log.getfLineNo());
			stmt.setString(6, log.getfLoggerName());
			stmt.setString(7, log.getfMessage());
			stmt.setString(8, log.getfMethodName());
			stmt.setString(9, nodeName);
			stmt.setString(10, log.getfThreadName());
			stmt.setTimestamp(11, new Timestamp(log.getfTimestamp().getTime()));

			stmt.addBatch();
			if (i % batchSize == 0) {
				stmt.executeBatch();
			}

		}
		if (i % batchSize != 0) {
			stmt.executeBatch();
		}
	}

}