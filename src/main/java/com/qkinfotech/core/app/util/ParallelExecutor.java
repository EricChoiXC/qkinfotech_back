package com.qkinfotech.core.app.util;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ParallelExecutor {

	public static void start(Runnable... runnables) throws InterruptedException {
		start(Math.min(200, runnables.length) , runnables);
	}
	
	public static void start(int poolSisze, Runnable... runnables) throws InterruptedException {

		int count = runnables.length;

		if (count == 0) {
			return;
		}

		ExecutorService executorService = Executors.newFixedThreadPool(poolSisze);
		
		CountDownLatch latch = new CountDownLatch(count);

		for (Runnable runnable : runnables) {
			executorService.submit(new Runnable() {
				public void run() {
					try {
						runnable.run();
					} catch (Throwable e) {
						e.printStackTrace();
					} finally {
						latch.countDown();
					}
				}
			});
		}
		
		latch.await();
		executorService.shutdown();
		
		while(!executorService.isTerminated()) {}
	}

}
