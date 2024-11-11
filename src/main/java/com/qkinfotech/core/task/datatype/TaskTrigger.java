package com.qkinfotech.core.task.datatype;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.StringUtils;

public class TaskTrigger implements Serializable {

	private static final long serialVersionUID = 1L;

	private String trigger;

	public TaskTrigger(String trigger) {

		this.trigger = trigger;

		if (trigger == null || !StringUtils.hasText(trigger)) {
			this.trigger = null;
		} else	if (trigger.startsWith("cron:")) {

		} else if (trigger.startsWith("frequency:")) {

		} else if (trigger.startsWith("interval:")) {

		} else if (trigger.startsWith("onetime:")) {

		} else {
			throw new RuntimeException("unknow trigger expression");
		}
	}

	public Date next(TriggerContext triggerContext) {
		if (trigger == null || trigger.startsWith("onetime:")) {
			return null;
		} else if (trigger.startsWith("frequency:")) {
			long ms = 1000l * Long.parseLong(trigger.substring(10));
			
			Instant instant = triggerContext.lastActualExecution();
			if(instant == null) {
				return new Date((long) Math.ceil((System.currentTimeMillis() + ms) / ms) * ms);
			} else {
				return Date.from(instant.plusMillis(ms));
			}
		} else if (trigger.startsWith("interval:")) {
			long ms = 1000 * Long.parseLong(trigger.substring(9));
			return new Date(System.currentTimeMillis() + ms);
		} else if (trigger.startsWith("cron:")) {
			CronTrigger t = new CronTrigger(trigger.substring(5));
			Instant instant = t.nextExecution(triggerContext);
			return (instant != null ? Date.from(instant) : null);
		}
		return null;
	}

	public String getTrigger() {
		return trigger;
	}
	
	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}

}
