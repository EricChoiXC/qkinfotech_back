package com.qkinfotech.util;

public class StringUtil {
	
	public static Boolean isNull(String str) {
		if (str == null) {
			return true;
		}
		return str.trim().length() == 0;
	}
	
	public static Boolean isNotNull(String str) {
		return !isNull(str);
	}
	
}
