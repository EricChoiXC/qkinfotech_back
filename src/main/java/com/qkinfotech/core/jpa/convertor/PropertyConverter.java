package com.qkinfotech.core.jpa.convertor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PropertyConverter implements AttributeConverter<Properties, String> {

	@Override
	public String convertToDatabaseColumn(Properties properties) {
		if(properties == null) {
			return null;
		}
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			properties.store(out, null);
		} catch (IOException e) {
			return null;
		}
		return out.toString();
	}

	@Override
	public Properties convertToEntityAttribute(String data) {
		if(data == null) {
			return null;
		}
		Properties prop = new Properties();
		try {
			prop.load(new ByteArrayInputStream(data.getBytes("ISO_8859_1")));
		} catch (Exception e) {
		}
		return prop;
	}

}
