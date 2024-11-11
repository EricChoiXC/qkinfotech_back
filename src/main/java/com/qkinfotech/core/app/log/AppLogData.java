package com.qkinfotech.core.app.log;

import java.util.Date;

public interface AppLogData {
	
	void setup(String server, Date timestamp, String thread);
	
}
