package com.qkinfotech.core.task.datatype;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import jakarta.persistence.AttributeConverter;

public class JSONObjectConverter implements AttributeConverter<JSONObject, String> {

	@Override
	public String convertToDatabaseColumn(JSONObject attribute) {
		if (attribute != null) {
			return attribute.toJSONString();
		}
		return null;
	}

	@Override
	public JSONObject convertToEntityAttribute(String dbData) {
		if (dbData != null) {
			return JSON.parseObject(dbData);
		}
		return null;
	}

}
