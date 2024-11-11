package com.qkinfotech.core.tendering.apps.project.web;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.auth.util.ApiRsaUtil;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.tendering.interfaceConfig.InterfaceLog;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.util.StringUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/project-main/web/service")
public class AppsProjectMainWeb {

    @Autowired
    private SimpleService<AppsProjectMain> appsProjectMainService;

    @Autowired
    private SimpleService<InterfaceLog> interfaceLogService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletRequest response;


    protected String updateProjectStatusName = "项目状态变更接口";

    /**
     * 项目状态变更接口
     * @throws Exception
     */
    @PostMapping("/updateProjectStatus")
    public JSONObject updateProjectStatus(@RequestBody String requestStr) throws Exception {
        InterfaceLog log = new InterfaceLog();
        JSONObject result = new JSONObject();
        
        try {
            log.getfId();
            log.setfCreateTime(new Date());
            log.setfInterfaceName(updateProjectStatusName);
            log.setfInterfaceUrl(this.getClass().getAnnotation(RequestMapping.class).value()[0]
                    + this.getClass().getMethod("updateProjectStatus", String.class).getAnnotation(PostMapping.class).value()[0]);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        try {
            String jsonString = ApiRsaUtil.decrypt(requestStr);
            JSONObject json = JSONObject.parseObject(jsonString);
            log.setfInputParameter(json.toString());

            String projectNo = json.getString("projectNo");
            String pmId = json.getString("pmId");
            if (StringUtil.isNull(pmId)) {
                throw new Exception("未提供项目编号");
            } else {
                log.setfProtocolNo(StringUtil.isNull(projectNo) ? pmId : projectNo);
                AppsProjectMain main = appsProjectMainService.getById(pmId);
                if (main != null) {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    main.setfAuditStatus(json.getString("status"));
                    if (json.containsKey("baseFinishTime") && StringUtil.isNotNull(json.getString("baseFinishTime"))) {
                        main.setfBaseFinishTime(df.parse(json.getString("baseFinishTime")));
                    }
                    if (json.containsKey("limitFinishTime") && StringUtil.isNotNull(json.getString("limitFinishTime"))) {
                        main.setfBaseFinishTime(df.parse(json.getString("limitFinishTime")));
                    }
                    if (json.containsKey("qualifyTime") && StringUtil.isNotNull(json.getString("qualifyTime"))) {
                        main.setfQualifyTime(df.parse(json.getString("qualifyTime")));
                    }
                    appsProjectMainService.save(main);
                    result.put("status", "S");
                    log.setfInterfaceStatus("1");
                    log.setfInterfaceInfo(result.toString());
                } else {
                    throw new Exception("未查询到项目");
                }
            }
        } catch (Exception e) {
            result.put("status", "E");
            result.put("message", e.getMessage());
            log.setfInterfaceStatus("2");
            log.setfInterfaceInfo(e.getMessage());
        }
        interfaceLogService.save(log);
        return result;
    }
}
