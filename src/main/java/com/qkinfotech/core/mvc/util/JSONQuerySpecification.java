package com.qkinfotech.core.mvc.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.BaseEntity;

public class JSONQuerySpecification<T> {

	private static <T> Predicate toPredicate(JSONObject conds, Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		if(conds.keySet().size() > 1) {
			throw new IllegalArgumentException("too many query conditions");
		} else if(conds.keySet().size() == 0) {
			throw new IllegalArgumentException("empty query specification");
		}
		
		String key = conds.keySet().iterator().next();
		Object data = conds.get(key);
		if(key.equals("any") || key.equals("or")) {
			if(data instanceof JSONArray ary) {
				List<Predicate> predicates = toPredicates(ary, root, query, criteriaBuilder);
				return criteriaBuilder.or(predicates.stream().toArray(Predicate[]::new));
			} else {
				throw new IllegalArgumentException("[or] expression needs array data");
			}
		}
		if(key.equals("all") || key.equals("and")) {
			if(data instanceof JSONArray ary) {
				List<Predicate> predicates = toPredicates(ary, root, query, criteriaBuilder);
				return criteriaBuilder.and(predicates.stream().toArray(Predicate[]::new));
			} else {
				throw new IllegalArgumentException("[and] expression needs array data");
			}
		}
		if(key.equals("not")) {
			if(data instanceof JSONObject obj) {
				return criteriaBuilder.not(toPredicate(obj, root, query, criteriaBuilder));
			} else {
				throw new IllegalArgumentException("[not] expression needs conditional data");
			}
		}
		Path<?> field = null;
		try {
			String attributeName = getField(data);
			if (attributeName.contains(".")) {
				Path<T> tempPath = root;
				while (attributeName.contains(".")) {
					tempPath = tempPath.get(attributeName.substring(0, attributeName.indexOf(".")));
					attributeName = attributeName.substring(attributeName.indexOf(".")+1);
				}
				field = tempPath.get(attributeName);
			} else {
				field = root.get(getField(data));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		switch(key) {
		case "eq":
			return criteriaBuilder.equal(field, getValue(field, data));
		case "neq":
			return criteriaBuilder.notEqual(field, getValue(field,data));
		case "lt":
			return criteriaBuilder.lessThan((Expression<? extends Comparable>) field, getValue(field, data));
		case "gt":
			return criteriaBuilder.greaterThan((Expression<? extends Comparable>) field, getValue(field, data));
		case "lteq":
			return criteriaBuilder.lessThanOrEqualTo((Expression<? extends Comparable>) field, getValue(field, data));
		case "gteq":
			return criteriaBuilder.greaterThanOrEqualTo((Expression<? extends Comparable>) field, getValue(field, data));
		case "contains":
			return criteriaBuilder.like((Expression<String>) field, "%" + getValue(field, data).toString() + "%");
		case "starts":
			return criteriaBuilder.like((Expression<String>) field, getValue(field, data) + "%");
		case "ends":
			return criteriaBuilder.like((Expression<String>) field, "%" + getValue(field, data));
		case "null":
			return criteriaBuilder.isNull(field);
		case "notnull":
			return criteriaBuilder.isNotNull(field);
		case "blank":
			return criteriaBuilder.or(criteriaBuilder.isNull(field), criteriaBuilder.equal(field, ""));
		case "notblank":
			return criteriaBuilder.and(criteriaBuilder.isNotNull(field), criteriaBuilder.notEqual(field, ""));
		case "in":
			if (data instanceof JSONObject) {
				Comparable comp = getValue(field, data);
				Gson gson = new Gson();
				String[] array = gson.fromJson(comp.toString(), String[].class);
				CriteriaBuilder.In<T> in = (CriteriaBuilder.In<T>) criteriaBuilder.in(field);
				for (String str : array) {
					in.value((T) str);
				}
				return in;
			} else {
				return criteriaBuilder.in(root.get(data.toString())).in((Collection<?>)getValue(field, data));
			}
		default:
			throw new IllegalArgumentException("unkndow condition:[" + key +"]");
		}
	}
	
	private static Comparable getValue(Path<?> field, Object data) {
		if(data instanceof JSONObject json) {
			String key;
			if(json.keySet().size() == 1) {
				key = json.keySet().iterator().next();
			} else {
				throw new IllegalArgumentException(data.toString());
			}
			Class<?> clazz = field.getJavaType();
			if(clazz.equals(String.class)) {
				return json.getString(key);
			} else if(clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
				return json.getBoolean(key);
			} else if(clazz.equals(byte.class) || clazz.equals(Byte.class)) {
				return json.getBoolean(key);
			} else if(clazz.equals(short.class) || clazz.equals(Short.class)) {
				return json.getShort(key);
			} else if(clazz.equals(int.class) || clazz.equals(Integer.class)) {
				return json.getInteger(key);
			} else if(clazz.equals(long.class) || clazz.equals(Long.class)) {
				return json.getLong(key);
			} else if(clazz.equals(float.class) || clazz.equals(Float.class)) {
				return json.getFloat(key);
			} else if(clazz.equals(double.class) || clazz.equals(Double.class)) {
				return json.getDouble(key);
			} else if(clazz.equals(BigDecimal.class)) {
				return json.getBigDecimal(key);
			} else if(clazz.equals(BigInteger.class)) {
				return json.getBigInteger(key);
			} else if(clazz.equals(Date.class) || clazz.equals(Timestamp.class)) {
				return json.getDate(key);
			}
		}
		throw new IllegalArgumentException(data.toString());
	}

	private static String getField(Object data) {
		if(data instanceof JSONObject json) {
			if(json.keySet().size() == 1) {
				return json.keySet().iterator().next();
			}
		} else if (data instanceof String) {
			return (String) data;
		}
		return null;
	}

	private static <T> List<Predicate> toPredicates(JSONArray conds, Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		List<Predicate> list = new ArrayList<>();
		
		for(int i=0; i < conds.size(); ++ i) {
			if(conds.get(i) instanceof JSONArray ary) {
				list.addAll(toPredicates(ary, root, query, criteriaBuilder));
			} else if(conds.get(i) instanceof JSONObject obj) {
				list.add(toPredicate(obj, root, query, criteriaBuilder));
			} else {
				throw new IllegalArgumentException("unknow query specification");
			}
		}
		return list;
	}

	public static <T> Specification<T> getSpecification(JSONObject body) {
		
		Specification<T> spec = (root,query, criteriaBuilder) -> {
			if(body.containsKey("distinct")){
				query.distinct(true);
				body.remove("distinct");
			}
			if(body.containsKey("query")) {
				Predicate predicate = null;
				if(body.get("query") instanceof JSONArray ary) {
					List<Predicate> predicates = toPredicates(ary, root, query, criteriaBuilder);
					predicate = criteriaBuilder.and(predicates.stream().toArray(Predicate[]::new));
				} else if(body.get("query") instanceof JSONObject obj) {
					predicate = toPredicate(obj, root, query, criteriaBuilder);
				} else {
					throw new IllegalArgumentException("unknow query specification");
				}
				return query.where(predicate).getRestriction();
				
			} else {
				return query.getRestriction();
			}
		};
		return spec;
	}
}