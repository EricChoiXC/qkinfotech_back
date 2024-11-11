package com.qkinfotech.core.file;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;

import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.storage.StorageManager;
import com.qkinfotech.core.storage.SysStorage;
import com.qkinfotech.core.storage.SysStorageData;

import jakarta.persistence.criteria.Predicate;

public class SysFileInputStream extends InputStream {
	
	protected SimpleService<SysFileSlice> sysFileSliceService;
	
	protected SysFile sysFile;

	protected long position;
	
	protected long start;

	protected long end;
	
	protected StorageManager storageManager;
	
	protected SysFileSlice slice = null;
	protected SysStorageData sysStorageData = null;
	protected byte[] buffer = null;

	public SysFileInputStream(SysFile sysFile, StorageManager storageManager, SimpleService<SysFileSlice> sysFileSliceService) {
		this.storageManager = storageManager;
		this.sysFile = sysFile;
		this.sysFileSliceService = sysFileSliceService;
		this.position = 0;
		this.start = 0;
		this.end = sysFile.getfSize();
	}
	
	public SysFileInputStream(SysFile sysFile, StorageManager storageManager, SimpleService<SysFileSlice> sysFileSliceService, long start, long end) {
		this.storageManager = storageManager;
		this.sysFile = sysFile;
		this.sysFileSliceService = sysFileSliceService;
		this.position = start;
		this.start = start;
		this.end = end;
	}
	
	public SysFile getSysFile() {
		return sysFile;
	}

	protected void readFromStorage() throws Exception {
		Specification<SysFileSlice> spec = (root, query, cb) -> {
			Predicate predicate = cb.and(
				cb.equal(root.get("fSysFile").get("fId"), sysFile.getfId()), 
				cb.lessThanOrEqualTo(root.get("fStart"), position)
			);
			return query.where(predicate).getRestriction();
		}; 
		Pageable pageable = PageRequest.of(0, 1, Sort.by(Order.desc("fStart")));
		Page<SysFileSlice> slices = sysFileSliceService.findAll(spec, pageable);
		if(slices.isEmpty()) {
			throw new IOException("File slice is not found");
		}
		slice = slices.getContent().get(0);
		sysStorageData = slice.getfSysStorageData();
		buffer = storageManager.get(sysStorageData.getfId());
	}
	
	@Override
	public int read() throws IOException {

		if(position >= end) {
			position = end;
			return -1;
		}
		if(buffer == null || (position - slice.getfStart()) >= slice.getfSize() || (position - slice.getfStart()) < 0) {
			try {
				readFromStorage();
			} catch(Exception e) {
				throw new IOException(e);
			}
		}
				
		int result =  buffer[(int)(position - slice.getfStart())]  & 0xFF;
		++ position;
		
		return result;
	}

	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int i =  super.read(b, off, len);
		//position += i;
		return i;
	}

	@Override
	public long skip(long n) throws IOException {
		if(position + n >= end) {
			long skipped = end - position;
			position = end;
			return skipped;
		} else {
			position += n;
			return n;
		}
	}

	@Override
	public void skipNBytes(long n) throws IOException {
		if( n < 0) {
			throw new IllegalArgumentException("skipNBytes n = " + n);
		}
		if(position + n >= end) {
			throw new EOFException();
		}
		position += n;
	}

	@Override
	public int available() throws IOException {
		return (int)(end - position);
	}

}
