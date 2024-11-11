package com.qkinfotech.core.task;

import com.alibaba.fastjson2.JSONObject;

public interface ITask {
	
	void execute(TaskLogger logger, JSONObject parameter) throws Exception;
	
}
