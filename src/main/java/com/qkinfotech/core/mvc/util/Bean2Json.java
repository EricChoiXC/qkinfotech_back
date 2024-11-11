package com.qkinfotech.core.mvc.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hpsf.Decimal;
import org.springframework.data.util.ProxyUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.IBaseEntity;

@Component
public class Bean2Json {

	protected Integer parseHierarchyLevel_MAX = 5;

	private static Map<Class<?>, Map<String, PropertyDescriptor>> pds = new HashMap<>();

	private boolean isTransient(PropertyDescriptor propertyDescriptor) {
		Object value = propertyDescriptor.getValue("transient");
		return (value instanceof Boolean) ? (Boolean) value : false;
	}

	private Map<String, PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) {

		Map<String, PropertyDescriptor> result;

		result = pds.get(clazz);
		if (result != null) {
			return result;
		}

		result = new HashMap<String, PropertyDescriptor>();

		PropertyDescriptor[] pda = PropertyUtils.getPropertyDescriptors(clazz);

		for (PropertyDescriptor p : pda) {
			if (p.getWriteMethod() != null && p.getReadMethod() != null && !isTransient(p)) {
				result.put(p.getName(), p);
			}
		}
		pds.put(clazz, result);

		return result;
	}

	private static List<String> exp = List.of("class");

	private static List<Class<?>> rawTypes = List.of(String.class, byte.class, Byte.class, short.class, Short.class, char.class, Character.class, int.class, Integer.class, long.class, Long.class,
			float.class, Float.class, double.class, Double.class, Number.class, Decimal.class, BigDecimal.class, boolean.class, Boolean.class);


	private Object toJson(Object value, boolean recursive, int hierarchyLevel) throws Exception {
		if(value == null) {
			return null;
		}
		if(value instanceof Collection<?> collection) {
			JSONArray arr = new JSONArray();
			if (hierarchyLevel <= parseHierarchyLevel_MAX) {
				for(Object item: collection) {
					arr.add(toJson(item, true, hierarchyLevel));
				}
			}
			return arr;
		} else if(value instanceof Map<?,?> map) {
			//System.out.println("toJson map:" + value.getClass());
			//System.out.println("value:" + value.toString());
			JSONObject json = new JSONObject();
			if (hierarchyLevel <= parseHierarchyLevel_MAX) {
				for(Map.Entry<?,?> e : map.entrySet()) {
					json.put(e.getKey().toString(), toJson(e.getValue(), true, 0));
				}
			}
			return json;
		} else if(value.getClass().isArray()) {
			JSONArray arr = new JSONArray();
			int len = Array.getLength(value);
			if (hierarchyLevel <= parseHierarchyLevel_MAX) {
				for(int i = 0; i < len; ++ i) {
					arr.add(toJson(Array.get(arr, i), true, hierarchyLevel));
				}
			}
			return arr;
		} else if(value instanceof Date date) {
			return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(date);
		} else if (value instanceof Boolean) {
			return value;
		} else if(rawTypes.contains(value.getClass())) {
			return value.toString();
		} else {
			//System.out.println("toJson json:" + value.getClass());
			JSONObject json = new JSONObject();
			Class<?> clazz = ProxyUtils.getUserClass(value.getClass());
			Map<String, PropertyDescriptor> pds = getPropertyDescriptors(clazz);
			for(PropertyDescriptor p : pds.values()) {
				Object v = p.getReadMethod().invoke(value);
				if(v != null && !recursive) {
					if(v instanceof Collection<?> collection) {
					} else if(v instanceof Map<?,?> map) {
					} else if(v.getClass().isArray()) {
					} else if(v instanceof Date) {
					} else if(rawTypes.contains(v.getClass())) {
					} else {
						v = null;
					}
				}
				if(v != null) {
					if (hierarchyLevel <= parseHierarchyLevel_MAX) {
						//System.out.println(value.getClass() + ":" + p.getName() + " " + hierarchyLevel);
						json.put(p.getName(), toJson(v, false, hierarchyLevel+1));
					}
				}
			}
			return json;
		}
	}

	public JSONObject toJson(Object value, boolean recursive) {
		try {
			return (JSONObject)toJson(value, true, 0);
		}catch(Exception e) {
			if(e instanceof RuntimeException ex) {
				throw ex;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	public JSONObject toJson(IBaseEntity data) {
		try {
			return (JSONObject)toJson(data, true);
		}catch(Exception e) {
			if(e instanceof RuntimeException ex) {
				throw ex;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	public JSONObject toJson(Object data, List<String> output) throws Exception {
		try {
			return (JSONObject) toJson(data, output, 0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 2024-10-21 指定输出字段后的新版toJson
	 * @param data 对象
	 * @param output 输出字段
	 */
	public Object toJson(Object data, List<String> output, int hierarchyLevel) throws Exception {
		try {
			//空对象返回null
			if (data == null) {
				return null;
			}
			if (data instanceof Collection<?> collection) {
				//1. 对象为集合，遍历集合内数据
				JSONArray arr = new JSONArray();
				for (Object item : collection) {
					arr.add(toJson(item, output));
				}
				return arr;
			} else if (data instanceof Map<?, ?> map) {
				//2. 对象为Map，直接转换
				JSONObject json = new JSONObject();
				for (Map.Entry<?, ?> entry : map.entrySet()) {
					json.put(entry.getKey().toString(), toJson(entry.getValue(), output));
				}
				return json;
			} else if (data.getClass().isArray()) {
				//3. 对象为数组，同集合遍历
				JSONArray arr = new JSONArray();
				int len = Array.getLength(data);
				for(int i = 0; i < len; ++ i) {
					arr.add(toJson(Array.get(arr, i), output));
				}
				return arr;
			} else if (data instanceof Date date) {
				return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(date);
			} else if (data instanceof Boolean) {
				return data;
			} else if(rawTypes.contains(data.getClass())) {
				return data.toString();
			} else {
				//是否指定标准或简单输出
				if (output == null) {
					return null;
				}
				boolean simpleMode = false;
				boolean standardMode = false;
				Map<String, List<String>> nextOutput = new HashMap<>();
				Iterator<String> it = output.iterator();
				while(it.hasNext()) {
					String str = it.next();
					if (str.equals("*")) {
						standardMode = true;
						hierarchyLevel = 0;
					}
					if (str.equals("?")) {
						simpleMode = true;
					}
					if (str.indexOf(".") != -1) {
						String name = str.substring(0, str.indexOf("."));
						List<String> list = nextOutput.get(name);
						if (list == null) {
							list = new ArrayList<>();
							nextOutput.put(name, list);
						}
						list.add(str.substring(str.indexOf(".") + 1));
					}
				}

				JSONObject json = new JSONObject();
				Class<?> clazz = ProxyUtils.getUserClass(data.getClass());
				Map<String, PropertyDescriptor> pds = getPropertyDescriptors(clazz);

				for(PropertyDescriptor p : pds.values()) {
					Object v = p.getReadMethod().invoke(data);
					if(v != null) {
						//遍历字段
						if (standardMode) {
							if (hierarchyLevel < parseHierarchyLevel_MAX) {
								List<String> list = nextOutput.get(p.getName());
								if (list == null) {
									list = new ArrayList<>();
								}
								list.add("*");
								json.put(p.getName(), toJson(v, list, hierarchyLevel + 1));
							}
						} else if (output.contains(p.getName()) || nextOutput.containsKey(p.getName())) {
							json.put(p.getName(), toJson(v, nextOutput.get(p.getName()), hierarchyLevel));
						} else if (simpleMode) {
							if (rawTypes.contains(v.getClass())) {
								json.put(p.getName(), toJson(v, nextOutput.get(p.getName()), hierarchyLevel));
							} else if (v instanceof Date date) {
								json.put(p.getName(), (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(date));
							}
						}
					}
				}
				return json;
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public boolean hasAttribute(PropertyDescriptor p, String name) {
		Object att = p.getValue(name);
		return (att instanceof Boolean) ? (Boolean)att : false;
	}
}
