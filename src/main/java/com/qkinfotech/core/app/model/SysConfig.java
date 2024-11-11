package com.qkinfotech.core.app.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "sys_config")
@Entity
@SimpleModel(url="sys/config")
public class SysConfig  extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

	@Column(name = "f_model_name", length = 32)
	private String fModelName;

	@Column(name = "f_property_name")
	private String fPropertyName;

	@Column(name = "f_property_value", length = Integer.MAX_VALUE)
	@Lob
	private String fPropertyValue;

}
