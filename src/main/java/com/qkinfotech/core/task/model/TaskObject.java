package com.qkinfotech.core.task.model;

import java.util.Date;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.task.datatype.JSONObjectConverter;
import com.qkinfotech.core.task.datatype.TaskTrigger;
import com.qkinfotech.core.task.datatype.TaskTriggerConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class TaskObject extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "f_name", length = 32)
	private String fName; // 任务名
	
//	@Column(name = "f_relation_key", length = 128)
//	private String fRelationKey; // 任务关联信息
	
	@Column(name = "f_group", length = 32)
	private String fGroup; // 任务组名

	@Column(name = "f_limit", nullable=false)
	private int fLimit = 0; // 组内并发限制
	
	@Column(name = "f_scheduled_time", nullable = false)
	private Date fScheduledTime; // 任务启动时间

	@Column(name = "f_status", length = 1, nullable=false)
	private String fStatus = TaskStatus.WAIT; // 任务状态

	@Column(name = "f_execution_node", length = 32)
	private String fExecutionNode; // 任务执行节点
	
//	@Column(name = "f_dispatch_node", length = 16)
//	private String fDispatchNode; // 调度节点
//
//	@Column(name = "f_dispatch_time")
//	private Date fDispatchTime; // 调度时间

	@Column(name = "f_task_bean_name", length = 64, nullable=false)
	private String fTaskBeanName; // 任务Bean

	@Column(name = "f_task_parameter", length = Integer.MAX_VALUE)
	@Lob
	@Convert(converter = JSONObjectConverter.class)
	private JSONObject fTaskParameter; // 任务参数
	
	@Column(name = "f_auto_retry", nullable=false)
	private boolean fAutoRetry = false; // 是否重试

	@Column(name = "f_max_num_of_retries", length=8, nullable=false)
	private int fMaxNumOfRetries = 5; // 最大重试次数

	@Column(name = "f_num_of_retries", length=8, nullable=false)
	private int fNumOfRetries = 0; // 重试次数

	@Column(name = "f_task_trigger", length = 128)
	@Convert(converter = TaskTriggerConverter.class)
	private TaskTrigger fTaskTrigger; // 触发类型
	
	@Column(name = "f_create_time")
	private Date fCreateTime; // 创建时间

	@Column(name = "f_stock_task", nullable=false)
	private boolean fStockTask = false; // 系统任务


}
