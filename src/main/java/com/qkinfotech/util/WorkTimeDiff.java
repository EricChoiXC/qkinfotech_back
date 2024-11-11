package com.qkinfotech.util;

public class WorkTimeDiff {
	
	/* 天数 */
	private long days;
	
	/* 秒数（如果相差 3天 200秒， 返回 200）*/
	private long secondsOfDay;

	/* 秒数（如果相差 3天 200秒，如果每天工作时间 8 小时， 返回 200 + 8 * 60 * 60）*/
	private long totalSeconds;

	public WorkTimeDiff(long days, long secondsOfDay, long totalSeconds) {
		super();
		this.days = days;
		this.secondsOfDay = secondsOfDay;
		this.totalSeconds = totalSeconds;
	}

	public long getDays() {
		return days;
	}

	public long getSecondsOfDay() {
		return secondsOfDay;
	}

	public long getTotalSeconds() {
		return totalSeconds;
	}

	@Override
	public String toString() {
		return days + " days " + secondsOfDay + " seconds. total: " + totalSeconds + " seconds";
	}

}
