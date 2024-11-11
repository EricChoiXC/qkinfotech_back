package com.qkinfotech.core.tendering.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.tendering.model.apps.finalization.AppsFinalizationResultPackage;
import com.qkinfotech.core.tendering.model.apps.meeting.AppsMeetingKickoff;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingMain;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingPackage;
import com.qkinfotech.core.tendering.model.apps.pre.AppsPreAuditMeeting;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.core.tendering.model.attachment.AttachmentPackage;
import com.qkinfotech.core.tendering.service.AppsMeetingPackageService;
import com.qkinfotech.core.tendering.service.ProjectMainUtilService;
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
 * 启动会
 */

@RestController
@RequestMapping("/meeting/kickoff")
public class MeetingKickoffController {

    @Autowired
    protected HttpServletRequest request;
    @Autowired
    protected SimpleResult result;
    @Autowired
    protected HttpServletResponse response;
    @Autowired
    protected SimpleService<MeetingMain> meetingMainService;
    @Autowired
    protected SimpleService<MeetingPackage> meetingPackageService;
    @Autowired
    protected SimpleService<AppsMeetingKickoff> appsMeetingKickoffService;
    @Autowired
    protected SimpleService<AttachmentPackage> attachmentPackageService;
    @Autowired
    protected SimpleService<AppsProjectMain> appsProjectMainService;
    @Autowired
    protected AppsMeetingPackageService appsMeetingPackageService;

    @Autowired
    protected Bean2Json bean2json;

    @Autowired
    protected Json2Bean json2bean;

    @Autowired
    protected ProjectMainUtilService projectMainUtilService;


    @RequestMapping("/save")
    @ResponseBody
    public void save() throws Exception {
        JSONObject body = getPostData();
        //启动会
        String fId = body.getString("fId");
        AppsMeetingKickoff meetingKickoff = new AppsMeetingKickoff();
        if (StringUtil.isNotNull(fId)) {
            meetingKickoff = appsMeetingKickoffService.getById(fId);
        }
        if (meetingKickoff == null) {
            meetingKickoff = new AppsMeetingKickoff();
            meetingKickoff.setfId(body.getString("fId"));
        }
        meetingKickoff.setfIsOpen("1");
        meetingKickoff.setfCreateTime(new Date());
        //项目
        AppsProjectMain projectMain = body.getObject("fProjectMain", AppsProjectMain.class);
        projectMain = appsProjectMainService.getById(projectMain.getfId());
        if(null != projectMain){
            projectMain.setfIsProjectStart(true);
            appsProjectMainService.save(projectMain);
        }
        meetingKickoff.setfMainId(projectMain);
        appsMeetingKickoffService.save(meetingKickoff);

        //会议main
        MeetingMain main = new MeetingMain();
        if (StringUtil.isNotNull(fId)) {
            main = meetingMainService.getById(fId);
        }
        if (main == null) {
            main = new MeetingMain();
            main.setfId(meetingKickoff.getfId());
        }
        main.setfModelId(meetingKickoff.getfId());
        main.setfModelName("AppsMeetingKickoff");
        main.setfPlace(body.getString("fMeetingPlace"));
        main.setfName(body.getString("fName"));
        main.setfStartTime(new Date(body.getLong("fMeetingStartTime")));
        main.setfFinishTime(new Date(body.getLong("fMeetingEndTime")));
        main.setfEdited(true);
        meetingMainService.save(main);


        //包件信息
        /* 2024-08-20 更新时的包件优化处理，避免重复添加包件 */
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
        Set<String> packageIds = new HashSet<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            AppsProjectPackage projectPackage = jsonObject.to(AppsProjectPackage.class);
            if (deleteIds.containsKey(projectPackage.getfId())) {
                packageIds.add(projectPackage.getfId());
                deleteIds.remove(projectPackage.getfId());
            } else {
                MeetingPackage meetingPackage = new MeetingPackage();
                meetingPackage.setfMeetingId(main);
                meetingPackage.setfPackageId(projectPackage);
                meetingPackageService.save(meetingPackage);
                packageIds.add(projectPackage.getfId());
            }
        }
        for (String id : deleteIds.keySet()) {
            meetingPackageService.delete(deleteIds.get(id));
        }
        //删除本会议附件中，无效的包件关联
        //查询条件：1.附件的fAttachmentId.fModelName=AppsMeetingKickoff;fAttachmentId.fModelId=meeting.fId;fPackageId.fId not in (packageIds)
        Specification<AttachmentPackage> spec = new Specification<AttachmentPackage>() {
            @Override
            public Predicate toPredicate(Root<AttachmentPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList();
                predicates.add(criteriaBuilder.not(root.get("fPackageId").get("fId").in(packageIds)));
                predicates.add(criteriaBuilder.equal(root.get("fAttachmentId").get("fModelName"), "com.qkinfotech.core.tendering.model.apps.meeting.AppsMeetingKickoff"));
                predicates.add(criteriaBuilder.equal(root.get("fAttachmentId").get("fModelId"), fId));
                return query.where(predicates.toArray(new Predicate[predicates.size()])).getRestriction();
            }
        };
        List<AttachmentPackage> deletePackages = attachmentPackageService.findAll(spec);
        attachmentPackageService.delete(deletePackages.toArray(new AttachmentPackage[deletePackages.size()]));

    }


    @RequestMapping("/meetinglist")
    @ResponseBody
    public void meetinglist() throws Exception {
        JSONObject body = getPostData();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ; //使用了默认的格式创建了一个日期格式化对象。
        //通过项目id 找到 启动会
        String fId = body.getString("fId");
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fMainId.fId",fId);
        queryJson.put("eq",equal);
        query.put("query",queryJson);
        List<AppsMeetingKickoff> meetingList = appsMeetingKickoffService.findAll(JSONQuerySpecification.getSpecification(query));
        //启动会 对应包件信息
        JSONArray array = new JSONArray();
        Map<String, MeetingMain> meetingMainMap = new HashMap<>();
        List<String> ids = new ArrayList<>();
        for (AppsMeetingKickoff meeting : meetingList) {
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
        for(AppsMeetingKickoff meeting : meetingList){
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
                obj.put("json", bean2json.toJson(main));
            }
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

    /**
     *  获取只有发过入围结果的包件
     * @throws Exception
     */
    @RequestMapping("/getMeetingPackages")
    @ResponseBody
    public void getMeetingPackages() throws Exception {
        JSONObject body = getPostData();
        JSONArray array = new JSONArray();
        String id = body.getString("fId");
        List<AppsProjectPackage> packages = projectMainUtilService.getPackagesByMainId(id);
        for (AppsProjectPackage projectPackage : packages) {
            List<AppsFinalizationResultPackage> finalizationResult = projectMainUtilService.getFinalizationResult(projectPackage);
            if (!finalizationResult.isEmpty()) {
                array.add(projectPackage);
            }
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