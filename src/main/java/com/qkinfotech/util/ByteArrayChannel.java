package com.qkinfotech.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class ByteArrayChannel implements ReadableByteChannel {
	
	protected byte[] bytes;
	
	protected int position;
	
	public ByteArrayChannel(byte[] bytes) {
		position = 0;
		this.bytes = bytes;
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		if(position >= bytes.length) {
			return -1;
		}
		int size = dst.remaining();
		if(size > bytes.length - position) {
			size = bytes.length - position;
		}
		dst.put(bytes, position, size);
		position += size;
		return size;
	}

}
