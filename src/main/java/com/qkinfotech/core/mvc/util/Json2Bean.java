package com.qkinfotech.core.mvc.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

import jakarta.persistence.Convert;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hpsf.Decimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.util.TypeUtils;
import com.qkinfotech.core.mvc.BaseEntity;

import jakarta.persistence.EntityManager;

@Component
public class Json2Bean {

	@Autowired
	private EntityManager em;

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

	private Object[] toArrayValue(Object array, Type elementType, JSONArray value) throws Exception {
		Object[] result = null;

		if (elementType instanceof GenericArrayType) {
			Type type = ((GenericArrayType) elementType).getGenericComponentType();
			if (type instanceof ParameterizedType pt) {
				Class<?> clazz = Class.forName(pt.getRawType().getTypeName());
				result = (Object[]) Array.newInstance(clazz, value.size());
				if (Collection.class.isAssignableFrom(clazz)) {
					for (int i = 0; i < value.size(); ++i) {
						result[i] = toCollectionValue(null, pt, (JSONArray) value.get(i));
					}
				} else if (Map.class.isAssignableFrom(clazz)) {
					for (int i = 0; i < value.size(); ++i) {
						result[i] = toMapValue(null, pt, (JSONObject) value.get(i));
					}
				} else {
					for (int i = 0; i < value.size(); ++i) {
						result[i] = toObject(null, clazz, value.get(i));
					}
				}
			} else {
				result = (Object[]) Array.newInstance((Class<?>) type, value.size());
				for (int i = 0; i < value.size(); ++i) {
					result[i] = toObject(null, (Class<?>) type, value.get(i));
				}
			}
		} else if (elementType instanceof Class<?> tp) {
			if (tp.isArray()) {
				Class<?> clazz = tp.getComponentType();
				result = (Object[]) Array.newInstance(clazz, value.size());
				for (int i = 0; i < value.size(); ++i) {
					result[i] = toArrayValue(null, clazz.getComponentType(), (JSONArray) value.get(i));
				}
			} else {
				result = (Object[]) Array.newInstance(tp, value.size());
				for (int i = 0; i < value.size(); ++i) {
					result[i] = toObject(null, elementType, value.get(i));
				}
			}
		} else {
			throw new IllegalArgumentException("elementType is unknow:" + elementType);
		}
		return result;
	}

	private Map<String, ?> toMapValue(Object map, ParameterizedType elementType, JSONObject value) throws Exception {
		Map<String, Object> result = new HashMap<>();

		Type type = elementType.getActualTypeArguments()[1];
		if (type instanceof ParameterizedType) {
			Class<?> clazz = Class.forName(((ParameterizedType) type).getRawType().getTypeName());
			if (Collection.class.isAssignableFrom(clazz)) {
				for (String key : value.keySet()) {
					result.put(key, toCollectionValue(null, (ParameterizedType) type, (JSONArray) value.get(key)));
				}
			} else if (Map.class.isAssignableFrom(clazz)) {
				for (String key : value.keySet()) {
					result.put(key, toMapValue(null, (ParameterizedType) type, (JSONObject) value.get(key)));
				}
			} else {
				for (String key : value.keySet()) {
					result.put(key, toObject(null, clazz, value.get(key)));
				}
			}

		} else if (type instanceof Class<?> tp) {
			if (tp.isArray()) {
				for (String key : value.keySet()) {
					result.put(key, toArrayValue(null, tp.getComponentType(), (JSONArray) value.get(key)));
				}
			} else {
				for (String key : value.keySet()) {
					result.put(key, toObject(null, type, value.get(key)));
				}
			}
		} else {
			throw new IllegalArgumentException("elementType is unknow:" + elementType);
		}
		return result;
	}

	private Collection<?> toCollectionValue(Object collection, ParameterizedType elementType, JSONArray value) throws Exception {
		/* 2024-07-08 List和Set情况处理，默认List格式 */
		Collection<Object> result = new ArrayList<>();
		if (elementType.getRawType().getTypeName().equals(Set.class.getName())) {
			result = new HashSet<>();
		} else if (elementType.getRawType().getTypeName().equals(List.class.getName())) {
			result = new ArrayList<>();
		}

		Type type = elementType.getActualTypeArguments()[0];
		if (type instanceof ParameterizedType) {
			Class<?> clazz = Class.forName(((ParameterizedType) type).getRawType().getTypeName());
			if (Collection.class.isAssignableFrom(clazz)) {
				for (Object o : value) {
					result.add(toCollectionValue(null, (ParameterizedType) type, (JSONArray) o));
				}
			} else if (Map.class.isAssignableFrom(clazz)) {
				for (Object o : value) {
					result.add(toMapValue(null, (ParameterizedType) type, (JSONObject) o));
				}
			} else {
				for (Object o : value) {
					result.add(toObject(null, clazz, o));
				}
			}

		} else if (type instanceof Class<?> tp) {
			if (tp.isArray()) {
				for (Object o : value) {
					result.add(toArrayValue(null, tp.getComponentType(), (JSONArray) o));
				}
			} else {
				for (Object o : value) {
					result.add(toObject(null, type, o));
				}
			}

		} else {
			throw new IllegalArgumentException("elementType is unknow:" + elementType);
		}
		return result;
	}

