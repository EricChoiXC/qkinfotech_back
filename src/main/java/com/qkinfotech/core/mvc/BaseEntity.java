package com.qkinfotech.core.mvc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.qkinfotech.util.IDGenerate;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

@MappedSuperclass
public class BaseEntity implements IBaseEntity, Serializable {

	private static final long serialVersionUID = 1L;

	@Id()
	@Column(name="f_id", length=36)
	@Access(AccessType.PROPERTY)
	protected String fId;
	
	public BaseEntity() {
	}
	
	public String getfId() {
		if(!StringUtils.hasText(fId)) {
			fId = IDGenerate.generate();
		}
		return fId;
	}

	public void setfId(String fId) {
//		if(StringUtils.hasText(fId)) {
			this.fId = fId;
//		}
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder(-426830461, 631494429);
		hcb.append(AopUtils.getTargetClass(this).getName());
		hcb.append(fId);
		return hcb.toHashCode();
	}
	
	@Override
	public String toString() {
		return JSONObject.toJSONString(this, JSONWriter.Feature.PrettyFormat);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!AopUtils.getTargetClass(obj).equals(AopUtils.getTargetClass(this)))
			return false;
		
		BaseEntity object = (BaseEntity) obj;
		
		return Objects.equals(object.getfId(), fId);
	}

	@Transient
	private Map<String, Object> featureMap = null;
	
	public Map<String, Object> getFeatureMap() {
		if(featureMap == null) {
			featureMap = new HashMap<String, Object>();
		}
		return featureMap;
	}
	
	@Transient
	protected Map<String, Object> customerDataMap = new HashMap<String, Object>();

	public Map<String, Object> getCustomerDataMap() {
		return customerDataMap;
	}

	public void setCustomerDataMap(Map<String, Object> customerDataMap) {
		this.customerDataMap = customerDataMap;
		
	}

	public void setCustomerData(String field, Object value) {
		customerDataMap.put(field, value);
		
	}

	public Object getCustomerData(String field) {
		return customerDataMap.get(field);
	}
}
