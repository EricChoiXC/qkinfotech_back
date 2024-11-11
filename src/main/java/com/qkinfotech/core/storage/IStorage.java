package com.qkinfotech.core.storage;

public interface IStorage {
	
	void config(SysStorage sysStorage);

	String put(byte[] bytes) throws Exception;

	byte[] get(String key) throws Exception;


}
