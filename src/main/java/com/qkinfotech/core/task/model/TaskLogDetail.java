package com.qkinfotech.core.task.model;

import java.util.Date;

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
@Table(name = "task_log_detail")
@Entity
@SimpleModel(url="/task/log/detail")
public class TaskLogDetail extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

	@Column(name = "f_task_log_id", length = 32)
	private String fTaskLogId;
	
	@Column(name = "f_time")
	private Date fTime;
	
	@Column(name = "f_content", length = Integer.MAX_VALUE)
	@Lob
	private String fContent; 

}
