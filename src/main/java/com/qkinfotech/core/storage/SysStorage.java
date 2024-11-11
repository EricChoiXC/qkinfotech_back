package com.qkinfotech.core.storage;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.jpa.convertor.JSONObjectConverter;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="sys_storage")
@SimpleModel(url="/sys/storage")
public class SysStorage extends BaseEntity{

	private static final long serialVersionUID = 1L;

	@Column(name = "f_name", length = 254)
	private String fName;

	@Column(name = "f_class_name", length = 254)
	private String fClassName;

	@Column(name = "f_default")
	private Boolean fDefault;
	
	@Lob
	@Column(name = "f_config", length = Integer.MAX_VALUE)
	@Convert(converter = JSONObjectConverter.class)
	private JSONObject fConfig;

}
