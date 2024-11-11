package com.qkinfotech.core.web.controller;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.app.config.EkpConfig;
import com.qkinfotech.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Controller
@RequestMapping("/process")
public class ProcessController {

    @Autowired
    private EkpConfig ekpConfig;

    @RequestMapping("/getProcessHandlers")
    @ResponseBody
    public JSONObject getProcessHandlers(@RequestBody JSONObject requestJson) throws Exception {
        JSONObject resJson = new JSONObject();

        String processId = requestJson.getString("processId");
        if (StringUtil.isNotNull(processId)) {
            Connection conn = null;
            PreparedStatement statement = null;
            ResultSet rs = null;
            try {
                conn = DriverManager.getConnection(
                    ekpConfig.getEkpDatabaseUrl(),
                    ekpConfig.getEkpDatabaseUsername(),
                    ekpConfig.getEkpDatabasePassword()
                );
                statement = conn.prepareStatement(" select fd_id from sys_org_element where fd_id in (select fd_handler_id from lbpm_expecter_log where fd_process_id = ?)");
                statement.setString(1, processId);
                rs = statement.executeQuery();
                Set<String> ids = new HashSet<>();
                while (rs.next()) {
                    ids.add(rs.getString(1));
                }
                resJson.put("success", true);
                resJson.put("message", ids);
            } catch (Exception e) {
                resJson.put("success", false);
                resJson.put("message", e.getMessage());
                e.printStackTrace();
            } finally {
                if (Objects.nonNull(conn)){
                    conn.close();
                }
                if (Objects.nonNull(rs)) {
                    rs.close();
                }
                if (Objects.nonNull(statement)){
                    statement.close();
                }
            }
        } else {
            resJson.put("success", false);
            resJson.put("message", "未提供流程id");
        }

        return resJson;
    }

}
