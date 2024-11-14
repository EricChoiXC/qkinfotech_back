package com.qkinfotech.core.mvc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson2.JSONArray;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.org.model.OrgElement;
import com.qkinfotech.core.sys.base.model.AccessControl;
import com.qkinfotech.util.OrganizationUtil;
import com.qkinfotech.util.SpringUtil;
import jakarta.persistence.criteria.Predicate;
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
		}

		boolean hasAcl = false;
		// 判断是否存在Acl
		String aclModelName = modelClass.getName() + "Acl";
		try {
			Class<?> aclClass = Class.forName(aclModelName);
			aclClass.newInstance();
			hasAcl = true;
			if (hasAcl) {
				// 有Acl的情况下，查询条件需要增加
				// 用户相关组织架构有配置到read或all权限
				String upperBeanName = aclModelName.substring(aclModelName.lastIndexOf(".") + 1) + "Service";
				String aclBeanName = upperBeanName.substring(0, 1).toLowerCase() + upperBeanName.substring(1);
				SimpleService aclService = (SimpleService) SpringUtil.getContext().getBean(aclBeanName);
				OrganizationUtil organizationUtil = new OrganizationUtil();
				Set<OrgElement> orgElements = organizationUtil.getOrgElements(body.getString("userId"));
				Specification spec = new Specification() {
					@Override
					public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder criteriaBuilder) {
						CriteriaBuilder.In in1 = criteriaBuilder.in(root.get("fAuth"));
						in1.value("read");
						in1.value("all");
						CriteriaBuilder.In in2 = criteriaBuilder.in(root.get("fOrg").get("fId"));
						for (OrgElement element : orgElements) {
							in2.value(element.getfId());
						}
						return criteriaBuilder.and(in1, in2);
					}
				};
				List<AccessControl> aclList = aclService.findAll(spec);
				String[] docIds = new String[aclList.size()];
				for (int i = 0; i < aclList.size(); i++) {
					docIds[i] = aclList.get(i).getfId();
				}
				JSONArray and = new JSONArray();
				JSONObject in = new JSONObject();
				JSONObject q = new JSONObject();
				in.put("fId", docIds);
				q.put("in", in);
				and.add(q);
				if (body.containsKey("query")) {
					Object query = body.get("query");
					if (query instanceof JSONObject js) {
						and.add(js);
					} else if (query instanceof JSONArray ja) {
						and.addAll(ja);
					}
				}
				JSONObject query = new JSONObject();
				query.put("and", and);
				body.put("query", query);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
