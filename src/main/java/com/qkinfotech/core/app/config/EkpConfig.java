package com.qkinfotech.core.app.config;

import org.springframework.stereotype.Component;

import com.qkinfotech.core.mvc.SimpleModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class EkpConfig extends AppConfig{

	private String ekpUrl;
	private String ekpCookie;
	private String ekpDatabaseType;
	private String ekpDatabaseUrl;
	private String ekpDatabaseUsername;
	private String ekpDatabasePassword;

	private String ekpProjectApprovalTemplateId;
	private String ekpIsoApprovalTemplateId;
	
	public static void main(String[] args) {
		EkpConfig c = new EkpConfig();
		c.commit();
	}
	
	@Override
	public String getModelName() {
		return "ekpConfig";
	}
	
}
