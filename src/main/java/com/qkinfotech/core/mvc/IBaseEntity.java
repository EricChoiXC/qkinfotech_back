package com.qkinfotech.core.mvc;

import java.util.Map;

public interface IBaseEntity {

	String getfId();
	
	void setfId(String fId);
	
	Map<String, Object> getFeatureMap();

	default void setFeatureValue(String key, Object value) {
		getFeatureMap().put(key, value);
	}

	default Object getFeatureValue(String key) {
		return getFeatureMap().get(key);
	}
	
}
