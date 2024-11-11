package com.qkinfotech.core.task.model;

import org.springframework.beans.BeanUtils;

import com.qkinfotech.core.mvc.SimpleModel;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "task_history")
@Entity
@SimpleModel(url="/task/history")
public class TaskHistory extends TaskObject {
	
	private static final long serialVersionUID = 1L;
	
	public void from(TaskMain task) {
		BeanUtils.copyProperties(task, this);
	}

}
