package com.qkinfotech.core.mvc.models;

import com.qkinfotech.core.mvc.IBaseEntity;
import com.qkinfotech.core.user.model.SysUser;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public interface IUserMark extends IBaseEntity {

    final String fCreateUser = "f_create_user";

    final String fModifyUser = "f_modify_user";


	@ManyToOne(targetEntity = SysUser.class)
	@JoinColumn(name = "f_create_user_id")
	default SysUser getfCreateUser() {
		return (SysUser)getFeatureMap().get(IUserMark.fCreateUser);
	}

	default void setfCreateUser(SysUser fCreateUser) {
		getFeatureMap().put(IUserMark.fCreateUser, fCreateUser);
	}

	@ManyToOne(targetEntity = SysUser.class)
	@JoinColumn(name = "f_modify_user_id")
	default SysUser getfModifyUser() {
		return (SysUser)getFeatureMap().get(IUserMark.fModifyUser);
	}

	default void setfModifyUser(SysUser fModifyUser) {
		getFeatureMap().put(IUserMark.fModifyUser, fModifyUser);
	}

	
}
