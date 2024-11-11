package com.qkinfotech.core.storage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.file.FileStorage;

import jakarta.persistence.criteria.Predicate;

@Component
public class StorageManager implements ApplicationListener<ApplicationReadyEvent>, InitializingBean {

	private Map<SysStorage, IStorage> storages = new HashMap<SysStorage, IStorage>();

	private SysStorage defaultSysStorage;

	@Autowired
	private SimpleService<SysStorage> sysStorageService;

	@Autowired
	private SimpleService<SysStorageData> sysStorageDataService;

	private static String defaultStorageId = "ffffffffffffffffffffffff";
	
	public StorageManager() {
		
	}
	
	private SysStorage initDefaultStorage() {
		JSONObject config = new JSONObject();
		config.put("baseDir", "e:/storage");
		SysStorage sysStorage = new SysStorage();
		sysStorage.setfDefault(false);
		sysStorage.setfName("default");
		sysStorage.setfId(defaultStorageId);
		sysStorage.setfConfig(config);
		sysStorage.setfClassName(FileStorage.class.getCanonicalName());
		return sysStorage;
	}
	
	public void refresh() {
		List<SysStorage> list = sysStorageService.findAll();
		if(list.size() == 0) {
			SysStorage sysStorage = initDefaultStorage();
			sysStorageService.save(sysStorage);
			list.add(sysStorage);
		}
		storages.clear();
		for(SysStorage sysStorage : list) {
			try {
				Class<?> clazz = Class.forName(sysStorage.getfClassName());
				IStorage storage = (IStorage) clazz.getConstructor().newInstance();
				storage.config(sysStorage);
				if(sysStorage.getfDefault()) {
					this.defaultSysStorage = sysStorage;
				}
				storages.put(sysStorage, storage);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}		
		}
		if(defaultSysStorage == null) {
			defaultSysStorage = storages.keySet().iterator().next();
		}
	}

	public Collection<IStorage>  getStorages() {
		return storages.values();
	}
	
	public IStorage getDefaultStorage() {
		if(defaultSysStorage == null) {
			refresh();
		}
		return storages.get(defaultSysStorage);
	}
	
	public SysStorageData put(byte[] src) throws Exception {
		return put(defaultSysStorage, src, 0, src.length);
	}
	public SysStorageData put(SysStorage sysStorage, byte[] src) throws Exception {
		return put(sysStorage, src, 0, src.length);
	}
	
	public SysStorageData put(byte[] src, int position, int length) throws Exception {
		return put(defaultSysStorage, src, position, length);
	}
	public SysStorageData put(SysStorage sysStorage, byte[] src, int position, int length) throws Exception {
		
		byte[] bytes = Arrays.copyOfRange(src, position, position + length);
		IStorage storage = storages.get(sysStorage);
		if(storage == null) {
			throw new IOException("sysStorage not defined.");
		}
		String md5 = DigestUtils.md5DigestAsHex(bytes);
		String sha = Sha512DigestUtils.shaHex(bytes);
		Specification<SysStorageData> spec = (root, query, cb) -> {
			Predicate predicate = cb.and(
				cb.equal(root.get("fSize"), bytes.length),
				cb.equal(root.get("fMD5"), md5), 
				cb.equal(root.get("fSHA"), sha)
			);
			return query.where(predicate).getRestriction();
		}; 
		List<SysStorageData> list = sysStorageDataService.findAll(spec);
		if(list.size() > 0) {
			return list.get(0);
		}
		
		SysStorageData ssd = new SysStorageData();
		ssd.setfMD5(md5);
		ssd.setfSHA(sha);
		ssd.setfSysStorage(sysStorage);
		String fKey = storage.put(bytes);
		ssd.setfKey(fKey);
		ssd.setfSize(bytes.length);
		sysStorageDataService.save(ssd);
		
		return ssd;
	}

	public byte[] get(String id) throws Exception {
		SysStorageData sysStorageData = sysStorageDataService.getById(id);
		
		if(sysStorageData == null) {
			throw new IOException("store data id not found:" + id);
		}
		
		if(sysStorageData.getfSysStorage() == null) {
			throw new IOException("storage is null:" + id);
		}
		
		IStorage storage = storages.get(sysStorageData.getfSysStorage());
		
		if(storage == null) {
			throw new IOException("storage not found:" + sysStorageData.getfSysStorage().getfId());
		}
		
		return storage.get(sysStorageData.getfKey());
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
//		if (event.getApplicationContext().getParent() != null) {
//			return;
//		}
//		refresh();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		refresh();
		
	}

}
