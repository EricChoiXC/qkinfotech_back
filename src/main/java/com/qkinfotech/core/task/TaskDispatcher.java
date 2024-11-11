package com.qkinfotech.core.task;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.qkinfotech.core.task.model.TaskMain;
import com.qkinfotech.core.task.service.TaskDispatchService;

@Component
public class TaskDispatcher implements DisposableBean, ApplicationRunner {

	@Autowired
	private TaskDispatchService service;

	private Thread thread = null;

	private ThreadPoolTaskExecutor executor = null;

	private TaskDispatchThread runner = null;

	@Override
	public void destroy() throws Exception {
		if (thread != null) {
			thread.interrupt();
		}

		if (executor != null) {
			executor.shutdown();
		}
	}

	private static class TaskDispatchThread implements Runnable {

		private TaskDispatchService service;

		private ThreadPoolTaskExecutor executor;

		private TaskDispatcher dispatcher;

		public TaskDispatchThread(TaskDispatcher dispatcher, TaskDispatchService service, ThreadPoolTaskExecutor executor) {
			super();
			this.service = service;
			this.executor = executor;
			this.dispatcher = dispatcher;
		}

		@Override
		public void run() {

			while (!Thread.interrupted()) {
				if (executor.getQueueSize() < executor.getQueueCapacity()) {
					try {
						TaskMain task = service.take();
						executor.execute(new TaskRunner(dispatcher, task));
					} catch (TaskWaitException e) {
						sleep(e.getTime());
					}
				} else {
					sleep(60 * 1000l);
				}
			}
		}

		private void sleep(long time) {
			synchronized (this) {
				try {
					this.wait(time);
				} catch (InterruptedException e1) {
				}
			}
		}
	}

	public void dispatch() {
		if (runner != null) {
			synchronized (runner) {
				runner.notify();
			}
		}
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// TODO 根据配置 创建 线程池
		executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(20);
		executor.setQueueCapacity(30);
		executor.setThreadNamePrefix("Task-Execution-Thread-");
		executor.initialize();

		// 重置本节点的运行状态
		service.reset();

		// 同步系统定时任务
		service.sync();

		// 启动分发线程
		runner = new TaskDispatchThread(this, service, executor);
		thread = new Thread(runner);
		thread.start();

	}

}
