package com.qkinfotech.core.mvc.models;

import java.util.Date;

import com.qkinfotech.core.user.model.SysUser;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class LogicDeleteField {

	@Column(name="f_delete_user", length = 36) 
    protected SysUser fDeleteUser;

	@Column(name="f_delete_time") 
    protected Date fDeleteTime;

	public SysUser getfDeleteUser() {
		return fDeleteUser;
	}

	public void setfDeleteUser(SysUser fDeleteUser) {
		this.fDeleteUser = fDeleteUser;
	}

	public Date getfDeleteTime() {
		return fDeleteTime;
	}

	public void setfDeleteTime(Date fDeleteTime) {
		this.fDeleteTime = fDeleteTime;
	}

	
}
