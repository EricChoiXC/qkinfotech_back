package com.qkinfotech.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class ExceptionUtil {

	public static String toString(Throwable e) {
		
		return ExceptionUtils.getMessage(e) + System.lineSeparator() +	ExceptionUtils.getStackTrace(e);
		
	}
	
}
