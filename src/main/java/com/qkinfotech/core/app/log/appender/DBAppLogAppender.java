package com.qkinfotech.core.app.log.appender;

import org.springframework.stereotype.Component;

import com.qkinfotech.core.app.log.AppLogAppender;
import com.qkinfotech.core.app.log.AppLogData;

import jakarta.persistence.EntityManager;

@Component
public class DBAppLogAppender implements AppLogAppender {

	private EntityManager em;
	
	public DBAppLogAppender(EntityManager em){
		this.em = em;
	}

	@Override
	public void write(AppLogData data) {
		em.persist(data);
	}
	
}
