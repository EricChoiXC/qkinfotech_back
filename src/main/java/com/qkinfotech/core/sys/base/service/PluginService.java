package com.qkinfotech.core.sys.base.service;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.BaseEntity;

import java.util.List;
import java.util.Set;

public interface PluginService {

    void save(BaseEntity entity, Object value);

    void delete(BaseEntity entity, Object parameter);

    void deleteAll(Set<String> ids, JSONObject json);
}
