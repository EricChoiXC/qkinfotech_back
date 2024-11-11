package com.qkinfotech.core.tendering.iso.controller;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.tendering.iso.model.IsoApproval;
import com.qkinfotech.core.tendering.model.apps.collection.AppsCollectionResultDetail;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import com.qkinfotech.util.IDGenerate;
import com.qkinfotech.util.StringUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/isoController")
public class IsoController {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected SimpleResult result;

    @Autowired
    protected Json2Bean json2bean;

    @Autowired
    protected Bean2Json bean2json;

    @Autowired
    protected SimpleService<IsoApproval> isoApprovalService;

    @Autowired
    protected SimpleService<AttachmentMain> attachmentMainService;

    @PostMapping("getIsoEkpId")
    @ResponseBody
    public void getIsoEkpId() throws Exception {
        JSONObject body = getPostData();
        String fId = body.getString("fId");
        if (StringUtil.isNotNull(fId)) {
            IsoApproval isoApproval = isoApprovalService.getById(fId);
            JSONObject res = new JSONObject();
            if (isoApproval != null) {
                res.put("fEkpId", isoApproval.getfEkpId());
                if (isoApproval.getfCreator() != null) {
                    res.put("fCreatorId", isoApproval.getfCreator().getfId());
                }
            }
            result.from(res);
        }
    }

    @PostMapping("copyAttachment")
    @ResponseBody
    public void copyAttachment() throws Exception {
        JSONObject body = getPostData();
        String oldFid = body.getString("oldFid");
        String newFid = body.getString("newFid");
        String key = body.getString("key");
        Specification<AttachmentMain> specification = new Specification<AttachmentMain>() {
            @Override
            public Predicate toPredicate(Root<AttachmentMain> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate eq1 = criteriaBuilder.equal(root.get("fModelId"), oldFid);
                Predicate eq2 = criteriaBuilder.equal(root.get("fModelName"), "com.qkinfotech.core.tendering.iso.model.IsoApproval");
                Predicate eq3 = criteriaBuilder.equal(root.get("fKey"), key);
                return criteriaBuilder.and(eq1, eq2, eq3);
            }
        };
        List<AttachmentMain> attachmentMainList = attachmentMainService.findAll(specification);
        for (AttachmentMain attachmentMain : attachmentMainList) {
            AttachmentMain newAttachmentMain = new AttachmentMain();
            newAttachmentMain.setfId(IDGenerate.generate());
            newAttachmentMain.setfModelName(attachmentMain.getfModelName());
            newAttachmentMain.setfModelId(newFid);
            newAttachmentMain.setfKey(attachmentMain.getfKey());
            newAttachmentMain.setfFileName(attachmentMain.getfFileName());
            newAttachmentMain.setfFileLink(attachmentMain.getfFileLink());
            newAttachmentMain.setfFileSize(attachmentMain.getfFileSize());
            newAttachmentMain.setfFile(attachmentMain.getfFile());
            newAttachmentMain.setfIsoFlag(0);
            newAttachmentMain.setfDocumentation(0);
            newAttachmentMain.setfDisplay(0);
            newAttachmentMain.setfCreateTime(new Date());
            attachmentMainService.save(newAttachmentMain);
        }
    }


    private JSONObject getPostData() {
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
