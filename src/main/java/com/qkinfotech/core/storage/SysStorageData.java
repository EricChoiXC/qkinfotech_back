package com.qkinfotech.core.storage;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 存放 Chunk 的 MD5 信息。
 */
@Getter
@Setter
@Entity
@Table(name="sys_storage_data")
@SimpleModel(url="/sys/storage/data")
public class SysStorageData extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "f_sha", length = 254, nullable = false)
	private String fSHA;

	@Column(name = "f_md5", length = 32, nullable = false)
	private String fMD5;
	
	@Column(name = "f_size", nullable = false)
	private long fSize;

	@Column(name = "f_key", length = 254, nullable = false)
	private String fKey;

	@JoinColumn(name = "f_storage_id", nullable = false)
	@ManyToOne
	private SysStorage fSysStorage;

}
