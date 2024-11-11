package com.qkinfotech.core.tendering.attachment.controller;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.tendering.iso.model.IsoApproval;
import com.qkinfotech.core.tendering.model.apps.meeting.AppsMeetingKickoff;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingMain;
import com.qkinfotech.core.tendering.model.apps.pre.AppsPreAuditMeeting;
import com.qkinfotech.core.tendering.model.apps.report.AppsReport;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import com.qkinfotech.core.tendering.model.attachment.AttachmentPackage;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/attachmentController")
public class AttachmentController {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected Json2Bean json2bean;

    @Autowired
    protected Bean2Json bean2json;

    @Autowired
    protected SimpleService<AttachmentMain> attachmentMainService;

    @Autowired
    protected SimpleService<AttachmentPackage> attachmentPackageService;

    @Autowired
    protected SimpleService<IsoApproval> isoApprovalService;

    @Autowired
    protected SimpleService<MeetingMain> meetingMainService;

    @Autowired
    protected SimpleService<AppsMeetingKickoff> appsMeetingKickoffService;

    @Autowired
    protected SimpleService<AppsPreAuditMeeting> appsPreAuditMeetingService;

    @Autowired
    protected SimpleService<AppsReport> appsReportService;


    @PostMapping("/finishIsoApproval")
    @ResponseBody
    public void finishIsoApproval() throws Exception {
        JSONObject body = getPostData();
        String isoId = body.getString("id");
        IsoApproval iso = isoApprovalService.getById(isoId);
        if (iso != null) {
            Set<AttachmentMain> attachments = iso.getfAttachments();
            for (AttachmentMain att : attachments) {
                att.setfIsoFlag(Integer.valueOf(1));
                attachmentMainService.save(att);
                Set<AttachmentPackage> packages = att.getfPackages();
                if (!packages.isEmpty()) {
                    for (AttachmentPackage pack : packages) {
                        pack.setfIsoFlag(Integer.valueOf(1));
                        attachmentPackageService.save(pack);
                    }
                }
            }
            Specification<AttachmentMain> spec = new Specification<AttachmentMain>() {
                @Override
                public Predicate toPredicate(Root<AttachmentMain> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate eq1 = criteriaBuilder.equal(root.get("fModelId"), isoId);
                    Predicate eq2 = criteriaBuilder.equal(root.get("fModelName"), "com.qkinfotech.core.tendering.iso.model.IsoApproval");
                    return criteriaBuilder.and(eq1, eq2);
                }
            };
            List<AttachmentMain> list = attachmentMainService.findAll(spec);
            for (AttachmentMain att : list) {
                att.setfIsoFlag(Integer.valueOf(1));
                attachmentMainService.save(att);
                Set<AttachmentPackage> packages = att.getfPackages();
                if (!packages.isEmpty()) {
                    for (AttachmentPackage pack : packages) {
                        pack.setfIsoFlag(Integer.valueOf(1));
                        attachmentPackageService.save(pack);
                    }
                }
            }
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

    /**
     * 附件删除，已归档附件不删除，只记为不可见
     * @throws Exception
     */
    @RequestMapping("/delete")
    @ResponseBody
    private void delete() throws Exception {
        String id = request.getParameter("id");
        JSONObject body = getPostData();
        if (body.containsKey("id")) {
            id = body.getString("id");
        }
        if (StringUtils.hasText(id)) {
            AttachmentMain att = attachmentMainService.getById(id);
            if (att != null) {
                if (1 == att.getfDocumentation()) {
                    att.setfDisplay(1);
                    attachmentMainService.save(att);
                } else {
                    attachmentMainService.delete(att.getfId());
                }
            }
        }
    }

    /**
     * 归档后，所有附件的归档标记fDocumentation变更为1
     * @throws Exception
     */
    @PostMapping("/attmentDocument")
    @ResponseBody
    private void attmentDocument() throws Exception {
        JSONObject json = getPostData();
        String id = json.getString("id");
        if (StringUtils.hasText(id)) {

            List<String> meetingIds = getProjectMeetingIds(id);

            Specification<AttachmentMain> spec = new Specification<AttachmentMain>() {
                @Override
                public Predicate toPredicate(Root<AttachmentMain> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    CriteriaBuilder.In in = criteriaBuilder.in(root.get("fModelId"));
                    in.value(id);
                    if (!meetingIds.isEmpty()) {
                        in.value(meetingIds);
                    }
                    return in;
                }
            };
            List<AttachmentMain> list = attachmentMainService.findAll(spec);
            for (AttachmentMain att : list) {
                att.setfDocumentation(1);
                attachmentMainService.save(att);
            }
        }
    }

    public List<String> getProjectMeetingIds (String projectId) {
        List<String> ids = new ArrayList<>();

        Specification<AppsPreAuditMeeting> spec = new Specification<AppsPreAuditMeeting>() {
            @Override
            public Predicate toPredicate(Root<AppsPreAuditMeeting> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("fMainId").get("fId"), projectId);
            }
        };
        List<AppsPreAuditMeeting> meetingList = appsPreAuditMeetingService.findAll(spec);
        List<String> meetingIds = meetingList.stream().map(AppsPreAuditMeeting::getfId).toList();
        ids.addAll(meetingIds);

        Specification<AppsMeetingKickoff> spec2 = new Specification<AppsMeetingKickoff>() {
            @Override
            public Predicate toPredicate(Root<AppsMeetingKickoff> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("fMainId").get("fId"), projectId);
            }
        };
        List<AppsMeetingKickoff> meetingList2 = appsMeetingKickoffService.findAll(spec2);
        List<String> meetingIds2 = meetingList2.stream().map(AppsMeetingKickoff::getfId).toList();
        ids.addAll(meetingIds2);

        Specification<AppsReport> spec3 = new Specification<AppsReport>() {
            @Override
            public Predicate toPredicate(Root<AppsReport> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("fMainId").get("fId"), projectId);
            }
        };
        List<AppsReport> meetingList3 = appsReportService.findAll(spec3);
        List<String> meetingIds3 = meetingList3.stream().map(AppsReport::getfId).toList();
        ids.addAll(meetingIds3);

        return ids;
    }
}
