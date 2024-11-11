package com.qkinfotech.core.app.model;

import java.util.Date;

import com.qkinfotech.core.app.log.AppLogData;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "sys_login_log")
@Entity
@SimpleModel(url="sys/login/log")
public class SysLoginLog extends BaseEntity implements AppLogData {

	private static final long serialVersionUID = 1L;

	private String fServer;
	
	private Date fTimestamp;
	
	private String fThrread;

	private String fUsername;
	
	private String fDevice;

	private boolean fSuccess;
	
	private String fIpAddress;

	@Override
	public void setup(String server, Date timestamp, String thread) {
		this.fServer = server;
		this.fTimestamp = timestamp;
		this.fThrread = thread;
	}
	
}
