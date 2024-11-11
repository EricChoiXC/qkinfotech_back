package com.qkinfotech.core.sentinel;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.qkinfotech.core.security.BaseModule;

@Configuration("module:sys.sentinel")
public class ModuleConfiguration extends BaseModule {

	@Bean
	public SentinelFilter sentinelFilter() {
		return new SentinelFilter();
	}
	
	@Bean
	public FilterRegistrationBean<OrderedFilter> orderFilter() {
	    FilterRegistrationBean<OrderedFilter> filter = new FilterRegistrationBean<>();
	    filter.setName("SentinelFilter");
	    filter.setFilter(sentinelFilter());
	    filter.setOrder(Integer.MIN_VALUE);
	    return filter;
	}

}
