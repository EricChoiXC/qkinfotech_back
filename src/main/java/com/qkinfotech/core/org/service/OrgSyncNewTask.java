package com.qkinfotech.core.org.service;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.app.config.EkpConfig;
import com.qkinfotech.core.task.ITask;
import com.qkinfotech.core.task.Task;
import com.qkinfotech.core.task.TaskLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;

import java.sql.*;
import java.util.*;

@Task(trigger = "cron:0 0 1 * * ?", group = "org", name = "新组织架构同步")
@Slf4j
public class OrgSyncNewTask implements ITask {

    @Autowired
    private EkpConfig ekpConfig;

    @Autowired
    private OrgSyncService orgSyncService;

    @Override
    public void execute(TaskLogger log, JSONObject parameter) throws Exception {
        try {
            log.write("===== start sync organization =====");
            logger.info("===== start sync organization =====");

            //1.同步机构
            log.write(" sync company ");
            logger.info(" sync company ");
            Map<String, JSONObject> companyMap = scroll(" select * from sys_org_element where fd_org_type = ? ", new String[]{"1"}, "fd_id");
            orgSyncService.syncCompany(companyMap);

            //2.同步部门
            log.write(" sync dept ");
            logger.info(" sync dept ");
            Map<String, JSONObject> deptMap = scroll(" select * from sys_org_element where fd_org_type = ? ", new String[]{"2"}, "fd_id");
            orgSyncService.syncDept(deptMap);

            //3.同步岗位
            log.write(" sync post ");
            logger.info(" sync post ");
            Map<String, JSONObject> postMap = scroll(" select * from sys_org_element where fd_org_type = ? ", new String[]{"4"}, "fd_id");
            orgSyncService.syncPost(postMap);

            //4.同步人员
            log.write(" sync person ");
            logger.info(" sync person ");
            //4.1 同步sys_user
            log.write(" sync sys_user ");
            logger.info(" sync sys_user ");
            Map<String, JSONObject> personEleMap = scroll(" select * from sys_org_element where fd_org_type = ? ", new String[]{"8"}, "fd_id");
            Map<String, JSONObject> personMap = scroll(" select * from sys_org_person ", new String[]{}, "fd_id");
            orgSyncService.syncUser(personMap, personEleMap);
            //4.2 同步org_person
            log.write(" sync org_person ");
            logger.info(" sync org_person ");
            orgSyncService.syncPerson(personMap, personEleMap);
            //4.3 同步供应商
            log.write(" sync supplier ");
            logger.info(" sync supplier ");
            Map<String, JSONObject> supplierMap = scroll(" select * from km_supplier_person order by doc_create_time desc ", new String[]{}, "fd_id");
            orgSyncService.syncSupplier(supplierMap);
            //4.4 同步专家
            log.write(" sync expert ");
            logger.info(" sync expert ");
            Map<String, JSONObject> expertMap = scroll(" select * from km_expert_person order by doc_create_time desc ", new String[]{}, "fd_id");
            orgSyncService.syncExpert(expertMap);

            //5.同步群组
            log.write(" sync group ");
            logger.info(" sync group ");
            Map<String, JSONObject> groupMap = scroll(" select * from sys_org_element where fd_org_type = ? ", new String[]{"16"}, "fd_id");
            orgSyncService.syncGroup(groupMap);

            //6.同步岗位人员
            log.write(" sync post members ");
            logger.info(" sync post members ");
            Set<JSONObject> postMembersSet = scroll(" select * from sys_org_post_person ", new String[]{});
            orgSyncService.syncPostMembers(postMembersSet);

            //7.同步群组人员
            log.write(" sync group members ");
            logger.info(" sync group members ");
            Set<JSONObject> groupMembersSet = scroll(" select * from sys_org_group_element ", new String[]{});
            orgSyncService.syncGroupMembers(groupMembersSet);

            log.write(" ===== sync finish =====");
            logger.info(" ===== sync finish =====");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {

        }
    }

    /**
     * 查询语句，返回Map，需要key字段名
     */
    public Map<String, JSONObject> scroll(String sql, String[] inputs, String key) throws Exception {
        Connection conn = DriverManager.getConnection(
                ekpConfig.getEkpDatabaseUrl(),
                ekpConfig.getEkpDatabaseUsername(),
                ekpConfig.getEkpDatabasePassword());
        Map<String, JSONObject> result = new HashMap<String, JSONObject>();
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

            while(rs.next()) {
                JSONObject json = new JSONObject();
                for (int i=1; i<=columnNum; i++) {
                    String name = data.getColumnName(i);
                    String columnTypeName = data.getColumnTypeName(i);
                    if (columnTypeName.equals("DATETIME") ||
                            columnTypeName.equals("DATE") ||
                            columnTypeName.equals("TIME") ||
                            columnTypeName.equals("TIMESTAMP")){
                        Timestamp timestamp = rs.getTimestamp(i);
                        json.put(name, timestamp);
                    } else {
                        Object value = rs.getObject(i);
                        json.put(name, value);
                    }
                }
                result.put(json.getString(key), json);
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

    /**
     * 查询语句，返回Map，需要key字段名
     */
    public Set<JSONObject> scroll(String sql, String[] inputs) throws Exception {
        Connection conn = DriverManager.getConnection(
                ekpConfig.getEkpDatabaseUrl(),
                ekpConfig.getEkpDatabaseUsername(),
                ekpConfig.getEkpDatabasePassword());
        Set<JSONObject> result = new HashSet<JSONObject>();
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

            while(rs.next()) {
                JSONObject json = new JSONObject();
                for (int i=1; i<=columnNum; i++) {
                    String name = data.getColumnName(i);
                    String columnTypeName = data.getColumnTypeName(i);
                    if (columnTypeName.equals("DATETIME") ||
                            columnTypeName.equals("DATE") ||
                            columnTypeName.equals("TIME") ||
                            columnTypeName.equals("TIMESTAMP")){
                        Timestamp timestamp = rs.getTimestamp(i);
                        json.put(name, timestamp);
                    } else {
                        Object value = rs.getObject(i);
                        json.put(name, value);
                    }
                }
                result.add(json);
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
