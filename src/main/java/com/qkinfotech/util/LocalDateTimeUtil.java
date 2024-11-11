package com.qkinfotech.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public class LocalDateTimeUtil {
	
	@SuppressWarnings("deprecation")
	public static LocalDateTime toLocalDateTime(Date date) {
		return LocalDateTime.of(date.getYear()+1900, date.getMonth() + 1, date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds());
	}
	
	@SuppressWarnings("deprecation")
	public static LocalDate toLocalDate(Date date) {
		return LocalDate.of(date.getYear()+1900, date.getMonth() + 1, date.getDate());
	}

	@SuppressWarnings("deprecation")
	public static LocalTime toLocalTime(Date date) {
		return LocalTime.of(date.getHours(), date.getMinutes(), date.getSeconds());
	}

}
