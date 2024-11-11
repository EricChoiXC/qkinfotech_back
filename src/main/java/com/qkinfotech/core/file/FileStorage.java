package com.qkinfotech.core.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.qkinfotech.core.storage.IStorage;
import com.qkinfotech.core.storage.SysStorage;
import com.qkinfotech.util.IDGenerate;

public class FileStorage implements IStorage {

	protected String base;
	
	@Override
	public void config(SysStorage sysStorage) {
		this.base = sysStorage.getfConfig().getString("baseDir");
		base = base.trim();
		if (!base.endsWith("/")) {
			base = base + "/";
		}
	}

	@Override
	public String put(byte[] bytes) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		String location = sdf.format(new Date()) + "/" + IDGenerate.generate();
		
		File file = new File(base + location);
		file.getParentFile().mkdirs();
		try(FileOutputStream fos = new FileOutputStream(file)) {
			try(FileChannel fc = fos.getChannel()) {
				ByteBuffer buf = ByteBuffer.wrap(bytes);
				fc.write(buf);
			}
		}
		
		return location;
	}

	@Override
	public byte[] get(String key) throws Exception {
		File file = new File(base + key);
		try(FileInputStream fis = new FileInputStream(file)) {
			try(FileChannel fc = fis.getChannel()) {
				byte[] bytes = new byte[(int)file.length()];
				ByteBuffer buf = ByteBuffer.wrap(bytes);
				fc.read(buf);
				return buf.array();
			}
		}
	}
	
}
