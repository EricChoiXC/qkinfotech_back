package com.qkinfotech.util;

import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class UrlMappingUtil {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 获取所有接口列表
     * @return      接口列表
     */
    @PostConstruct
    public List<JSONObject> printAllUrls() {
        List<JSONObject> urlList = new ArrayList<>();
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            Set<PathPattern> patterns = null;
            if (mappingInfo.getPathPatternsCondition() != null) {
                patterns = mappingInfo.getPathPatternsCondition().getPatterns();
            }
            if (patterns != null) {
                patterns.forEach(url -> {
                    JSONObject obj = new JSONObject();
                    obj.put("url" , url.getPatternString());
                    obj.put("controller" , entry.getValue().getBeanType().getName());
                    urlList.add(obj);
                });
            }
        }
        return urlList;
    }
}
