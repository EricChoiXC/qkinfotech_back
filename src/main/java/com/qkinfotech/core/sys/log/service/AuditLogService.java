package com.qkinfotech.core.sys.log.service;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.IEntityExtension;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.sys.log.model.SysAuditLog;
import com.qkinfotech.core.sys.log.model.SysAuditModel;
import com.qkinfotech.core.user.model.SysAuthority;
import com.qkinfotech.util.SpringUtil;
import com.qkinfotech.util.StringUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class AuditLogService implements IEntityExtension {

    @Autowired
    protected Bean2Json bean2json;

    @Autowired
    protected Json2Bean json2bean;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    private Map<String, JSONObject> oldDataMap = new HashMap<String, JSONObject>();

    private String OPERATION_ADD = "add";

    private String OPERATION_UPDATE = "update";

    private String OPERATION_DELETE = "delete";

    public void adding(BaseEntity entity){
        //System.out.println("adding");
    }

    public void added(BaseEntity entity){
        if (entity.getClass().isAnnotationPresent(SysAuditModel.class)) {
            System.out.println("added");
            SysAuditLog auditLog = writeAddLog(entity);
            if (auditLog != null){
                SimpleService<SysAuditLog> sysAuditLogService = (SimpleService<SysAuditLog>) SpringUtil.getContext().getBean("sysAuditLogService");
                sysAuditLogService.save(auditLog);
            }
        }
    }

    public JSONObject updating(BaseEntity entity, JSONObject savedData){
        if (entity.getClass().isAnnotationPresent(SysAuditModel.class)) {
            System.out.println("updating");
            oldDataMap.put(entity.getfId(), bean2json.toJson(entity));
        }
        return null;
    }

    public void updated(BaseEntity entity, JSONObject savedData){
        if (entity.getClass().isAnnotationPresent(SysAuditModel.class)) {
            System.out.println("updated");
            JSONObject oldData = oldDataMap.get(entity.getfId());
            SysAuditLog auditLog = writeUpLog(oldData, entity);
            if (auditLog != null){
                SimpleService<SysAuditLog> sysAuditLogService = (SimpleService<SysAuditLog>) SpringUtil.getContext().getBean("sysAuditLogService");
                sysAuditLogService.save(auditLog);
            }
            oldDataMap.remove(entity.getfId());
        }
    }

    public void deleting(BaseEntity entity) {
        //System.out.println("deleting");
    }

    public void deleted(BaseEntity entity) {
        //System.out.println("deleted");
        if (entity.getClass().isAnnotationPresent(SysAuditModel.class)) {
            System.out.println("deleted");
            SysAuditLog auditLog = writeDelLog(entity);
            if (auditLog != null){
                SimpleService<SysAuditLog> sysAuditLogService = (SimpleService<SysAuditLog>) SpringUtil.getContext().getBean("sysAuditLogService");
                sysAuditLogService.save(auditLog);
            }

        }
    }

    public void init(BaseEntity entity){
        //System.out.println("init");
    }

    public void prepare(Class<? extends BaseEntity> entutyClass, String method, JSONObject requestData) {
        //System.out.println("prepare");
    }

    private SysAuditLog writeAddLog(BaseEntity entity) {
        JSONObject saveData = bean2json.toJson(entity);
        SysAuditLog auditLog = new SysAuditLog();
        auditLog.getfId();
        auditLog.setfOperation(OPERATION_ADD);
        auditLog.setfModelName(entity.getClass().getName());
        auditLog.setfModelId(entity.getfId());
        auditLog.setfCreateTime(new Date());
        auditLog.setfPerson(getPerson());
        auditLog.setfAuditMessage("添加记录");
        return auditLog;
    }

    private SysAuditLog writeDelLog(BaseEntity entity) {
        JSONObject saveData = bean2json.toJson(entity);
        SysAuditLog auditLog = new SysAuditLog();
        auditLog.getfId();
        auditLog.setfOperation(OPERATION_DELETE);
        auditLog.setfModelName(entity.getClass().getName());
        auditLog.setfModelId(entity.getfId());
        auditLog.setfCreateTime(new Date());
        auditLog.setfPerson(getPerson());
        auditLog.setfAuditMessage("删除该记录");
        return auditLog;
    }

    private SysAuditLog writeUpLog(JSONObject oldDate, BaseEntity entity) {
        JSONObject savedData = bean2json.toJson(entity);

        String message = getMessage(oldDate, savedData);
        if (StringUtil.isNull(message)) {
            return null;
        }

        SysAuditLog auditLog = new SysAuditLog();
        auditLog.getfId();
        auditLog.setfOperation(OPERATION_UPDATE);
        auditLog.setfModelName(entity.getClass().getName());
        auditLog.setfModelId(entity.getfId());
        auditLog.setfCreateTime(new Date());
        auditLog.setfPerson(getPerson());
        auditLog.setfAuditMessage(message);
        return auditLog;
    }

    private OrgPerson getPerson() {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("id")) {
                SimpleService<OrgPerson> orgPersonService = (SimpleService<OrgPerson>) SpringUtil.getContext().getBean("orgPersonService");
                return orgPersonService.getById(cookie.getValue());
            }
        }
        return null;
    }

    private String getMessage(JSONObject oldDate, JSONObject savedData) {
        StringBuffer message = new StringBuffer();
        for (String key : oldDate.keySet()) {
            if (savedData.containsKey(key)) {
                if (!oldDate.get(key).equals(savedData.get(key))) {
                    if (oldDate.get(key) instanceof Collection<?> oldCollection) {
                        message.append("更新集合字段信息：" + key + "\n");
                    } else if (oldDate.get(key) instanceof Map<?, ?> oldMap) {
                        message.append("更新集合字段信息：" + key + "\n");
                        Map savedMap = (Map) savedData.get(key);

                    } else if (oldDate.get(key).getClass().isArray()) {
                        message.append("更新明细表信息：" + key + "\n");


                    } else {
                        message.append("更新字段信息：" + key + "\n");
                    }
                }
            } else {
                message.append("删除新字段信息：" + key + ":" + oldDate.get(key) + "\n");
            }
        }
        for (String key : savedData.keySet()) {
            if (oldDate.containsKey(key)) {

            } else {
                message.append("增加新字段信息：" + key + ":" + savedData.get(key) + "\n");
            }
        }
        return message.toString();
    }

    private String contrastCollection(Collection oldCollection, Collection savedCollection) {
        for (Object o : oldCollection) {
            if (o instanceof BaseEntity) {

            } else {

            }
            break;
        }

        return null;
    }
}
