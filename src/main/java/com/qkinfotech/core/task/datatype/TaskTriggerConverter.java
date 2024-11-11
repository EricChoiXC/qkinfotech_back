package com.qkinfotech.core.task.datatype;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TaskTriggerConverter implements AttributeConverter<TaskTrigger, String> {

	@Override
	public String convertToDatabaseColumn(TaskTrigger taskTrigger) {
		return taskTrigger.getTrigger();
	}

	@Override
	public TaskTrigger convertToEntityAttribute(String data) {
		return new TaskTrigger(data);
	}

}
