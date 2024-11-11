package com.qkinfotech.core.org.service;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.app.config.EkpConfig;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.org.model.OrgDept;
import com.qkinfotech.core.org.model.OrgElement;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.tendering.interfaceConfig.InterfaceLog;
import com.qkinfotech.core.tendering.model.org.SitcExpert;
import com.qkinfotech.core.tendering.model.org.SitcSupplier;
import com.qkinfotech.core.user.model.SysUser;
import com.qkinfotech.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * 接收MQ推送更新组织架构信息
 * @author 蔡咏钦
 */
@Component
public class OrgSyncMqHandler {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    private SimpleService<OrgPerson> orgPersonService;

    @Autowired
    private SimpleService<OrgElement> orgElementService;

    @Autowired
    private SimpleService<OrgDept> orgDeptService;

    @Autowired
    private SimpleService<SysUser> sysUserService;

    @Autowired
    private SimpleService<SitcExpert> sitcExpertService;

    @Autowired
    private SimpleService<SitcSupplier> sitcSupplierService;

    @Autowired
    private SimpleService<InterfaceLog> interfaceLogService;

    @Autowired
    private EkpConfig ekpConfig;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private OrgSyncService orgSyncService;

    /**
     * 同步更新供应商
     * @param message
     * @return
     * @throws Exception
     */
    //@JmsListener(destination = "EKP_SUPPLIER_UPDATE_NOTIFY")
    public JSONObject syncSupplier(String message) throws Exception {
        System.out.println("syncSupplier");
        JSONObject json = JSONObject.parseObject(message);
        InterfaceLog interfaceLog = new InterfaceLog();
        interfaceLog.setfInputParameter(JSONObject.toJSONString(json));
        interfaceLog.setfCreateTime(new Date());
        interfaceLog.setfInterfaceName("MQ同步更新供应商");

        String fId = json.getString("id");
        String updateId = json.getString("update_id");
        try {
            JSONObject personJson = scroll(" select * from sys_org_person where fd_id = ? ", new String[]{fId}, "fd_id");
            JSONObject elementJson = scroll(" select * from sys_org_element where fd_id = ? ", new String[]{fId}, "fd_id");
            JSONObject supplierJson = scroll(" select * from km_supplier_person where fd_id = ? ", new String[]{StringUtil.isNull(updateId) ? fId : updateId}, "fd_id");
            if (StringUtil.isNotNull(json.getString("name"))) {
                personJson.put("fd_name", json.getString("name"));
            }
            if (StringUtil.isNotNull(json.getString("user_type"))) {
                personJson.put("fd_ekp_user_type", json.getString("user_type"));
            }
            if (StringUtil.isNotNull(json.getString("update_id"))) {
                personJson.put("fd_update_id", json.getString("update_id"));
            }
            if (StringUtil.isNotNull(json.getString("supplierType"))) {
                personJson.put("fd_supplier_type", json.getString("supplierType"));
            }
            if (StringUtil.isNotNull(json.getString("supplierCode"))) {
                personJson.put("fd_supplier_code", json.getString("supplierCode"));
            }
            if (StringUtil.isNotNull(json.getString("supplierContacts"))) {
                personJson.put("fd_supplier_contacts", json.getString("supplierContacts"));
            }
            if (StringUtil.isNotNull(json.getString("update_id"))) {
                personJson.put("fd_update_id", json.getString("update_id"));
            }


            System.out.println("syncUser");
            orgSyncService.updateUser(personJson, elementJson);
            System.out.println("syncPerson");
            orgSyncService.updatePerson(personJson, elementJson);
            System.out.println("syncSupplier");
            orgSyncService.updateSupplier(supplierJson);

            interfaceLog.setfInterfaceStatus("1");
        } catch (Exception e) {
            interfaceLog.setfInterfaceInfo(e.getMessage());
            e.printStackTrace();
            interfaceLog.setfInterfaceStatus("2");
            throw e;
        } finally {
            orgSyncService.saveLog(interfaceLog);
        }
        System.out.println("syncSupplier finish");

        return null;
    }


