package com.qkinfotech.core.task.model;

import java.util.Date;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "task_log")
@Entity
@SimpleModel(url="/task/log")
public class TaskLog extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

	@Column(name = "f_task_id", length = 32)
	private String fTaskId;

	@Column(name = "f_start_time")
	private Date fStartTime;

	@Column(name = "f_end_time")
	private Date fEndTime;
	
	@Column(name = "f_execution_node", length = 128)
	private String fExecutionNode; 

	@Column(name = "f_status", length = 1)
	private String fStatus; 

}
