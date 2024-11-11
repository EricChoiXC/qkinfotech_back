package com.qkinfotech.core.task.model;

import com.qkinfotech.core.mvc.SimpleModel;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "task_main")
@Entity
@SimpleModel(url="/task")
public class TaskMain extends TaskObject {
	
	private static final long serialVersionUID = 1L;

}
