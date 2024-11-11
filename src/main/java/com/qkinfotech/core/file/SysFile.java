package com.qkinfotech.core.file;

import java.util.Date;
import java.util.Properties;

import com.qkinfotech.core.jpa.convertor.PropertyConverter;
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
@Table(name="sys_file")
@SimpleModel(url="/sys/file")
public class SysFile extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "f_file_name", length = 254)
	private String fFileName;

	@Column(name = "f_mime_type", length = 64)
	private String fMimeType;

	@Column(name = "f_size")
	private long fSize;

	@Column(name = "f_digest", length = 128)
	private String fDigest;

	@Column(name = "f_status")
	private int fStatus = 0;
	
	@Column(name = "f_create_time")
	public Date fCreateTime;
	
	@Lob
	@Column(name = "f_metadata", length = Integer.MAX_VALUE)
	@Convert(converter = PropertyConverter.class)
	private Properties fMetaData;
	
}
