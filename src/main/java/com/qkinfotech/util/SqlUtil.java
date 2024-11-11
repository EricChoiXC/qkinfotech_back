package com.qkinfotech.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 * sql工具类
 */
public class SqlUtil {

    /**
     * 给body设置参数
     *
     * @param jsonObject 原body参数
     * @param keyName    需要设置的键名
     * @param keyValue   需要设置的键值
     * @param jsonKey    需要设置的jsonKey
     * @param condition  条件字段
     */
    public static void setParameter(JSONObject jsonObject, String keyName, String keyValue, String jsonKey, String condition) {
        if (null == jsonObject) {
            jsonObject = new JSONObject();
        }
        JSONArray jsonKeyModel = null;
        if (jsonObject.containsKey(jsonKey)) {
            jsonKeyModel = jsonObject.getJSONArray(jsonKey);
        } else {
            jsonKeyModel = new JSONArray();
        }
        JSONObject keyNameModel = new JSONObject();
        JSONObject conditionModel = new JSONObject();
        keyNameModel.put(keyName, keyValue);
        conditionModel.put(condition, keyNameModel);
        jsonKeyModel.add(conditionModel);
        jsonObject.put(jsonKey, jsonKeyModel);
    }

}
