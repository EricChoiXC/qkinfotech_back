package com.qkinfotech.core.mvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.Json2Bean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson2.JSONObject;

import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

@Transactional(propagation=Propagation.REQUIRED)
@DependsOn("entityManager")
public class SimpleService<T extends BaseEntity> implements InitializingBean {
	
	protected SimpleRepository<T> repository; 
	
	protected Class<T> entityClass;
	
	@Autowired
	ApplicationContext context;

	@Autowired
	protected Bean2Json bean2json;

	@Autowired
	protected Json2Bean json2bean;
	
	@Autowired
    private PlatformTransactionManager transactionManager;
	
	@Autowired(required = false)
	protected List<IEntityExtension> extensions; 

	public SimpleService(SimpleRepository<T> repository) {
		this.repository = repository;
		this.entityClass = repository.getEntityClass();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
//		repository = (SimpleRepository<T>) context.getBean(StringUtils.uncapitalize(entityClass.getSimpleName()) + "Repository");
//		if(repository == null) {
//			throw new RuntimeException("rerpository is null");
//		}
	}

	public Class<T> getEntityClass() {
		return entityClass;
	}

	public void save(T entity) {
		JSONObject savedData = new JSONObject();
		T model = null;
		if(extensions != null) {
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
	        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
	        
	        TransactionStatus status = transactionManager.getTransaction(def);
	        try {
				model = repository.getById(entity.getfId());
				if(model == null ) {
					for(int i = 0; i < extensions.size(); ++ i) {
						IEntityExtension extension = extensions.get(i);
						extension.adding(entity);
					}
				} else {
					for(int i = 0; i < extensions.size(); ++ i) {
						IEntityExtension extension = extensions.get(i);
						savedData = bean2json.toJson(entity);
						extension.updating(model, savedData);
					}
				}
	        } catch (RuntimeException ex) {
	            throw ex;
	        } finally {
	            transactionManager.rollback(status);
	        }
		}
		repository.save(entity);
		if(extensions != null) {
			if(model == null) {
				for(int i = extensions.size() - 1; i >= 0; -- i) {
					IEntityExtension extension = extensions.get(i);
					extension.added(entity);
				}
			} else {
				for(int i = 0; i < extensions.size(); ++ i) {
					IEntityExtension extension = extensions.get(i);
					extension.updated(entity, savedData);
				}
			}
		}
	}

	public void delete(T entity) {
		if(extensions != null) {
			for(int i = 0; i < extensions.size(); ++ i) {
				IEntityExtension extension = extensions.get(i);
				extension.deleting(entity);
			}
		}
		repository.delete(entity);
		if(extensions != null) {
			for(int i = extensions.size() - 1; i >= 0; -- i) {
				IEntityExtension extension = extensions.get(i);
				extension.deleted(entity);
			}
		}
	}

	public void delete(T[] entities) {
		for(T entry : entities) {
			delete(entry);
		}
	}

	public void delete(String fId) {
		T model = repository.getById(fId);
		if(model != null) {
			delete(model);
		}
	}

	public void delete(String[] fIds) {
		for(String fId : fIds) {
			delete(fId);
		}
	}

	public void delete(Specification<T> spec) {
		scroll(spec, (model) -> {
			delete(model);
		});
	}
	
	public List<T> findAll() {
		return repository.findAll();
	}

	public List<T> findAll(Specification<T> spec) {
		return repository.findAll(spec);
	}

	public List<T> findAll(Specification<T> spec, Sort sort) {
		return repository.findAll(spec, sort);
	}
	
	public Page<T> findAll(Specification<T> spec, Pageable pageable) {
		return repository.findAll(spec, pageable);
	}
	
	public T findOne(Specification<T> spec, Sort sort) {
		Optional<T> result = repository.findOne(spec, sort);
		return result.isEmpty()? null : result.get();
	}

	public T findOne(Specification<T> spec) {
		Optional<T> result = repository.findOne(spec);
		return result.isEmpty()? null : result.get();
	}


	public void scroll(Specification<T> spec, Consumer<T> consumer) {
		scroll(spec, Sort.unsorted(), consumer);
	}
	
	public void scroll(Specification<T> spec, Sort sort, Consumer<T> consumer) {
		repository.scroll(spec, sort, consumer);
	}
	
	public T getById(String fId) {
		return repository.getById(fId);
	}

	public void flush() {
		repository.flush();
	}
	
	public SimpleRepository<T> getRepository() {
		return repository;
	}

	public void setRepository(SimpleRepository<T> repository) {
		this.repository = repository;
	}
	
	public Query createQuery(String query) {
		return repository.getEntityManager().createQuery(query);
	}
	
	public void lock(T d) {
		repository.lock(d);
	}

	public void lock(T d, LockModeType lockModeType, Map<String, Object> properties) {
		repository.lock(d, LockModeType.WRITE, properties);
	}


}
