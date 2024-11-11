package com.qkinfotech.core.app.config;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.ProxyUtils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.app.model.SysConfig;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.util.SerializableUtil;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public abstract class AppConfig {

	@Autowired
	protected SimpleService<SysConfig> sysConfigService;

	@Autowired
	protected EntityManager em;

	protected PropertyDescriptor[] propertyDescriptors;
	
	public abstract String getModelName();
	
	private Specification<SysConfig> load() {
		Specification<SysConfig> spec = new Specification<SysConfig>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<SysConfig> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				
				Predicate predicate = cb.equal(root.get("fModelName"), getModelName());

				return query.where(predicate).getRestriction();
			}
		};
		return spec;
	}
	
	@PostConstruct
	public void refresh() {
		Class<?> clazz = ProxyUtils.getUserClass(this.getClass());
		propertyDescriptors = PropertyUtils.getPropertyDescriptors(clazz);
		
		List<SysConfig> list = sysConfigService.findAll(load());
		if(!CollectionUtils.isEmpty(list)) {
			for(SysConfig sysConfig : list) {
				for(PropertyDescriptor propertyDescriptor : propertyDescriptors) {
					if(Objects.equals(propertyDescriptor.getName(), sysConfig.getfPropertyName())) {
						if(propertyDescriptor.getWriteMethod() == null || propertyDescriptor.getReadMethod() == null || isTransient(propertyDescriptor)) {
							break;
						}
						try {
							decode(this, propertyDescriptor, sysConfig.getfPropertyValue());
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					}
				}
			}
		}
	}
	
	public void commit() {
		sysConfigService.delete(load());
		sysConfigService.flush();
		for(PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			if(propertyDescriptor.getWriteMethod() == null || propertyDescriptor.getReadMethod() == null || isTransient(propertyDescriptor)) {
				continue;
			}
			SysConfig sysConfig = new SysConfig();
			sysConfig.setfModelName(getModelName());
			sysConfig.setfPropertyName(propertyDescriptor.getName());
			try {
				sysConfig.setfPropertyValue(encode(this, propertyDescriptor));
				sysConfigService.save(sysConfig);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//TODO 通知其他节点 
		
	}
	
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	
	private boolean isTransient(PropertyDescriptor propertyDescriptor){
		Object value = propertyDescriptor.getValue("transient");
        return (value instanceof Boolean)? (Boolean) value : false;
	}

	private Object decode(String value, Type type) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, SecurityException, ParseException {
		if (value == null) {
			return null;
		}

		Class clazz = null;

		if (type instanceof GenericArrayType t) {
			List<String> list = JSONArray.parseArray(value, String.class);
			Object[] result = (Object[]) Array.newInstance((Class)t.getGenericComponentType(), list.size());
			for (int i=0; i < list.size(); ++i) {
				result[i] = decode(list.get(i), t.getGenericComponentType());
			}
			return result;
		} else if (type instanceof ParameterizedType) {
			clazz = (Class) ((ParameterizedType) type).getRawType();
		} else if (type instanceof Class) {
			clazz = (Class) type;
		} else {
			throw new IllegalArgumentException("Unknow type: " + type);
		}

		if (Collection.class.isAssignableFrom(clazz)) {
			JSONArray ary = JSONArray.parseArray(value);
			List list = new ArrayList();
			for (int i = 0; i < ary.size(); i++) {
				list.add(encode(ary.getString(i), ((ParameterizedType) type).getActualTypeArguments()[0]));
			}
			return list;
		} else if (Map.class.isAssignableFrom(clazz)) {
			JSONObject obj = JSONObject.parseObject(value);
			Map map = new HashMap<>();
			Type keyType = ((ParameterizedType) type).getActualTypeArguments()[0];
			Type valueType = ((ParameterizedType) type).getActualTypeArguments()[1];
			for (Map.Entry e : obj.entrySet()) {
				Object k = decode(e.getKey().toString(), keyType);
				Object v = decode(e.getValue().toString(), valueType);
				map.put(k, v);
			}
			return map;
		} else if (BaseEntity.class.isAssignableFrom(clazz)) {
			return em.find(clazz, value);
		} else if (clazz.isPrimitive()) {
			return DefaultConversionService.getSharedInstance().convert(value, clazz);
		} else if (String.class.equals(clazz)) {
			return value;
		} else if (Date.class.isAssignableFrom(clazz)) {
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			return sdf.parse(value);
		} else if (Number.class.isAssignableFrom(clazz)) {
			return DefaultConversionService.getSharedInstance().convert(value, clazz);
		} else {
			return SerializableUtil.unmarshall(value);
		}
	}

	private void decode(Object target, PropertyDescriptor propertyDescriptor, String value)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, SecurityException, ParseException {
		propertyDescriptor.getWriteMethod().invoke(target, decode(value, propertyDescriptor.getPropertyType()));
	}

	private String encode(Object value, Type type) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, SecurityException {
		if (value == null) {
			return null;
		}

		Class clazz = null;

		if (type instanceof GenericArrayType t) {
			int len = Array.getLength(value);
			List list = new ArrayList();
			for (int i = 0; i < len; i++) {
				list.add(encode(Array.get(value, i), t.getGenericComponentType()));
			}
			return JSONArray.toJSONString(list);
		} else if (type instanceof ParameterizedType) {
			clazz = (Class) ((ParameterizedType) type).getRawType();
		} else if (type instanceof Class) {
			clazz = (Class) type;
		} else {
			throw new IllegalArgumentException("Unknow type: " + type);
		}

		if (Collection.class.isAssignableFrom(clazz)) {
			Collection col = (Collection) value;
			List list = new ArrayList();
			for (int i = 0; i < col.size(); i++) {
				list.add(encode(Array.get(value, i), ((ParameterizedType) type).getActualTypeArguments()[0]));
			}
			return JSONArray.toJSONString(list);
		} else if (Map.class.isAssignableFrom(clazz)) {
			Map<?, ?> map = (Map) value;
			Map<String, String> result = new HashMap<>();
			Type keyType = ((ParameterizedType) type).getActualTypeArguments()[0];
			Type valueType = ((ParameterizedType) type).getActualTypeArguments()[1];
			for (Map.Entry e : map.entrySet()) {
				String k = encode(e.getKey(), keyType);
				String v = encode(e.getValue(), valueType);
				result.put(k, v);
			}
			return JSONObject.toJSONString(result);
		} else if (BaseEntity.class.isAssignableFrom(clazz)) {
			return ((BaseEntity) value).getfId();
		} else if (clazz.isPrimitive()) {
			return Objects.toString(value);
		} else if (String.class.equals(clazz)) {
			return Objects.toString(value);
		} else if (Date.class.isAssignableFrom(clazz)) {
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			return sdf.format(((Date) value));
		} else if (Number.class.isAssignableFrom(clazz)) {
			return Objects.toString(value);
		} else {
			return SerializableUtil.marshall(value);
		}
	}

	private String encode(Object target, PropertyDescriptor propertyDescriptor)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, SecurityException {

		Object o = propertyDescriptor.getReadMethod().invoke(target);

		return encode(o, propertyDescriptor.getPropertyType());
	}

}
