package com.qkinfotech.core.sys.log.service;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.sys.log.model.SysReadLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Service
@Transactional
@Slf4j
public class ReadLogService {

    @Autowired
    private SimpleService<SysReadLog> sysReadLogService;

    @Autowired
    private SimpleService<OrgPerson> orgPersonService;

    public void readDocument(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject data = getPostData(request);
        String fModelId = data.getString("fModelId");
        String fModelName = data.getString("fModelName");
        String fType = data.getString("fType");
        String fUserId = data.getString("fUserId");
        Date fReadTime = new Date();
        addLog(fModelId, fModelName, fType, fUserId, fReadTime);
    }

    public void readAttachment(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject data = getPostData(request);
        String fModelId = data.getString("fModelId");
        String fModelName = data.getString("fModelName");
        String fType = data.getString("fType");
        String fUserId = data.getString("fUserId");
        Date fReadTime = new Date();
        String fAttachmentId = data.getString("fAttachmentId");
        String fOperation = data.getString("fOperation");
        addLog(fModelId, fModelName, fType, fUserId, fReadTime, fAttachmentId, fOperation);
    }

    public void addLog(String fModelId, String fModelName, String fType, String fUserId, Date fReadTime) throws Exception {
        addLog(fModelId, fModelName, fType, fUserId, fReadTime, null, null);
    }

    public void addLog(String fModelId, String fModelName, String fType, String fUserId, Date fReadTime, String fAttachmentId, String fOperation) throws Exception {
        OrgPerson person = orgPersonService.getById(fUserId);
        if (person == null) {
            return;
        }
        SysReadLog sysReadLog = new SysReadLog();
        sysReadLog.setfPerson(person);
        sysReadLog.setfModelId(fModelId);
        sysReadLog.setfModelName(fModelName);
        sysReadLog.setfType(fType);
        sysReadLog.setfReadTime(fReadTime);
        sysReadLog.setfAttachmentId(fAttachmentId);
        sysReadLog.setfOperation(fOperation);

        sysReadLogService.save(sysReadLog);
    }

    private JSONObject getPostData(HttpServletRequest request) {
        JSONObject data = new JSONObject();
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
        return data;
    }
}
