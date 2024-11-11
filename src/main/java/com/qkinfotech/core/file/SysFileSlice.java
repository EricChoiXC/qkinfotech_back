package com.qkinfotech.core.file;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.storage.SysStorageData;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="sys_file_slice")
@SimpleModel(url="/sys/file/slice")
public class SysFileSlice extends BaseEntity{

	private static final long serialVersionUID = 1L;

	@JoinColumn(name = "f_sys_file_id")
	@ManyToOne
	private SysFile fSysFile;

	@Column(name = "f_start")
	private long fStart;

	@Column(name = "f_size")
	private int fSize;

	@JoinColumn(name = "f_storage_data_id")
	@ManyToOne
	private SysStorageData fSysStorageData;

}
