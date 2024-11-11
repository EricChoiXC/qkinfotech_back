package com.qkinfotech.core.mvc;

import com.alibaba.fastjson2.JSONObject;

public interface IEntityExtension {
	
	default void adding(BaseEntity entity){ }

	default void added(BaseEntity entity){ }

	default JSONObject updating(BaseEntity entity, JSONObject savedData){ return null;}

	default void updated(BaseEntity entity, JSONObject savedData){ }
	
	default void deleting(BaseEntity entity) { }

	default void deleted(BaseEntity entity) { }
	
	default void init(BaseEntity entity){ }
	
	default void prepare(Class<? extends BaseEntity> entutyClass, String method, JSONObject requestData) { }

}
