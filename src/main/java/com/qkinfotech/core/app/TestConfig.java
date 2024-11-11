package com.qkinfotech.core.app;

import java.util.Date;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.qkinfotech.core.app.config.AppConfig;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
//@Lazy
public class TestConfig extends AppConfig{
	
//	public TestConfig(SimpleService<SysConfig> sysConfigService) {
//		super(sysConfigService);
//		// TODO Auto-generated constructor stub
//	}


	private int age = 10;

	private String name = "111";
	
	private Date date = new Date();
	
	
	public static void main(String[] args) {
		TestConfig c = new TestConfig();
		c.commit();
		
	}


	@Override
	public String getModelName() {
		return "TestConfig";
	}
	

}
