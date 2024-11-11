package com.qkinfotech.core.jpa;


import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;

public abstract class AbstractListener {

	@Autowired
	private EntityManagerFactory emf;

	@PostConstruct
	public void registerListeners() {
		
		SessionFactoryImplementor sessionFactory = emf.unwrap(SessionFactoryImplementor.class);
		ServiceRegistry serviceRegistry = sessionFactory.getServiceRegistry();
		
		
		EventListenerRegistry registry = serviceRegistry.getService(EventListenerRegistry.class);
		
		if(this instanceof PostLoadEventListener) {
			registry.getEventListenerGroup(EventType.POST_LOAD).appendListener((PostLoadEventListener) this);
		}
		
		register(registry);
		
	}

	protected abstract void register(EventListenerRegistry registry);


}
