package com.qkinfotech.core.jpa.convertor;

import com.qkinfotech.util.DESUtil;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class BCryptConverter implements AttributeConverter<String, String> {

	@Override
	public String convertToDatabaseColumn(String data) {
		if (data != null) {
			return DESUtil.encrypt(data);
		}
		return null;
	}

	@Override
	public String convertToEntityAttribute(String data) {
		if (data != null) {
			try {
				return DESUtil.decrypt(data);
			} catch (Exception e) {
				return data;
			}
		}
		return null;
	}

}
