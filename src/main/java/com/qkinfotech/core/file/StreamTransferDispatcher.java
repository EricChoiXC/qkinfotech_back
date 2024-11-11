package com.qkinfotech.core.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StreamTransferDispatcher {

	public static int TRANSFER_STATUS_NONE = 0;
	public static int TRANSFER_STATUS_READ = 1;
	public static int TRANSFER_STATUS_WROTE = 2;
	public static int TRANSFER_STATUS_WRITING = 3;

	private static LinkedBlockingQueue<byte[]> bufferQueue;

	static {
		bufferQueue = new LinkedBlockingQueue<byte[]>();
		for (int i = 0; i < 20; ++i) {
			bufferQueue.add(new byte[409600]);
		}
	}

	private static LinkedBlockingQueue<StreamTransferTask> taskQueue = new LinkedBlockingQueue<StreamTransferTask>();

	private static Hashtable<InputStream, LinkedBlockingQueue<StreamTransferTask>> runningQueue = new Hashtable<InputStream, LinkedBlockingQueue<StreamTransferTask>>();

	private static Hashtable<InputStream, CompletableFuture<Void>> futureQueue = new Hashtable<InputStream, CompletableFuture<Void>>();

	public synchronized CompletableFuture<Void> addTask(InputStream in, OutputStream out, long pos, long totalSize) {
		CompletableFuture<Void> future = futureQueue.get(in);
		if (future == null) {
			future = new CompletableFuture<Void>();
			futureQueue.put(in, future);
		}
		StreamTransferTask task = new StreamTransferTask(in, out, pos, totalSize);
		if (!taskQueue.offer(task)) {
			throw new RuntimeException("too many tasks");
		}
		dispatch();
		return future;
	}

	private synchronized LinkedBlockingQueue<StreamTransferTask> getTaskQueue(InputStream inputStream) {

		LinkedBlockingQueue<StreamTransferTask> queue = runningQueue.get(inputStream);
		if (queue == null) {
			queue = new LinkedBlockingQueue<StreamTransferTask>();
			runningQueue.put(inputStream, queue);
		}
		return queue;
	}

	synchronized void dispatch() {
		byte[] buffer = bufferQueue.poll();
		if (buffer == null) {
			return;
		}
		StreamTransferTask task = taskQueue.poll();
		if (task == null) {
			bufferQueue.offer(buffer);
			return;
		}
		task.setDispatcher(this);
		task.setBuffer(buffer);
		LinkedBlockingQueue<StreamTransferTask> rq = getTaskQueue(task.getInputStream());
		rq.offer(task);
		task.read();
	}

	synchronized void readComplete(InputStream inputStream) {
		LinkedBlockingQueue<StreamTransferTask> rq = getTaskQueue(inputStream);
		if (rq == null) {
			throw new RuntimeException("illegal queue status");
		}
		if (rq.size() == 0) {
			throw new RuntimeException("illegal transfer status");
		}
		StreamTransferTask task = rq.peek();
		if (task.getStatus() == TRANSFER_STATUS_READ) {
			task.setStatus(TRANSFER_STATUS_WRITING);
			task.write();
		}
	}

	synchronized void writeComplete(InputStream inputStream, byte[] buffer) {
		bufferQueue.add(buffer);
		dispatch();
		LinkedBlockingQueue<StreamTransferTask> rq = getTaskQueue(inputStream);
		if (rq == null) {
			throw new RuntimeException("illegal queue status");
		}
		StreamTransferTask task = rq.peek();
		if (task.getStatus() != TRANSFER_STATUS_WROTE) {
			throw new RuntimeException("illegal transfer status");
		}
		rq.poll();
		task = rq.peek();
		if (task != null) {
			if (task.getStatus() == TRANSFER_STATUS_READ) {
				task.setStatus(TRANSFER_STATUS_WRITING);
				task.write();
			}
		}
	}
	
	synchronized void complete(InputStream inputStream) {
		LinkedBlockingQueue<StreamTransferTask> rq = runningQueue.remove(inputStream);
		if (rq == null) {
			throw new RuntimeException("illegal queue status");
		}
		StreamTransferTask task = rq.poll();
		while (task != null) {
			bufferQueue.add(task.getBuffer());
			task = rq.poll();
		}
		dispatch();
		CompletableFuture<Void> future = futureQueue.remove(inputStream);
		future.complete(null);

	}

	synchronized void error(InputStream inputStream, Throwable ex) {
		LinkedBlockingQueue<StreamTransferTask> rq = runningQueue.remove(inputStream);
		if (rq == null) {
			throw new RuntimeException("illegal queue status");
		}
		StreamTransferTask task = rq.poll();
		while (task != null) {
			bufferQueue.add(task.getBuffer());
			task = rq.poll();
		}
		dispatch();
		CompletableFuture<Void> future = futureQueue.remove(inputStream);
		future.completeExceptionally(ex);
	}

	public static Thread download(StreamTransferDispatcher std, int i) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					File file = new File("e:/temp/Landray(2).log");
					InputStream in = new FileInputStream(file);
					OutputStream out = new FileOutputStream("e:/temp/tt"+ i + ".log");

					CompletableFuture<Void> future = std.addTask(in, out, 0, file.length());
					future.whenComplete((r, e) -> {
						try {
							out.flush();
						} catch (IOException e1) {
						}
						try {
							out.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						try {
							in.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						if (e != null) {
							e.printStackTrace();
						}
						System.out.println("transfer finished.");
					});
					future.join();
					System.out.println("exit");
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}

		});
	}

	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		StreamTransferDispatcher std = new StreamTransferDispatcher();
		int len = 20;
		Thread[] t = new Thread[len];
		
		long start= System.currentTimeMillis();
		
		for (int i = 0; i < len; ++i) {
			t[i] = download(std, i);
			t[i].start();
		}
		for (int i = 0; i < len; ++i) {
			t[i].join();
		}
		System.out.println("end:" + (System.currentTimeMillis() - start));
		StreamTransferTask.executor.shutdown();
	}

	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

}
