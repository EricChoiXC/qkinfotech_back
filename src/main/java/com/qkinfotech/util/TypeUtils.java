package com.qkinfotech.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

import com.qkinfotech.core.log.model.SysLog;

public class TypeUtils {

	public static List<Class<?>> rawTypes = List.of(String.class, byte.class, Byte.class, short.class, Short.class, char.class, Character.class, int.class, Integer.class, long.class, Long.class,
			float.class, Float.class, double.class, Double.class, Number.class, BigDecimal.class);

	public static Map<String, String> dateFormat;
	static {
		dateFormat = new HashMap<>();
		dateFormat.put("yyyy", "^\\d{4}$");
		dateFormat.put("yyyyMM", "^\\d{4}\\d{2}$");
		dateFormat.put("yyyyMMdd", "^\\d{4}\\d{1,2}\\d{1,2}$");

		dateFormat.put("yyyy/MM", "^\\d{4}/\\d{2}$");
		dateFormat.put("yyyy/MM/dd", "^\\d{4}/\\d{2}/\\d{2}$");

		dateFormat.put("yyyy-MM", "^\\d{4}-\\d{2}$");
		dateFormat.put("yyyy-MM-dd", "^\\d{4}-\\d{2}-\\d{2}$");

		dateFormat.put("yyyy.MM", "^\\d{4}\\.\\d{2}$");
		dateFormat.put("yyyy.MM.dd", "^\\d{4}\\.\\d{2}\\.\\d{2}$");

		dateFormat.put("yyyyMMdd HH:mm:ss", "^\\d{4}\\d{1,2}\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}$");
		dateFormat.put("yyyy/MM/dd HH:mm:ss", "^\\d{4}/\\d{1,2}/\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}$");
		dateFormat.put("yyyy-MM-dd HH:mm:ss", "^\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}$");
		dateFormat.put("yyyy.MM.dd HH:mm:ss", "^\\d{4}\\.\\d{1,2}\\.\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}$");
	}

	public boolean isRawType(Class<?> clazz) {
		return rawTypes.contains(clazz); 
	}

	public boolean toString(Object o) {
		return rawTypes.contains(o); 
	}
	
	private HashMap<List<SysLog>[], List<SysLog>> map;
	


	public HashMap<List<SysLog>[], List<SysLog>> getMap() {
		return map;
	}

	public void setMap(HashMap<List<SysLog>[], List<SysLog>> map) {
		this.map = map;
	}

	
	
	public static void main(String[] args) throws NoSuchFieldException, SecurityException {
		
		PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(TypeUtils.class);
		
		for(int i = 0; i < propertyDescriptors.length; ++i) {
			System.out.println(propertyDescriptors[i].getName());
		}
		
		PropertyDescriptor p = propertyDescriptors[0];
		
		Class<?> clazz = p.getPropertyType();
		ParameterizedType pt = (ParameterizedType)TypeUtils.class.getDeclaredField(p.getName()).getGenericType();
		Type k = pt.getActualTypeArguments()[0];
		ParameterizedType v = (ParameterizedType)pt.getActualTypeArguments()[1];
		System.out.println(((GenericArrayType)k).getGenericComponentType());
		System.out.println(v);
		System.out.println(v.getActualTypeArguments()[0]);
		
	}
}
