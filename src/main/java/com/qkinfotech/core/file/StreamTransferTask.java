package com.qkinfotech.core.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StreamTransferTask {
	
	public static ExecutorService executor = Executors.newFixedThreadPool(20);

	private InputStream in;
	private OutputStream out;
	private long pos;
	private long totalSize;

	private int size;
	private StreamTransferDispatcher dispatcher;
	private byte[] buffer;
	private int status = StreamTransferDispatcher.TRANSFER_STATUS_NONE;

	public StreamTransferTask(InputStream in, OutputStream out, long pos, long totalSize) {
		this.in = in;
		this.out = out;
		this.pos = pos;
		this.totalSize = totalSize;
	}

	void read() {
		if (pos >= totalSize) {
			return;
		}
		size = (int) (totalSize - pos);
		if (size > buffer.length) {
			size = buffer.length;
		}
		CompletableFuture.runAsync(() -> {
			try {
				in.read(buffer, 0, size);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}, executor).whenComplete((r, e) -> {
			//System.out.println("r:" + pos + "+" + size + "=" + (pos+size) + "/" + totalSize);
			if (e == null) {
				status = StreamTransferDispatcher.TRANSFER_STATUS_READ;
				if (pos + size < totalSize) {
					dispatcher.addTask(in, out, pos + size, totalSize);
				} else {
					logger.debug("read finished");
				}
				dispatcher.readComplete(in);
			} else {
				e.printStackTrace();
				dispatcher.error(in, e);
			}
		});
	}

	void write() {
		CompletableFuture.runAsync(() -> {
			try {
				//System.out.println("w:" + pos + "+" + size + "=" + (pos+size) + "/" + totalSize);
				out.write(buffer, 0, size);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}, executor).whenComplete((r, e) -> {
			if (e == null) {
				status = StreamTransferDispatcher.TRANSFER_STATUS_WROTE;
				dispatcher.writeComplete(in, buffer);
				if (pos + size >= totalSize) {
					dispatcher.complete(in);
				}
			} else {
				e.printStackTrace();
				dispatcher.error(in, e);
			}
		});
	}

	byte[] getBuffer() {
		return buffer;
	}

	void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	void setDispatcher(StreamTransferDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	int getStatus() {
		return status;
	}
	
	void setStatus(int status) {
		this.status = status;
	}

	public InputStream getInputStream() {
		return in;
	}
}