	private Object toObject(Object result, Type elementType, Object value) throws Exception {
		if (value == null) {
			return null;
		}

		if (elementType instanceof Class<?> clazz) {
			if (rawTypes.contains(clazz)) {
				// 基本型
				return DefaultConversionService.getSharedInstance().convert(value, clazz);
			}

			if ((elementType instanceof Class) && Date.class.isAssignableFrom(clazz)) {
				// 日期型
				return TypeUtils.toDate(value);
			}

			if (result == null) {
				if (BaseEntity.class.isAssignableFrom(clazz)) {
					String fId = null;
					if (value instanceof JSONObject json) {
						fId = json.getString("fId");
					} else if (value instanceof String id) {
						fId = id;
					} else {
						throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to " + elementType.getTypeName());
					}

					if (StringUtils.hasText(fId)) {// 根据fId获取对象
						result = em.find(clazz, fId);
						if (value instanceof String) {
							return result;
						}
					}
					if (result == null) {
						result = clazz.getConstructor().newInstance();
						((BaseEntity) result).setfId(fId);
					}
				} else {
					if (value == null) {
						result = clazz.getConstructor().newInstance();
					} else {
						result = value;
					}
				}
			} else {
				if (BaseEntity.class.isAssignableFrom(clazz)) {
					String fId = null;
					if (value instanceof JSONObject json) {
						fId = json.getString("fId");
					} else if (value instanceof String id) {
						fId = id;
					} else {
						throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to " + elementType.getTypeName());
					}

					if (StringUtils.hasText(fId)) {
						if (result instanceof BaseEntity) {
							if (!((BaseEntity) result).getfId().equals(fId)) {
								result = em.find(clazz, fId);
								if (value instanceof String) {
									return result;
								}
							}
						}
					}
					if (result == null) {
						result = clazz.getConstructor().newInstance();
						((BaseEntity) result).setfId(fId);
					}
				} else {
					if (value == null) {
						result = clazz.getConstructor().newInstance();
					} else {
						result = value;
					}
				}
			}

			if (value instanceof JSONObject json) {
				Map<String, PropertyDescriptor> pds = getPropertyDescriptors(clazz);

				// 遍历属性并输出
				for (String key : json.keySet()) {
					PropertyDescriptor pd = pds.get(key);
					if (pd == null || exp.contains(key)) {
						continue;
					}
					Object data = pd.getReadMethod().invoke(result);
					Class<?> type = pd.getPropertyType();
					try {
						if (Collection.class.isAssignableFrom(type)) {
							if (result.getClass().getDeclaredField(key).isAnnotationPresent(Convert.class)) {
								data = toObject(data, type, json.get(key));
							} else {
								ParameterizedType pt = (ParameterizedType) result.getClass().getDeclaredField(key).getGenericType();
								data = toCollectionValue(data, pt, (JSONArray) json.getJSONArray(key));
							}
						} else if (Map.class.isAssignableFrom(type)) {
							if (result.getClass().getDeclaredField(key).isAnnotationPresent(Convert.class)) {
								data = toObject(data, type, json.get(key));
							} else {
								ParameterizedType pt = (ParameterizedType) result.getClass().getDeclaredField(key).getGenericType();
								data = toMapValue(data, pt, (JSONObject) json.getJSONObject(key));
							}
						} else if (type.isArray()) {
							if (result.getClass().getDeclaredField(key).isAnnotationPresent(Convert.class)) {
								data = toObject(data, type, json.get(key));
							} else {
								Type pt = result.getClass().getDeclaredField(key).getGenericType();
								data = toArrayValue(data, pt, (JSONArray) json.getJSONArray(key));
							}
						} else {
							data = toObject(data, type, json.get(key));
						}
						pd.getWriteMethod().invoke(result, data);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
			}

			return result;

		}
		throw new IllegalArgumentException("elementType is unknow:" + elementType);
	}

	@SuppressWarnings("unchecked")
	public <T> T toBean(JSONObject body, Class<T> entityClass) throws Exception {
		return (T) toObject(null, entityClass, body);
	}

}
