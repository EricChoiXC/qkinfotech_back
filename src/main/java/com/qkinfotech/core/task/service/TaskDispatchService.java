package com.qkinfotech.core.task.service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.qkinfotech.core.jpa.convertor.SimpleCrudMethodMetadata;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.task.Task;
import com.qkinfotech.core.task.TaskWaitException;
import com.qkinfotech.core.task.datatype.TaskTrigger;
import com.qkinfotech.core.task.model.TaskHistory;
import com.qkinfotech.core.task.model.TaskLog;
import com.qkinfotech.core.task.model.TaskMain;
import com.qkinfotech.core.task.model.TaskStatus;
import com.qkinfotech.util.SpringUtil;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Service
@Transactional
public class TaskDispatchService {

	@Autowired
	SimpleService<TaskMain> taskMainService;
	
	@Autowired
	SimpleService<TaskHistory> taskHistoryService;

	@Autowired
	SimpleService<TaskLog> taskLogService;

	/**
	 * 获取可执行任务
	 * @return
	 */
	
	Specification<TaskMain> spec = (Specification<TaskMain>) (root, query, criteriaBuilder) -> {
		Subquery<Long> subQuery = query.subquery(Long.class);
		Root<TaskMain> subRoot = subQuery.from(TaskMain.class);

		return query.where(
			criteriaBuilder.equal(root.get("fStatus"), TaskStatus.WAIT),
			criteriaBuilder.or(
				criteriaBuilder.lessThanOrEqualTo(root.get("fLimit"), 0),
				criteriaBuilder.lessThanOrEqualTo(root.get("fLimit"), 
					subQuery.select(criteriaBuilder.count(root.get("fId"))).where(
						criteriaBuilder.equal(subRoot.get("fStatus"), TaskStatus.RUNNING),
						criteriaBuilder.isNotNull(subRoot.get("fGroup")),
						criteriaBuilder.equal(subRoot.get("fGroup"), root.get("fGroup"))))
				)
		)//.orderBy(criteriaBuilder.asc(root.get("fScheduledTime")))
		.getRestriction();
	};
	
	Pageable pageable = PageRequest.of(0, 1, Sort.by(Order.asc("fScheduledTime")));
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public TaskMain take() {
		
//		String sql = "from TaskMain t1 where t1.fStatus = :wait " +
//			     " and (t1.fLimit <= 0 or t1.fLimit <= (select count(t2.fId) from TaskMain t2 where t2.fStatus = :running and t2.fGroup is not null and t1.fGroup = t2.fGroup)) " +
//                 " order by t1.fScheduledTime";
//		Query query = taskMainService.createQuery(sql);
//		query.setParameter("wait", TaskStatus.WAIT);
//		query.setParameter("running", TaskStatus.RUNNING);
//		query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
//		query.setMaxResults(1);
//		query.setFirstResult(0);
//		
//		List<TaskMain> tasks = query.getResultList();

		taskMainService.getRepository().setRepositoryMethodMetadata(SimpleCrudMethodMetadata.LOCK);
		List<TaskMain> tasks = taskMainService.findAll(spec, pageable).toList();
		taskMainService.getRepository().setRepositoryMethodMetadata(SimpleCrudMethodMetadata.DEFAULT);
		

		for(TaskMain task : tasks) {
			Date time = task.getfScheduledTime();
			if (time != null && time.getTime() > System.currentTimeMillis() + 500) {
				throw new TaskWaitException(time.getTime() - System.currentTimeMillis());
			}
			task.setfStatus(TaskStatus.RUNNING);
			task.setfExecutionNode(SpringUtil.getNode());
			taskMainService.save(task);
			return task;
		}

		throw new TaskWaitException(60 * 1000);

	}

