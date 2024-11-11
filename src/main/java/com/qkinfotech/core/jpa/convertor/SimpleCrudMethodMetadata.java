package com.qkinfotech.core.jpa.convertor;

import java.lang.reflect.Method;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.QueryHints;

import jakarta.persistence.LockModeType;

public class SimpleCrudMethodMetadata implements CrudMethodMetadata{
	
	public static final SimpleCrudMethodMetadata DEFAULT = new SimpleCrudMethodMetadata();

	public static final SimpleCrudMethodMetadata LOCK = new SimpleCrudMethodMetadata(LockModeType.PESSIMISTIC_WRITE);
	
	LockModeType lockModeType;
	
	private SimpleCrudMethodMetadata(LockModeType lockModeType) {
		this.lockModeType = lockModeType;
	}

	private SimpleCrudMethodMetadata() {
		this.lockModeType = LockModeType.NONE;
	}

	// MutableQueryHints queryHints = new MutableQueryHints();

	@Override
	public LockModeType getLockModeType() {
		return lockModeType;
	}

	@Override
	public QueryHints getQueryHints() {
		return QueryHints.NoHints.INSTANCE;
	}

	@Override
	public QueryHints getQueryHintsForCount() {
		return QueryHints.NoHints.INSTANCE;
	}

	@Override
	public String getComment() {
		return null;
	}

	@Override
	public Optional<EntityGraph> getEntityGraph() {
		return null;
	}

	@Override
	public Method getMethod() {
		return null;
	}

}
