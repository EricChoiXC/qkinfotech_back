package com.qkinfotech.util;

import java.time.LocalTime;

public class WorkTimePeriod {

	public static final WorkTimePeriod FULLDAY = new WorkTimePeriod(LocalTime.of(0, 0, 0), LocalTime.of(23, 59, 59));

	private LocalTime st;

	private LocalTime et;

	public WorkTimePeriod(LocalTime st, LocalTime et) {
		this.st = st;
		this.et = et;
	}

	public boolean isAfter(LocalTime t) {
		return t.isAfter(et);
	}

	public boolean isBefore(LocalTime t) {
		return t.isBefore(st);
	}

	public boolean isWorkTime(LocalTime t) {
		long sd = t.toSecondOfDay();
		return sd >= st.toSecondOfDay() && sd <= et.toSecondOfDay();
	}

	public int getSecondOfWorkTime() {
		return et.toSecondOfDay() - st.toSecondOfDay();
	}

	public int getSecondOfWorkTimeUntil(LocalTime t) {
		if(et.isBefore(t) || et.equals(t)) {
			return et.toSecondOfDay() - st.toSecondOfDay();
		} else if(st.isAfter(t)){
			return 0;
		} else {
			return t.toSecondOfDay() - st.toSecondOfDay();
		}
	}

	public int getSecondOfWorkTimeFrom(LocalTime t) {
		if(st.isAfter(t) || st.equals(t)) {
			return et.toSecondOfDay() - st.toSecondOfDay();
		} else if(et.isBefore(t)){
			return 0;
		} else {
			return et.toSecondOfDay() - t.toSecondOfDay();
		}
	}

}
