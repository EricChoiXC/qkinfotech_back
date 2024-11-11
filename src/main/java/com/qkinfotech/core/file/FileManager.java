package com.qkinfotech.core.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.storage.StorageManager;
import com.qkinfotech.core.storage.SysStorageData;
import com.qkinfotech.util.ByteArrayChannel;

import jakarta.activation.MimetypesFileTypeMap;
import jakarta.persistence.criteria.Predicate;

@Component
@Transactional
public class FileManager {

	public static final int CHUNK_SIZE = 20 * 1024; //20 * 1024 * 1024;
	
	@Autowired
	protected SimpleService<SysFile> sysFileService;
	
	@Autowired
	protected SimpleService<SysFileSlice> sysFileSliceService;
	
	@Autowired
	protected StorageManager storageManager;
	
	protected MimetypesFileTypeMap typeMap = new MimetypesFileTypeMap();


	public SysFileInputStream getInputStream(String id) throws SysFileStatusException {
		return getInputStream(sysFileService.getById(id));
	}
	
	public SysFileInputStream getInputStream(SysFile sysFile) throws SysFileStatusException {
		if(sysFile == null) {
			throw new NullPointerException("getInputStream:SysFile is null");
		}
		if(sysFile.getfStatus() != 1) {
			throw new SysFileStatusException();
		}
		return new SysFileInputStream(sysFile, storageManager,sysFileSliceService);
	}


	public SysFileInputStream getInputStream(String id, long start, long end) throws SysFileStatusException {
		return getInputStream(sysFileService.getById(id), start, end);
	}

	public SysFileInputStream getInputStream(SysFile sysFile, long start, long end) throws SysFileStatusException {
		if(sysFile == null) {
			throw new NullPointerException("getInputStream:SysFile is null");
		}
		if(sysFile.getfStatus() != 1) {
			throw new SysFileStatusException();
		}
		return new SysFileInputStream(sysFile, storageManager,sysFileSliceService, start, end);
	}

	public SysFile getSysFile(String id) {
		return sysFileService.getById(id);
	}
	
	
	public SysFile getSysFileBySizeAndDigest(long size, String digest) {
		Specification<SysFile> spec = (root, query, cb) -> {
			Predicate predicate = cb.and(
				cb.equal(root.get("fDigest"), digest), 
				cb.equal(root.get("fSize"), size)
			);
			return query.where(predicate).getRestriction();
		}; 
		List<SysFile> files = sysFileService.findAll(spec);
		SysFile file = null;
		if(files != null && files.size() == 1) {
			file = files.get(0);
		} else {
			file = null;
		}	
		return file;
	}
	
	public SysFile create(String filename, long size, String digest) {
		
		SysFile file = getSysFileBySizeAndDigest(size, digest);
		if(file != null) {
			return file;
		}
		
		SysFile sysFile = new SysFile();
		sysFile.setfMimeType(typeMap.getContentType(filename));
		sysFile.setfDigest(digest);
		sysFile.setfSize(size);
		sysFile.setfCreateTime(new Date());
		sysFile.setfStatus(0);
		sysFile.setfFileName(filename);
		sysFileService.save(sysFile);
		
		return sysFile;
	}

	public SysFile checkSysFileStatus(SysFile sysFile) {
		
		Long size = (Long)sysFileSliceService.createQuery("select sum(fSize) from SysFileSlice where fSysFile.fId = :fId")
			.setParameter("fId", sysFile.getfId())
			.getSingleResult();
		if(sysFile.getfSize() == size) {
			sysFile.setfStatus(1);
			sysFileService.save(sysFile);
		}
		return sysFile;
		
	}
	
	public SysFile transfer(SysFile sysFile, ReadableByteChannel fc) throws Exception {
		if(sysFile.getfStatus() != 0) {
			throw new SysFileStatusException();
		}
		ByteBuffer buf= ByteBuffer.wrap(new byte[CHUNK_SIZE]);
		long pos = 0;
		while(true) {
			int size = fc.read(buf);
			if(size <= 0) {
				break;
			}
			SysStorageData ssd = storageManager.put(buf.array(), 0, size);
			buf.flip();
			SysFileSlice sfs = new SysFileSlice();
			sfs.setfSysFile(sysFile);
			sfs.setfStart(pos);
			sfs.setfSize(size);
			sfs.setfSysStorageData(ssd);
			sysFileSliceService.save(sfs);
			pos += size;
		}
		
		return checkSysFileStatus(sysFile);
	}
	
	public SysFile transfer(SysFile sysFile, String filename) throws Exception {
		try(FileInputStream fis = new FileInputStream(filename)) {
			return transfer(sysFile, fis.getChannel());
		}
	}
	
	public SysFile transfer(SysFile sysFile, File file) throws Exception {
		try(FileInputStream fis = new FileInputStream(file)) {
			return transfer(sysFile, fis.getChannel());
		}
	}

	public SysFile transfer(SysFile sysFile, byte[] bytes) throws Exception {
		return transfer(sysFile, new ByteArrayChannel(bytes));
	}
	
	public SysFile create(File filepath) throws Exception {
		return create(filepath.getName(), filepath);
	}
	
	public SysFile create(String filename, File filepath) throws Exception {
		String digest = DigestUtils.md5DigestAsHex(new FileInputStream(filepath));
		SysFile sysFile = create(filename, filepath.length(), digest);

		if(sysFile.getfStatus() == 0) {
			transfer(sysFile, filepath);
		}
		return sysFile;
	}
	
	public SysFile create(String filename, byte[] bytes) throws Exception {
		String digest = DigestUtils.md5DigestAsHex(bytes);
		SysFile sysFile = create(filename, bytes.length, digest);

		if(sysFile.getfStatus() == 0) {
			transfer(sysFile, new ByteArrayChannel(bytes));
		}
		return sysFile;
	}

}
