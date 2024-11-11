package com.qkinfotech.core.webservice;


import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.jws.WebService;


@Configuration
public class WebServiceConfig implements ApplicationListener<ApplicationEvent> {

	@Bean(name = "cxfServlet")
	public ServletRegistrationBean<CXFServlet> cxfServlet() {
		CXFServlet servlet = new CXFServlet();
		return new ServletRegistrationBean<CXFServlet>(servlet, "/webservice/*");
	}

	@Bean(name = Bus.DEFAULT_BUS_ID)
	public SpringBus springBus() {
		return new SpringBus();
	}

	@Override
	public void onApplicationEvent(ApplicationEvent ev) {
		if(ev instanceof ApplicationReadyEvent) {
			ApplicationReadyEvent event = (ApplicationReadyEvent)ev;
			if(event.getApplicationContext().getParent() == null) {
				SpringBus bus = springBus();
				Map<String, Object> services = event.getApplicationContext().getBeansWithAnnotation(WebService.class);
				
				for(String name : services.keySet()) {
					Object service = services.get(name);
					EndpointImpl endpoint = new EndpointImpl(bus, service);
					endpoint.publish("/api/" + name);
				}
			}
		}

	}

}
