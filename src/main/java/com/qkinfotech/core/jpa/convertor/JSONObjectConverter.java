package com.qkinfotech.core.jpa.convertor;

import com.alibaba.fastjson2.JSONObject;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class JSONObjectConverter implements AttributeConverter<JSONObject, String> {

	@Override
	public String convertToDatabaseColumn(JSONObject json) {
		if (json != null) {
			return json.toJSONString();
		}
		return null;
	}

	@Override
	public JSONObject convertToEntityAttribute(String data) {
		if (data != null) {
			try {
				return JSONObject.parseObject(data);
			} catch (Exception e) {
				return new JSONObject();
			}
		}
		return null;
	}

}
