package com.qkinfotech.core.i18n.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="sys_message")
@SimpleModel(url="/sys/message")
public class SysMessage extends BaseEntity {
	
	@Column(name="f_key", length=128)	
	String fKey;
	
	@Column(name="f_value", length=2000)	
	String fValue;
	
	@Column(name="f_locale", length=16)	
	String fLocale;

}
