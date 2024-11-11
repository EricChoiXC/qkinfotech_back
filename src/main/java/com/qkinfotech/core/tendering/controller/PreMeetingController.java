package com.qkinfotech.core.tendering.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingMain;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingPackage;
import com.qkinfotech.core.tendering.model.apps.pre.AppsPreAuditMeeting;
import com.qkinfotech.core.tendering.model.apps.pre.AppsPreAuditOutside;
import com.qkinfotech.core.tendering.model.apps.pre.AppsPreAuditOwner;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import com.qkinfotech.core.tendering.model.attachment.AttachmentPackage;
import com.qkinfotech.core.tendering.model.masterModels.MasterDataMeetingType;
import com.qkinfotech.core.tendering.service.AppsMeetingPackageService;
import com.qkinfotech.util.StringUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 资格预审会议
 */

@RestController
@RequestMapping("/pre/meeting/")
public class PreMeetingController {

    @Autowired
    protected HttpServletRequest request;
    @Autowired
    protected SimpleResult result;
    @Autowired
    protected SimpleService<AppsPreAuditMeeting> appsPreAuditMeetingService;
    @Autowired
    protected SimpleService<MeetingMain> meetingMainService;
    @Autowired
    protected SimpleService<MeetingPackage> meetingPackageService;
    @Autowired
    protected SimpleService<AppsPreAuditOutside> appsPreAuditOutsideService;
    @Autowired
    protected SimpleService<AppsPreAuditOwner> appsPreAuditOwnerService;
    @Autowired
    protected SimpleService<AttachmentPackage> attachmentPackageService;
    @Autowired
    protected AppsMeetingPackageService appsMeetingPackageService;

    @Autowired
    protected Bean2Json bean2json;

    @Autowired
    protected Json2Bean json2bean;

