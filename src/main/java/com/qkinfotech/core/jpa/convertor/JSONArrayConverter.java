package com.qkinfotech.core.jpa.convertor;

import com.alibaba.fastjson2.JSONArray;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class JSONArrayConverter implements AttributeConverter<JSONArray, String> {

	@Override
	public String convertToDatabaseColumn(JSONArray json) {
		if (json != null) {
			return json.toJSONString();
		}
		return null;
	}

	@Override
	public JSONArray convertToEntityAttribute(String data) {
		if (data != null) {
			try {
				return JSONArray.parseArray(data);
			} catch (Exception e) {
				return new JSONArray();
			}
		}
		return null;
	}

}
