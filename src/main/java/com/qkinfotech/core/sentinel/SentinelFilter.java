package com.qkinfotech.core.sentinel;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.util.AntPathMatcher;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.qkinfotech.core.security.IModuleSecurity;
import com.qkinfotech.util.SpringUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SentinelFilter implements Filter, OrderedFilter, ApplicationRunner{
	
	Set<String> pathPatterns = new HashSet<>();
	
	AntPathMatcher pathMatcher = new AntPathMatcher();

	@Override
	public int getOrder() {
		return Integer.MIN_VALUE;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		if(request instanceof HttpServletRequest) {
			try {
				String uri = ((HttpServletRequest)request).getRequestURI();
				for(String pathPattern : pathPatterns) {
					if(pathMatcher.match(pathPattern, uri)) {
						Entry entry = SphU.entry(pathPattern);
						break;
					}
				}
				chain.doFilter(request, response);
				
			} catch (BlockException e) {
				((HttpServletResponse)response).setStatus(503);
				e.printStackTrace(); // 输出 Blocked
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	private void setup(ApplicationContext applicationContext) throws Exception {
	    SentinelRequestRuleBuilder builder = new SentinelRequestRuleBuilder();
	    
	    Map<String, IModuleSecurity> moduleBeans = applicationContext.getBeansOfType(IModuleSecurity.class);
	    
	    moduleBeans.values().stream().forEach(o -> o.configSentinel(builder));
	    
	    List<FlowRule> rules = builder.getRules();
	    
	    for(FlowRule rule : rules) {
	    	logger.debug(rule.toString());
	    	pathPatterns.add(rule.getResource());
	    }
	    
	    FlowRuleManager.loadRules(rules);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		logger.debug("sentinel filter initializing...");
		setup(SpringUtil.getContext());
		logger.debug("sentinel filter initialized");
	}
}