	/**
	 * 任务结束
	 * 
	 * @param task
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void finish(String fId, TriggerContext triggerContext) {
		TaskMain task = taskMainService.getById(fId);

		// 设置下次执行时间
		Date next = null;
		if (TaskStatus.ERROR.equals(task.getfStatus())) {
			if (task.getfAutoRetry() && !task.getfStockTask()) {
				int retries = task.getfNumOfRetries();
				if (retries < task.getfMaxNumOfRetries()) {
					task.setfNumOfRetries(retries + 1);
					next = new Date(System.currentTimeMillis() + 5l * 60 * 1000 * retries);
				} 
			}

		}
		if (next == null) {
			next = task.getfTaskTrigger().next(triggerContext);
		}

		if (next != null && next.getTime() >= System.currentTimeMillis()) {
			task.setfScheduledTime(next);
			task.setfStatus(TaskStatus.WAIT);
			task.setfExecutionNode(null);
			taskMainService.save(task);
		} else {
			TaskHistory history = new TaskHistory();
			history.from(task);
			taskHistoryService.save(history);
			taskMainService.delete(task);
		}
	}

	/**
	 * 初始化
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void reset() {
		{
			// 重置本节点运行中状态
			Specification<TaskMain> spec = (root, query, criteriaBuilder) -> {
					return query.where(
						criteriaBuilder.equal(root.get("fExecutionNode"), SpringUtil.getNode()),
						criteriaBuilder.notEqual(root.get("fStatus"), TaskStatus.WAIT)
					).getRestriction();
			};
	
			List<TaskMain> list = taskMainService.findAll(spec);
			TriggerContext triggerContext = new SimpleTriggerContext(Instant.now(),Instant.now(),Instant.now());
			for (TaskMain item : list) {
				if(TaskStatus.RUNNING.equals(item.getfStatus())) {
					fail(item.getfId());
				}
				finish(item.getfId(), triggerContext);
			}
		};
		
		{
			// 重置本节点运行中日志状态
			Specification<TaskLog> spec = (root, query, criteriaBuilder) -> {
				return query.where(
						criteriaBuilder.equal(root.get("fExecutionNode"), SpringUtil.getNode()),
						criteriaBuilder.equal(root.get("fStatus"), TaskStatus.RUNNING)
					).getRestriction();
			};
	
			List<TaskLog> list = taskLogService.findAll(spec);
			for (TaskLog item : list) {
				item.setfStatus(TaskStatus.ERROR);
				taskLogService.save(item);
			}
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void start(String fId) {
		TaskMain data = taskMainService.getById(fId);
		data.setfStatus(TaskStatus.RUNNING);
		data.setfExecutionNode(SpringUtil.getNode());
		taskMainService.save(data);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void success(String fId) {
		TaskMain data = taskMainService.getById(fId);
		data.setfStatus(TaskStatus.SUCCESS);
		taskMainService.save(data);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void fail(String fId) {
		TaskMain data = taskMainService.getById(fId);
		data.setfStatus(TaskStatus.ERROR);
		taskMainService.save(data);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sync() {
		List<TaskMain> tasks = findAllStockTask();
		
		String[] taskBeanNames = SpringUtil.getContext().getBeanNamesForAnnotation(Task.class);
		for (String taskBeanName : taskBeanNames) {
			if(!tasks.stream().anyMatch(o -> taskBeanName.equals(o.getfTaskBeanName()))) {
				Task ti = SpringUtil.getContext().findAnnotationOnBean(taskBeanName, Task.class);
				TaskMain task = new TaskMain();
				task.setfTaskBeanName(taskBeanName);
				if(StringUtils.hasText(ti.trigger())) {
					task.setfTaskTrigger(new TaskTrigger(ti.trigger()));
				} else {
					throw new IllegalArgumentException("trigger is null." + taskBeanName);
				}
				if(StringUtils.hasText(ti.group())) {
					task.setfGroup(ti.group());
				}
				if(StringUtils.hasText(ti.name())) {
					task.setfName(ti.name());
				}
				task.setfStockTask(true);
				task.setfCreateTime(new Date());
				TriggerContext triggerContext = new SimpleTriggerContext(Instant.now(), Instant.now(), Instant.now());
				task.setfScheduledTime(task.getfTaskTrigger().next(triggerContext));
				
				taskMainService.save(task);
			}
		}
	}

	public List<TaskMain> findAllRunningTaskWithGroup() {
		Specification<TaskMain> spec = (Specification<TaskMain>) (root, query, criteriaBuilder) -> {
			return query.where(
					criteriaBuilder.equal(root.get("fStatus"), TaskStatus.RUNNING),
					criteriaBuilder.isNotNull(root.get("fGroup"))
			).getRestriction();
		};
		
		return taskMainService.findAll(spec);
	}
	
	public List<TaskMain> findAllRunningTask() {
		Specification<TaskMain> spec = (Specification<TaskMain>) (root, query, criteriaBuilder) -> {
			return query.where(criteriaBuilder.equal(root.get("fStatus"), TaskStatus.RUNNING)).getRestriction();
		};
		
		return taskMainService.findAll(spec);
	}
	
	public List<TaskMain> findAllStockTask() {
		Specification<TaskMain> spec = (Specification<TaskMain>) (root, query, criteriaBuilder) -> {
			return query.where(criteriaBuilder.isTrue(root.get("fStockTask"))).getRestriction();
		};
		
		return taskMainService.findAll(spec);
	}
}
