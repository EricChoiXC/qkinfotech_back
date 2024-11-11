package com.qkinfotech.core.mvc.models;

import java.util.Date;

import com.qkinfotech.core.mvc.IBaseEntity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;

public interface ITimeMark extends IBaseEntity {
	
    final String fCreateTime = "f_create_time";

    final String fModifyTime = "f_modify_time";

	@Column(name="f_create_time")
	@Access(AccessType.PROPERTY)
	default Date getfCreateTime() {
		return (Date)getFeatureMap().get(ITimeMark.fCreateTime);
	}

	default void setfCreateTime(Date fCreateTime) {
		this.getFeatureMap().put(ITimeMark.fCreateTime, fCreateTime);
	}

	@Column(name="f_modify_time")
	@Access(AccessType.PROPERTY)
	default Date getfModifyTime() {
		return (Date)getFeatureMap().get(ITimeMark.fModifyTime);
	}

	default void setfModifyTime(Date fModifyTime) {
		this.getFeatureMap().put(ITimeMark.fModifyTime, fModifyTime);
	}
	
}
