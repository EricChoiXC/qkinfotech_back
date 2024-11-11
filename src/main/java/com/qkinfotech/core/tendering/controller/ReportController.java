package com.qkinfotech.core.tendering.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingMain;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingPackage;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.core.tendering.model.apps.report.AppsReport;
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
 * 项目汇报评审
 */

@RestController
@RequestMapping("/report/")
public class ReportController {

    @Autowired
    protected HttpServletRequest request;
    @Autowired
    protected SimpleResult result;
    @Autowired
    protected SimpleService<AppsReport> appsReportService;
    @Autowired
    protected SimpleService<MeetingMain> meetingMainService;
    @Autowired
    protected SimpleService<MeetingPackage> meetingPackageService;
    @Autowired
    protected AppsMeetingPackageService appsMeetingPackageService;
    @Autowired
    protected SimpleService<AttachmentPackage> attachmentPackageService;

    @RequestMapping("/save")
    @ResponseBody
    @CrossOrigin("http://localhost:3000")
    public void save() throws Exception {
        JSONObject body = getPostData();
        //项目汇报评审
        String fId = body.getString("fId");
        //项目
        AppsReport report = new AppsReport();
        if (StringUtil.isNotNull(fId)) {
            report = appsReportService.getById(fId);
        }
        if (report == null) {
            report = new AppsReport();
            report.setfId(body.getString("fId"));
        }
        AppsProjectMain projectMain = body.getObject("fProjectMain", AppsProjectMain.class);
        report.setfMainId(projectMain);
        //会议类型
        MasterDataMeetingType meetingType = body.getObject("fMeetingType", MasterDataMeetingType.class);
        report.setfMeetingType(meetingType);
        report.setfCreateTime(new Date());
        report.setfIsOpen("1");
        report.setfOwnerExpert(body.getString("fOwnerExpert"));
        report.setfOutsideExpert(body.getJSONArray("fOutsideExpert"));
        report.setfRemark(body.getString("fRemark"));
        appsReportService.save(report);

        //会议主表
        MeetingMain main = new MeetingMain();
        if (StringUtil.isNotNull(fId)) {
            main = meetingMainService.getById(fId);
        }
        if (main == null) {
            main = new MeetingMain();
            main.setfId(fId);
        }
        main.setfModelId(report.getfId());
        main.setfModelName("AppsReport");
        main.setfPlace(body.getString("fMeetingPlace"));
        main.setfName(body.getString("fName"));
        main.setfStartTime(new Date(body.getLong("fMeetingStartTime")));
        main.setfFinishTime(new Date(body.getLong("fMeetingEndTime")));
        main.setfEdited(true);
        meetingMainService.save(main);

        //包件信息
        /* 2024-08-20 更新时的包件优化处理，避免重复添加包件 */
        Specification<MeetingPackage> spec = new Specification<MeetingPackage>() {
            @Override
            public Predicate toPredicate(Root<MeetingPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("fMeetingId").get("fId"), fId);
            }
        };
        List<MeetingPackage> packageList = meetingPackageService.findAll(spec);
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
                Specification<AttachmentPackage> spec2 = new Specification<AttachmentPackage>() {
                    @Override
                    public Predicate toPredicate(Root<AttachmentPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        Predicate and1 = criteriaBuilder.equal(root.get("fAttachmentId").get("fModelId"), fId);
                        Predicate and2 = criteriaBuilder.equal(root.get("fAttachmentId").get("fModelName"), "com.qkinfotech.core.tendering.model.apps.report.AppsReport");
                        Predicate and3 = criteriaBuilder.equal(root.get("fPackageId").get("fId"), id);
                        return criteriaBuilder.and(and1, and2, and3);
                    }
                };
                attachmentPackageService.delete(spec2);
            }
        }

    }

    @RequestMapping("/meetinglist")
    @ResponseBody
    @CrossOrigin("http://localhost:3000")
    public void meetinglist() throws Exception {
        JSONObject body = getPostData();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ; //使用了默认的格式创建了一个日期格式化对象。
        //通过项目id 找到 汇报评审
        String fId = body.getString("fId");
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fMainId.fId",fId);
        queryJson.put("eq",equal);
        query.put("query",queryJson);
        List<AppsReport> meetingList = appsReportService.findAll(JSONQuerySpecification.getSpecification(query));
        //汇报评审 对应包件信息
        JSONArray array = new JSONArray();
        Map<String, MeetingMain> meetingMainMap = new HashMap<>();
        List<String> ids = new ArrayList<>();
        for (AppsReport meeting : meetingList) {
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
        for(AppsReport meeting : meetingList){
            JSONObject obj = new JSONObject();
            Specification<MeetingPackage> spec = appsMeetingPackageService.hasFModelId(meeting.getfId());
            List<MeetingPackage> meetingInfo = meetingPackageService.findAll(spec);
            //会议信息
            MeetingMain main = meetingMainMap.get(meeting.getfId());
            if (main != null) {
                obj.put("id", main.getfId());
                obj.put("meetingRoom", main.getfPlace());
                obj.put("meetingStartTime", dateFormat.format(main.getfStartTime()));
                obj.put("meetingEndTime", dateFormat.format(main.getfFinishTime()));
                obj.put("fName", main.getfName());
                if (meetingMainMap.containsKey(meeting.getfId())) {
                    obj.put("fInId", meetingMainMap.get(meeting.getfId()).getfInId());
                    obj.put("fFrom", meetingMainMap.get(meeting.getfId()).getfFrom());
                    obj.put("fEdited", meetingMainMap.get(meeting.getfId()).getfEdited());
                }
            }
            obj.put("type", meeting.getfMeetingType() != null ? meeting.getfMeetingType().getfName() : "");
            obj.put("remarks", meeting.getfRemark());
            obj.put("fId", meeting.getfId());
            obj.put("fOwnerExpert", meeting.getfOwnerExpert());
            obj.put("fOutsideExpert", meeting.getfOutsideExpert());

            //包件信息
            String packageName = "";
                if(!meetingInfo.isEmpty()){
                    for(MeetingPackage meetingPackage : meetingInfo){
                        packageName += ("包件" + meetingPackage.getfPackageId().getfIndex() + ":" + meetingPackage.getfPackageId().getfName()+";");
                    }
                }
            obj.put("packageName",packageName);
            array.add(obj);
        }

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
}