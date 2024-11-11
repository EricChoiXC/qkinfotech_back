package com.qkinfotech.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeProfiler {
	
	private Date startTime;
	private Date stopTime;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:MM:ss.SSS");
	
	public void start() {
		startTime = new Date();
	}
	public void stop() {
		stopTime = new Date();
	}

	public long getCost() {
		return stopTime.getTime() - startTime.getTime();
	}

	public Date getStart() {
		return startTime;
	}
	
	public Date getStop() {
		return stopTime;
	}

	public String getStartTime() {
		return sdf.format(startTime);
	}
	
	public String getStopTime() {
		return sdf.format(stopTime);
	}
}
