package com.qkinfotech.core.mvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class SimpleRepositoryNoUse<T extends BaseEntity> {
	
	@Autowired
	EntityManager em;
	
	SimpleRepository<T> repository;
	
	protected Class<T> entityClass;
	
	public SimpleRepositoryNoUse(Class<T> clazz) {
		this.entityClass = clazz;
	}

	@PostConstruct
	public void init() {
		repository = new SimpleRepository<T>(entityClass, em);
	}
	
	public Class<T> getEntityClass() {
		return entityClass;
	}
	
	public void deleteAllById(String[] ids) {
		repository.deleteAllById(Arrays.asList(ids));
	}

	public T getById(String id) {
		return repository.getEntityManager().find(entityClass, id);
	}

	public JpaEntityInformation<T, ?> getEntityInformation() {
		return repository.getEntityInformation();
	}

	public EntityManager getEntityManager() {
		return em;
	}

	public void scroll(Specification<T> spec, Consumer<T> consumer) {
		scroll(spec, Sort.unsorted(), consumer);
	}
	
	public void scroll(Specification<T> spec, Sort sort, Consumer<T> consumer) {
		Query query = (Query)repository.getQuery(spec, sort);
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

	public int hashCode() {
		return repository.hashCode();
	}

	public void deleteInBatch(Iterable<T> entities) {
		repository.deleteInBatch(entities);
	}

	public TypedQuery<T> getQuery(Specification<T> spec, Pageable pageable) {
		return repository.getQuery(spec, pageable);
	}

	public TypedQuery<T> getQuery(Specification<T> spec, Sort sort) {
		return repository.getQuery(spec, sort);
	}

	public boolean equals(Object obj) {
		return repository.equals(obj);
	}

	public void setRepositoryMethodMetadata(CrudMethodMetadata crudMethodMetadata) {
		repository.setRepositoryMethodMetadata(crudMethodMetadata);
	}

	public void setEscapeCharacter(EscapeCharacter escapeCharacter) {
		repository.setEscapeCharacter(escapeCharacter);
	}

	public void deleteById(String id) {
		repository.deleteById(id);
	}

	public void delete(T entity) {
		repository.delete(entity);
	}

	public void deleteAllById(Iterable<? extends String> ids) {
		repository.deleteAllById(ids);
	}

	public void deleteAllByIdInBatch(Iterable<String> ids) {
		repository.deleteAllByIdInBatch(ids);
	}

	public void deleteAll(Iterable<? extends T> entities) {
		repository.deleteAll(entities);
	}

	public void deleteAllInBatch(Iterable<T> entities) {
		repository.deleteAllInBatch(entities);
	}

	public void deleteAll() {
		repository.deleteAll();
	}

	public void deleteAllInBatch() {
		repository.deleteAllInBatch();
	}

	public Optional<T> findById(String id) {
		return repository.findById(id);
	}

	public String toString() {
		return repository.toString();
	}

	public T getOne(String id) {
		return repository.getOne(id);
	}

	public T getReferenceById(String id) {
		return repository.getReferenceById(id);
	}

	public boolean existsById(String id) {
		return repository.existsById(id);
	}

	public List<T> findAll() {
		return repository.findAll();
	}

	public List<T> findAllById(Iterable<String> ids) {
		return repository.findAllById(ids);
	}

	public List<T> findAll(Sort sort) {
		return repository.findAll(sort);
	}

	public Page<T> findAll(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public Optional<T> findOne(Specification<T> spec) {
		return repository.findOne(spec);
	}

	public List<T> findAll(Specification<T> spec) {
		return repository.findAll(spec);
	}

	public Page<T> findAll(Specification<T> spec, Pageable pageable) {
		return repository.findAll(spec, pageable);
	}

	public List<T> findAll(Specification<T> spec, Sort sort) {
		return repository.findAll(spec, sort);
	}

	public boolean exists(Specification<T> spec) {
		return repository.exists(spec);
	}

	public long delete(Specification<T> spec) {
		return repository.delete(spec);
	}

	public <S extends T, R> R findBy(Specification<T> spec, Function<FetchableFluentQuery<S>, R> queryFunction) {
		return repository.findBy(spec, queryFunction);
	}

	public <S extends T> Optional<S> findOne(Example<S> example) {
		return repository.findOne(example);
	}

	public <S extends T> long count(Example<S> example) {
		return repository.count(example);
	}

	public <S extends T> boolean exists(Example<S> example) {
		return repository.exists(example);
	}

	public <S extends T> List<S> findAll(Example<S> example) {
		return repository.findAll(example);
	}

	public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
		return repository.findAll(example, sort);
	}

	public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
		return repository.findAll(example, pageable);
	}

	public <S extends T, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
		return repository.findBy(example, queryFunction);
	}

	public long count() {
		return repository.count();
	}

	public long count(Specification<T> spec) {
		return repository.count(spec);
	}

	public <S extends T> S save(S entity) {
		return repository.save(entity);
	}

	public <S extends T> S saveAndFlush(S entity) {
		return repository.saveAndFlush(entity);
	}

	public <S extends T> List<S> saveAll(Iterable<S> entities) {
		return repository.saveAll(entities);
	}

	public <S extends T> List<S> saveAllAndFlush(Iterable<S> entities) {
		return repository.saveAllAndFlush(entities);
	}

	public void flush() {
		repository.flush();
	}

}
