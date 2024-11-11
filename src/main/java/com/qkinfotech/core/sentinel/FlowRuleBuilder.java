package com.qkinfotech.core.sentinel;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

public class FlowRuleBuilder extends FlowRule {
	
	private SentinelRequestRuleBuilder sentinelRequestRuleBuilder;
	
	private FlowRule flowRule;
	
	public AbstractRule setId(Long id) {
		return flowRule.setId(id);
	}

	public AbstractRule setLimitApp(String limitApp) {
		return flowRule.setLimitApp(limitApp);
	}

	public AbstractRule setRegex(boolean regex) {
		return flowRule.setRegex(regex);
	}

	public FlowRule setControlBehavior(int controlBehavior) {
		return flowRule.setControlBehavior(controlBehavior);
	}

	public FlowRule setMaxQueueingTimeMs(int maxQueueingTimeMs) {
		return flowRule.setMaxQueueingTimeMs(maxQueueingTimeMs);
	}

	public FlowRule setWarmUpPeriodSec(int warmUpPeriodSec) {
		return flowRule.setWarmUpPeriodSec(warmUpPeriodSec);
	}

	public FlowRule setGrade(int grade) {
		return flowRule.setGrade(grade);
	}

	public FlowRule setCount(double count) {
		return flowRule.setCount(count);
	}

	public FlowRule setStrategy(int strategy) {
		return flowRule.setStrategy(strategy);
	}

	public FlowRule setRefResource(String refResource) {
		return flowRule.setRefResource(refResource);
	}

	public FlowRule setClusterMode(boolean clusterMode) {
		return flowRule.setClusterMode(clusterMode);
	}

	public FlowRule setClusterConfig(ClusterFlowConfig clusterConfig) {
		return flowRule.setClusterConfig(clusterConfig);
	}

	FlowRuleBuilder(FlowRule flowRule, SentinelRequestRuleBuilder sentinelRequestRuleBuilder) {
		this.flowRule = flowRule;
		this.sentinelRequestRuleBuilder = sentinelRequestRuleBuilder;
	}
	
	public SentinelRequestRuleBuilder and() {
		return sentinelRequestRuleBuilder;
	}

}
