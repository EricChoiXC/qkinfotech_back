package com.qkinfotech.core.mvc;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.QueryHints;
import org.springframework.data.jpa.repository.support.QueryHints.NoHints;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import com.qkinfotech.core.jpa.convertor.SimpleCrudMethodMetadata;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class SimpleRepository<T extends BaseEntity> extends SimpleJpaRepository<T, String> {
	
	protected EntityManager em;
	
	protected JpaEntityInformation<T, ?> entityInformation;
	
	protected Class<T> entityClass;
	
	public SimpleRepository(Class<T> clazz, EntityManager em) {
		super(clazz, em);
		this.em = em;
		this.entityInformation = JpaEntityInformationSupport.getEntityInformation(clazz, em);
		this.setRepositoryMethodMetadata(SimpleCrudMethodMetadata.DEFAULT); 
		this.entityClass = clazz;
	}

	public Class<T> getEntityClass() {
		return entityClass;
	}
	
	protected QueryHints getQueryHints() {
		return NoHints.INSTANCE;
	}

	protected QueryHints getQueryHintsForCount() {
		return NoHints.INSTANCE;
	}
	
	public void deleteAllById(String[] ids) {
		deleteAllById(Arrays.asList(ids));
	}

	public T getById(String id) {
		return em.find(getDomainClass(), id);
	}

	public JpaEntityInformation<T, ?> getEntityInformation() {
		return entityInformation;
	}

	public EntityManager getEntityManager() {
		return em;
	}

	public void scroll(Specification<T> spec, Consumer<T> consumer) {
		scroll(spec, Sort.unsorted(), consumer);
	}
	
	public void scroll(Specification<T> spec, Sort sort, Consumer<T> consumer) {
		Query query = (Query)getQuery(spec, sort);
		ScrollableResults<T> result = query.scroll(ScrollMode.FORWARD_ONLY);
		try {
			while(result.next()) {
				T obj = result.get();
				consumer.accept(obj);
			}
		} finally {
			result.close();
		}
	}

	@Override
	public TypedQuery<T> getQuery(Specification<T> spec, Pageable pageable) {
		return super.getQuery(spec, pageable);
	}

	@Override
	public TypedQuery<T> getQuery(Specification<T> spec, Sort sort) {
		return super.getQuery(spec, sort);
	}

	public Optional<T> findOne(Specification<T> spec, Sort sort) {
		try {
			return Optional.of(getQuery(spec, sort).setMaxResults(2).getSingleResult());
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}
	
	public void lock(T d) {
		em.lock(d, LockModeType.WRITE);
	}

	public void lock(T d, LockModeType lockModeType, Map<String, Object> properties) {
		em.lock(d, LockModeType.WRITE, properties);
	}

}
