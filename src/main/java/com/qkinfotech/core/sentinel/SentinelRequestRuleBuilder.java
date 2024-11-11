package com.qkinfotech.core.sentinel;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

public class SentinelRequestRuleBuilder {
	/*
	 * FlowRule rule = new FlowRule(); rule.setResource("HelloWorld");
	 * rule.setGrade(RuleConstant.FLOW_GRADE_QPS); // Set limit QPS to 20.
	 * rule.setCount(20); rules.add(rule);
	 */
	
	private List<FlowRule> rules = new ArrayList<>();
	
	public FlowRuleBuilder requestMatchers(String pattern) {
		FlowRule rule = new FlowRule(pattern);
		
		rules.add(rule);
		
		return new FlowRuleBuilder(rule, this);
	}
	
	List<FlowRule> getRules() {
		return rules;
	}

}
