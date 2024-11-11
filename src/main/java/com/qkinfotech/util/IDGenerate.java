package com.qkinfotech.util;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import com.github.f4b6a3.ulid.UlidCreator;

public class IDGenerate implements IdentifierGenerator {

	@Override
	public Object generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
		return generate();
	}
	
	public static String generate() {
		return UlidCreator.getUlid().toString();
		
	}

}
