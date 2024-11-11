package com.qkinfotech.core.security;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.sentinel.SentinelRequestRuleBuilder;
import com.qkinfotech.core.user.model.SysAuthority;
import com.qkinfotech.util.IDGenerate;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public abstract class BaseModule implements IModuleSecurity, ApplicationContextAware, ApplicationRunner  {
	
	protected ConfigurableApplicationContext applicationContext;

	public void configAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
		
	}

	public void configSecurityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, UserDetailsService userDetailsService) {
		
	}

	public void configSentinel(SentinelRequestRuleBuilder sentinelRequestRuleBuilder) {
		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext)applicationContext;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		syncAuthorities(authorities(), module(), group());
		
	}
	
	protected void syncAuthorities(List<String> authorities, String module, String group) {
		
		if(CollectionUtils.isEmpty(authorities) || !StringUtils.hasText(module) || !StringUtils.hasText(group)) {
			return;
		}
		
		SimpleService<SysAuthority> sysAuthorityService = (SimpleService<SysAuthority>)applicationContext.getBean("sysAuthorityService");

		Specification<SysAuthority> spec = new Specification<SysAuthority>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<SysAuthority> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return query.where(
					cb.equal(root.get("fModule"), module),
					cb.equal(root.get("fGroup"), group)).getRestriction();
			}
		};
		
		List<SysAuthority> list = sysAuthorityService.findAll(spec);
		for(SysAuthority authority : list) {
			if(!authorities.contains(authority.getfName())){
				sysAuthorityService.delete(authority);
			}
		}
		for(String authority : authorities) {
			if(!list.stream().filter(r -> authority.equals(r.getfName())).findFirst().isPresent()) {
				sysAuthorityService.save(new SysAuthority(IDGenerate.generate(), authority, module, group));
			}
		}		
	}

	public String group() {
		return null;
	}

	public String module() {
		return null;
	}

	public List<String> authorities() {
		return null;
	}
}