    //@JmsListener(destination = "EKP_EXPERT_UPDATE_NOTIFY")
    public JSONObject syncExpert(String message) throws Exception {
        JSONObject json = JSONObject.parseObject(message);

        InterfaceLog interfaceLog = new InterfaceLog();
        interfaceLog.setfInputParameter(JSONObject.toJSONString(json));
        interfaceLog.setfCreateTime(new Date());
        interfaceLog.setfInterfaceName("MQ同步更新专家");

        String fId = json.getString("id");
        String updateId = json.getString("update_id");
        try {
            JSONObject personJson = scroll(" select * from sys_org_person where fd_id = ? ", new String[]{fId}, "fd_id");
            JSONObject elementJson = scroll(" select * from sys_org_element where fd_id = ? ", new String[]{fId}, "fd_id");
            JSONObject expertJson = scroll(" select * from km_expert_person where fd_id = ? ", new String[]{StringUtil.isNull(updateId) ? fId : updateId}, "fd_id");
            if (StringUtil.isNotNull(json.getString("name"))) {
                personJson.put("fd_name", json.getString("name"));
            }
            if (StringUtil.isNotNull(json.getString("user_type"))) {
                personJson.put("fd_ekp_user_type", json.getString("user_type"));
            }
            if (StringUtil.isNotNull(json.getString("update_id"))) {
                personJson.put("fd_update_id", json.getString("update_id"));
            }
            if (StringUtil.isNotNull(json.getString("expertBankNum"))) {
                personJson.put("fd_expert_bank_num", json.getString("expertBankNum"));
            }
            if (StringUtil.isNotNull(json.getString("expertCode"))) {
                personJson.put("fd_expert_code", json.getString("expertCode"));
            }

            System.out.println("syncUser");
            orgSyncService.updateUser(personJson, elementJson);
            System.out.println("syncPerson");
            orgSyncService.updatePerson(personJson, elementJson);
            System.out.println("updateExpert");
            orgSyncService.updateExpert(expertJson);

            interfaceLog.setfInterfaceStatus("1");
        } catch (Exception e) {
            e.printStackTrace();
            interfaceLog.setfInterfaceStatus("2");
            interfaceLog.setfInterfaceInfo(e.getMessage());
            throw e;
        } finally {
            orgSyncService.saveLog(interfaceLog);
        }
        return null;
    }

    public JSONObject scroll(String sql, String[] inputs, String key) throws Exception {
        Connection conn = DriverManager.getConnection(
                ekpConfig.getEkpDatabaseUrl(),
                ekpConfig.getEkpDatabaseUsername(),
                ekpConfig.getEkpDatabasePassword());
        JSONObject result = new JSONObject();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = conn.prepareStatement(sql);
            if (inputs != null) {
                for (int i=0; i<inputs.length; i++) {
                    statement.setString(i+1, inputs[i]);
                }
            }
            rs = statement.executeQuery();

            ResultSetMetaData data = rs.getMetaData();
            int columnNum = data.getColumnCount();

            if(rs.next()) {
                JSONObject json = new JSONObject();
                for (int i=1; i<=columnNum; i++) {
                    String name = data.getColumnName(i);
                    String columnTypeName = data.getColumnTypeName(i);
                    if (columnTypeName.equals("DATETIME") ||
                            columnTypeName.equals("DATE") ||
                            columnTypeName.equals("TIME") ||
                            columnTypeName.equals("TIMESTAMP")){
                        Timestamp timestamp = rs.getTimestamp(i);
                        result.put(name, timestamp);
                    } else {
                        Object value = rs.getObject(i);
                        result.put(name, value);
                    }
                }
            }
        } finally {
            if (Objects.nonNull(rs)) {
                rs.close();
            }
            if (Objects.nonNull(statement)){
                statement.close();
            }
            if (Objects.nonNull(conn)) {
                conn.close();
            }
        }
        return result;
    }
}
