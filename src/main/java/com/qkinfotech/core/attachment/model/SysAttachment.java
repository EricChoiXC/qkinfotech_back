package com.qkinfotech.core.attachment.model;

import java.util.Date;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.file.SysFile;
import com.qkinfotech.core.jpa.convertor.JSONObjectConverter;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.user.model.SysUser;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name="sys_attachment")
@Entity
public class SysAttachment extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

	@Column(name = "f_doc_class", length = 64)
	private String fDocClass;
	
	@Column(name = "f_doc_id", length = 64)
	private String fDocId;
	
	@Column(name = "f_att_key", length = 128)
	private String fAttKey;
	
	@Column(name = "f_order")
	private int fOrder;

	@JoinColumn(name = "f_creator_id")
	@ManyToOne
	private SysUser fCreator;

	@Column(name = "f_create_time")
	private Date fCreateTime;
	
	@JoinColumn(name = "f_file_id")
	@ManyToOne
	private SysFile fFile;
	
	@Lob
	@Column(name = "f_props", length = Integer.MAX_VALUE)
	@Convert(converter = JSONObjectConverter.class)
	private JSONObject fProps;
	
}