    @RequestMapping("/save")
    @ResponseBody
    @CrossOrigin("http://localhost:3000")
    public void save() throws Exception {
        JSONObject body = getPostData();
        String fId = body.getString("fId");
        AppsPreAuditMeeting auditMeeting = new AppsPreAuditMeeting();
        if (StringUtil.isNotNull(fId)) {
            auditMeeting = appsPreAuditMeetingService.getById(fId);
        }
        if (auditMeeting == null) {
            auditMeeting = new AppsPreAuditMeeting();
            auditMeeting.setfId(fId);
        }
        //项目
        AppsProjectMain projectMain = body.getObject("fProjectMain", AppsProjectMain.class);
        auditMeeting.setfMainId(projectMain);
        //会议类型
        MasterDataMeetingType meetingType = body.getObject("fMeetingType", MasterDataMeetingType.class);
        auditMeeting.setfMeetingType(meetingType);
        //会议地点
        auditMeeting.setfMeetingPlace(body.getString("fMeetingPlace"));
        //会议时间
        auditMeeting.setfMeetingStartTime(new Date(body.getLong("fMeetingStartTime")));
        auditMeeting.setfMeetingEndTime(new Date(body.getLong("fMeetingEndTime")));
        //备注
        auditMeeting.setfRemark(body.getString("fRemark"));
        //业主专家
        auditMeeting.setfOwnerExpert(body.getString("fOwnerExpert"));
        //外聘专家
        auditMeeting.setfOutsideExpert(body.getJSONArray("fOutsideExpert"));
        appsPreAuditMeetingService.save(auditMeeting);

        //会议主表
        MeetingMain main = new MeetingMain();
        if (StringUtil.isNotNull(fId)) {
            main = meetingMainService.getById(fId);
        }
        if (main == null) {
            main = new MeetingMain();
            main.setfId(auditMeeting.getfId());
        }
        main.setfModelId(auditMeeting.getfId());
        main.setfModelName("AppsPreAuditMeeting");
        main.setfPlace(auditMeeting.getfMeetingPlace());
        main.setfStartTime(auditMeeting.getfMeetingStartTime());
        main.setfFinishTime(auditMeeting.getfMeetingEndTime());
        main.setfEdited(true);
        meetingMainService.save(main);

        //包件信息
        /* 2024-08-20 更新时的包件优化处理，避免重复添加包件 */
        /* 2024-10-09 由于会议包件信息可再更改，因此改成添加，删除，更新方式 */
        Specification<MeetingPackage> spec2 = new Specification<MeetingPackage>() {
            @Override
            public Predicate toPredicate(Root<MeetingPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("fMeetingId").get("fId"), fId);
            }
        };
        List<MeetingPackage> packageList = meetingPackageService.findAll(spec2);
        Map<String, String> deleteIds = new HashMap<>();
        packageList.forEach(val -> {
            deleteIds.put(val.getfPackageId().getfId(), val.getfId());
        });
        JSONArray array = body.getJSONArray("packageIds");
        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            AppsProjectPackage projectPackage = jsonObject.to(AppsProjectPackage.class);
            if (deleteIds.containsKey(projectPackage.getfId())) {
                deleteIds.remove(projectPackage.getfId());
            } else {
                MeetingPackage meetingPackage = new MeetingPackage();
                meetingPackage.setfMeetingId(main);
                meetingPackage.setfPackageId(projectPackage);
                meetingPackageService.save(meetingPackage);
            }
        }
        if (StringUtil.isNotNull(fId)) {
            for (String id : deleteIds.keySet()) {
                meetingPackageService.delete(deleteIds.get(id));
                Specification<AttachmentPackage> spec = new Specification<AttachmentPackage>() {
                    @Override
                    public Predicate toPredicate(Root<AttachmentPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        Predicate and1 = criteriaBuilder.equal(root.get("fAttachmentId").get("fModelId"), fId);
                        Predicate and2 = criteriaBuilder.equal(root.get("fAttachmentId").get("fModelName"), "com.qkinfotech.core.tendering.model.apps.pre.AppsPreAuditMeeting");
                        Predicate and3 = criteriaBuilder.equal(root.get("fPackageId").get("fId"), id);
                        return criteriaBuilder.and(and1, and2, and3);
                    }
                };
                attachmentPackageService.delete(spec);
            }
        }
    }

    @RequestMapping("/meetinglist")
    @ResponseBody
    @CrossOrigin("http://localhost:3000")
    public void list() throws Exception {
        JSONObject body = getPostData();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ; //使用了默认的格式创建了一个日期格式化对象。
        //通过项目id 找到 资格预审会议记录
        String fId = body.getString("fId");
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fMainId.fId",fId);
        queryJson.put("eq",equal);
        query.put("query",queryJson);
        List<AppsPreAuditMeeting> meetingList = appsPreAuditMeetingService.findAll(JSONQuerySpecification.getSpecification(query));
        //资格预审会议 对应包件信息
        JSONArray array = new JSONArray();
        Map<String, MeetingMain> meetingMainMap = new HashMap<>();
        List<String> ids = new ArrayList<>();
        for (AppsPreAuditMeeting meeting : meetingList) {
            ids.add(meeting.getfId());
        }
        JSONObject query1 = new JSONObject();
        JSONObject queryJson1 = new JSONObject();
        JSONObject in1 = new JSONObject();
        in1.put("fModelId", ids);
        queryJson1.put("in", in1);
        query1.put("query", queryJson1);
        List<MeetingMain> mains = meetingMainService.findAll(JSONQuerySpecification.getSpecification(query1));
        for (MeetingMain main : mains) {
            meetingMainMap.put(main.getfModelId(), main);
        }
        for(AppsPreAuditMeeting meeting : meetingList){
            JSONObject obj = new JSONObject();
            obj.put("id", meeting.getfId());
            if (meeting.getfMainId() != null) {
                obj.put("projId", meeting.getfMainId().getfId());
            }
            obj.put("type",meeting.getfMeetingType() != null ? meeting.getfMeetingType().getfName() : "");
            obj.put("meetingRoom",meeting.getfMeetingPlace());
            obj.put("meetingStartTime",dateFormat.format(meeting.getfMeetingStartTime()));
            obj.put("meetingEndTime",dateFormat.format(meeting.getfMeetingEndTime()));
            obj.put("remarks",meeting.getfRemark());
            obj.put("fOwnerExpert", meeting.getfOwnerExpert());
            obj.put("fOutsideExpert", meeting.getfOutsideExpert());
            if (meetingMainMap.containsKey(meeting.getfId())) {
                obj.put("fInId", meetingMainMap.get(meeting.getfId()).getfInId());
                obj.put("fFrom", meetingMainMap.get(meeting.getfId()).getfFrom());
                obj.put("fEdited", meetingMainMap.get(meeting.getfId()).getfEdited());
            }
            //包件信息
            Specification<MeetingPackage> spec = appsMeetingPackageService.hasFModelId(meeting.getfId());
            List<MeetingPackage> meetingInfo = meetingPackageService.findAll(spec);
            String packageName = "";
            for(MeetingPackage meetingPackage : meetingInfo){
                packageName += ("包件" + meetingPackage.getfPackageId().getfIndex() + ":" + meetingPackage.getfPackageId().getfName()+";");
            }
            obj.put("packageName",packageName);
            array.add(obj);
        }
//        Specification<MeetingPackage> spec = hasFModelIds(fModelIds);
//        List<MeetingPackage> meetingInfo = meetingPackageService.findAll(spec);

        result.from(array);
        result.getResponse().setCharacterEncoding("UTF-8");
    }

    private JSONObject getPostData() {
        if (!"POST".equals(request.getMethod())) {
            return new JSONObject();
        }
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
            return JSONObject.parseObject(txt);

        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @RequestMapping("/load")
    @ResponseBody
    public void load() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String fId = request.getParameter("fId");
        JSONObject body = getPostData();
        if (body.containsKey("fId")) {
            fId = body.getString("fId");
        }
        AppsPreAuditMeeting preMeeting = appsPreAuditMeetingService.getById(fId);
        JSONObject json = bean2json.toJson(preMeeting);
        json.put("fStartTimeNum", preMeeting.getfMeetingStartTime().getTime());
        json.put("fFinishTimeNum", preMeeting.getfMeetingEndTime().getTime());
        result.from(json);

    }
}