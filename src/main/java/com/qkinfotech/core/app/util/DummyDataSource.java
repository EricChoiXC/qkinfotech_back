package com.qkinfotech.core.app.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class DummyDataSource implements DataSource, Closeable {

	int maxPoolSize = 200;

	AtomicInteger[] status;

	DummyConnection[] connections;

	AtomicInteger index = new AtomicInteger(0);

	public DummyDataSource() {
		status = new AtomicInteger[maxPoolSize];
		connections = new DummyConnection[maxPoolSize];
		for (int i = 0; i < maxPoolSize; ++i) {
			status[i] = new AtomicInteger(0);
			connections[i] = null;
		}
	}

	public void release(int index) {
		status[index].set(0);
	}

	@Override
	public Connection getConnection() throws SQLException {
		int i = index.getAndIncrement();
		while (i >= maxPoolSize) {
			i -= maxPoolSize;
		}
		index.set(i);
		int e = i;
		while (true) {
			if (status[i].compareAndExchange(0, 1) == 0) {
				if (connections[i] == null) {
					connections[i] = new DummyConnection();
					connections[i].setup(this, i);
				}
				return connections[i];
			}
			++i;
			if (i >= maxPoolSize) {
				i -= maxPoolSize;
			}
			if (e == i) {
				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
//				try {
//					Thread.sleep(1);
//				} catch (InterruptedException e1) {
//					e1.printStackTrace();
//				}
			}
		}
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return getConnection(); // new DummyConnection();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public void close() throws IOException {
		System.out.println("close");
		for (int i = 0; i < maxPoolSize; ++i) {
			if (status[i].getAcquire() == 1) {
				System.out.println("unrelased:" + i);
				continue;
			}
			if (connections[i] != null) {
				try {
					connections[i].close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println("not initialized:" + i);
			}
		}
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getLoginTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

}
