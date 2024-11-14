package com.qkinfotech.core.tendering.controller;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.auth.token.EkpToken;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.IEntityExtension;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.mvc.util.QueryBuilder;
import com.qkinfotech.core.tendering.model.apps.project.*;
import com.qkinfotech.core.user.model.SysUser;
import com.qkinfotech.util.StringUtil;
import com.qkinfotech.util.TokenUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("apps/project/main/history")
public class AppsProjectMainHistoryController<T extends BaseEntity>{

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected SimpleResult result;

    @Autowired
    protected Json2Bean json2bean;

    @Autowired
    protected SimpleService<AppsProjectMainHistory> appsProjectMainHistoryService;

    @Autowired
    protected Bean2Json bean2json;

    @Autowired(required = false)
    protected List<IEntityExtension> extensions;

    protected Class<AppsProjectMainHistory> modelClass = AppsProjectMainHistory.class;

    private TokenUtil tokenUtil;

    @RequestMapping("/list")
    @ResponseBody
    public void list(HttpServletRequest request) throws Exception {
        JSONObject body = getPostData("list");
        body.put("distinct","");
        QueryBuilder<AppsProjectMainHistory> qb = QueryBuilder.parse(modelClass, body);

        Page<AppsProjectMainHistory> data = appsProjectMainHistoryService.findAll(qb.specification(), qb.pageable());

        Page<JSONObject> out = data.map(e -> bean2json.toJson(e));

        result.from(out);
    }


    private JSONObject getPostData(String method) {
        JSONObject data = new JSONObject();
        if ("POST".equals(request.getMethod())) {
            try {
                InputStream in = request.getInputStream();
                byte[] b = FileUtil.readAsByteArray(in);
                String enc = request.getCharacterEncoding();
                if (!StringUtils.hasText(enc)) {
                    enc = "UTF-8";
                }
                String txt = new String(b, enc);
                if (!StringUtils.hasText(txt)) {
                    return new JSONObject();
                }
                data = JSONObject.parseObject(txt);

            } catch (IOException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
        if(extensions != null) {
            for(int i = 0; i < extensions.size(); ++ i) {
                extensions.get(i).prepare(modelClass, method, data);
            }
        }
        return data;
    }
}
