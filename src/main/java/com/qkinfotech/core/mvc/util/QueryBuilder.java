package com.qkinfotech.core.mvc.util;

import java.util.ArrayList;
import java.util.List;

import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.util.SpringUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;

import com.alibaba.fastjson2.JSONObject;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class QueryBuilder<T> {

	private boolean authorization = true;
	
	private Sort sort = null;
	
	private Pageable pageable = null;
	
	private Specification<T> specification = null;

	public static <T> QueryBuilder<T> parse(Class<T> modelClass, JSONObject body) {
		
		QueryBuilder<T> qb = new QueryBuilder<T>();
		
		if (body.containsKey("authorization")) {
			qb.authorization = body.getBooleanValue("authorization");

			boolean hasAcl = false;
			// 判断是否存在Acl
			String aclModelName = modelClass.getClass().getName() + "Acl";
			try {
				Class<?> aclClass = Class.forName(aclModelName);
				aclClass.newInstance();
				hasAcl = true;
			} catch (Exception e) {

			}
			if (hasAcl) {
				// 有Acl的情况下，查询条件需要增加
				// 用户相关组织架构有配置到read或all权限
				SimpleService aclService = (SimpleService) SpringUtil.getContext().getBean(aclModelName + "Service");


			}

		}
		
		if(body.containsKey("sort")) {
			List<Order> orders = new ArrayList<>();
			List<String> sorts = body.getList("sort", String.class);
			for(String item : sorts) {
				String[] parts = item.split("\\s+");
				if(parts.length == 1) {
					orders.add(Order.asc(parts[0]));
				} else if(parts.length == 2) {
					if(parts[1].equalsIgnoreCase("asc")) {
						orders.add(Order.asc(parts[0]));
					} else if(parts[1].equalsIgnoreCase("desc")) {
						orders.add(Order.desc(parts[0]));
					} else {
						throw new IllegalArgumentException("unknow sort order");
					}
				} else {
					throw new IllegalArgumentException("unknow sort expression");
				}
			}
			qb.sort = Sort.by(orders);
		} else {
			qb.sort = Sort.unsorted();
		}

		if(body.containsKey("pagesize")) {
			int pagesize = body.getIntValue("pagesize");
			if(pagesize > 500 || pagesize < 15) {
				pagesize = 15;
			}
			int pagenum = body.getIntValue("pagenum");
			if(pagenum < 0) {
				pagenum = 0;
			}
			qb.pageable = PageRequest.of(pagenum, pagesize, qb.sort);
		} else {
			//qb.pageable = Pageable.unpaged(qb.sort);
			qb.pageable = PageRequest.of(0, 500, qb.sort);
		}
		
		qb.specification = JSONQuerySpecification.getSpecification(body);
			
		return qb;
	}
	
	public Sort sort() {
		return sort;
	}

	public Pageable pageable() {
		return pageable;
	}

	public void setPageable(Pageable pageable) {
		if (pageable != null) {
			this.pageable = pageable;
		}
	}

	public Specification<T> specification() {
		return specification;
	}
	
	public boolean authorization() {
		return authorization;
	}
}
