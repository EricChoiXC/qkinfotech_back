package com.qkinfotech.core.tendering.controller;


import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.auth.util.ApiRsaUtil;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.tendering.interfaceConfig.InterfaceLog;
import com.qkinfotech.core.tendering.model.apps.meeting.AppsMeetingKickoff;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingMain;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingPackage;
import com.qkinfotech.core.tendering.model.apps.pre.AppsPreAuditMeeting;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.core.tendering.model.apps.report.AppsReport;
import com.qkinfotech.core.tendering.model.masterModels.MasterDataMeetingType;
import com.qkinfotech.util.StringUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * 会议信息同步接口
 * @author 蔡咏钦
 */
@RestController
@RequestMapping("/meeting/web/service")
public class MeetingWebServiceController {

    @Autowired
    private SimpleService<MeetingMain> meetingMainService;

    @Autowired
    private SimpleService<MeetingPackage> meetingPackageService;

    @Autowired
    private SimpleService<AppsPreAuditMeeting> appsPreAuditMeetingService;

    @Autowired
    private SimpleService<AppsMeetingKickoff> appsMeetingKickoffService;

    @Autowired
    private SimpleService<AppsReport> appsReportService;

    @Autowired
    private SimpleService<AppsProjectMain> appsProjectMainService;

    @Autowired
    private SimpleService<MasterDataMeetingType> masterDataMeetingTypeService;

    @Autowired
    private SimpleService<InterfaceLog> interfaceLogService;

    private String kickoffMeetingModelName = "AppsMeetingKickoff";

    private String reportModelName = "AppsReport";

    private String preAuditMeetingModelName = "AppsPreAuditMeeting";

    private String ADD = "add";
    private String UPDATE = "update";
    private String DELETE = "delete";
    private String MEETING_TYPE_PRE = "1";
    private String MEETING_TYPE_KICKOFF = "2";
    private String MEETING_TYPE_REPORT = "3";
    private String MEETING_TYPE_REPORT_END = "4";
    private String MEETING_TYPE_OTHER = "5";

    private Map<String, String> MEETING_TYPE_NAME_SET = Stream.of(new Object[][] {
            {MEETING_TYPE_PRE, "资格预审"},
            {MEETING_TYPE_REPORT, "中期"},
            {MEETING_TYPE_REPORT_END, "终期"},
            {MEETING_TYPE_OTHER, "其他"}
    }).collect(toMap(data -> (String) data[0], data -> (String) data[1]));


    /**
     * 会议信息同步接口
     * {
     *     "meetingTime": "2024-10-17",
     *     "inId": "475",
     *     "s": "",
     *     "projectNo": "2403001008",
     *     "meetingPlace": "第一会议室(1901室)",
     *     "t": 1729057240427,
     *     "meetingStartTime": "2024-10-17 08:30:00.000",
     *     "inMeetingType": 1,
     *     "action": "update",
     *     "pmId": "01JA7SMEP7E31P2RF6YNWEAGRB",
     *     "meetingType": 1,
     *     "meetingEndTime": "2024-10-17 13:00:00.000"
     * }
     */
    @PostMapping ("/syncMeetingInfo")
    public JSONObject syncMeetingInfo (HttpServletRequest request, HttpServletRequest response, @RequestBody JSONObject requestJson) throws Exception {
        String requestStr = requestJson.getString("s");

        String jsonString = ApiRsaUtil.decrypt(requestStr);
        JSONObject json = JSONObject.parseObject(jsonString);

        JSONObject result = new JSONObject();
        if (!json.containsKey("action")) {
            result.put("status", "E");
            result.put("message", "操作值未提供");
            return result;
        }
        String action = json.getString("action").toLowerCase();
        InterfaceLog log = new InterfaceLog();
        log.setfInterfaceName("oa会议同步");
        log.setfInputParameter(jsonString);
        log.setfCreateTime(new Date());
        try {
            if (ADD.equals(action)) {
                addSyncMeeting(json, result);
            } else if (UPDATE.equals(action)) {
                updateSyncMeeting(json, result);
            } else if (DELETE.equals(action)) {
                deleteSyncMeeting(json, result);
            } else {
                result.put("status", "E");
                result.put("message", "操作值未提供或错误的操作值");
            }
            log.setfInterfaceStatus("1");
            log.setfInterfaceInfo(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "E");
            result.put("message", e.getMessage());
            log.setfInterfaceStatus("2");
            log.setfInterfaceInfo(result.toString());
        }
        interfaceLogService.save(log);
        return result;
    }

    /**
     * 添加会议室预定信息
     * @param json 预定信息
     * @param result 结果
     */
    public void addSyncMeeting(JSONObject json, JSONObject result) throws Exception {
        //内网id
        String inId = json.getString("inId");
        if (StringUtil.isNull(inId)) {
            result.put("status", "E");
            result.put("message", "未提供内网会议id");
            return;
        }

        //所属项目编号
        String projectNo = json.getString("projectNo");
        if (StringUtil.isNull(projectNo)) {
            result.put("status", "E");
            result.put("message", "未提供所属项目编号");
            return;
        }

        //会议类型
        String meetingType = json.getString("meetingType");
        if (StringUtil.isNull(meetingType)) {
            result.put("status", "E");
            result.put("message", "未提供会议类型");
            return;
        }
        if (!MEETING_TYPE_PRE.equals(meetingType) && !MEETING_TYPE_KICKOFF.equals(meetingType) && !MEETING_TYPE_REPORT.equals(meetingType) && !MEETING_TYPE_REPORT_END.equals(meetingType)) {
            result.put("status", "E");
            result.put("message", "错误的会议类型");
            return;
        }

        MeetingMain meetingMain = loadMeetingMain(inId);
        if (meetingMain != null) {
            result.put("status", "E");
            result.put("message", "该会议已被登记，请使用更新操作而非新建操作");
            return;
        }

        AppsProjectMain appsProjectMain = loadProjectMain(projectNo);
        if (appsProjectMain == null) {
            result.put("status", "E");
            result.put("message", "项目不存在");
            return;
        }

        //初始化会议记录
        meetingMain = new MeetingMain();
        meetingMain.getfId();
        meetingMain.setfModelId(meetingMain.getfId());
        meetingMain.setfInId(inId);
        meetingMain.setfFrom("oa");
        meetingMain.setfEdited(false);
        meetingMain.setfPlace(json.getString("meetingPlace"));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date preDate = df.parse(json.getString("meetingTime"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        if (json.containsKey("meetingStartTime")) {
            Date startTime = sdf.parse(json.getString("meetingStartTime"));
            meetingMain.setfStartTime(startTime);
        } else {
            meetingMain.setfStartTime(preDate);
        }
        if (json.containsKey("meetingEndTime")) {
            Date endTime = sdf.parse(json.getString("meetingEndTime"));
            meetingMain.setfFinishTime(endTime);
        } else {
            meetingMain.setfStartTime(preDate);
        }

        if (MEETING_TYPE_PRE.equals(meetingType)) {
            meetingMain.setfModelName(preAuditMeetingModelName);
            savePreMeeting(json, meetingMain, appsProjectMain);
        } else if (MEETING_TYPE_KICKOFF.equals(meetingType)) {
            meetingMain.setfModelName(kickoffMeetingModelName);
            saveKickoffMeeting(json, meetingMain, appsProjectMain);
        } else if (MEETING_TYPE_REPORT.equals(meetingType) || MEETING_TYPE_REPORT_END.equals(meetingType)) {
            meetingMain.setfModelName(reportModelName);
            saveReportMeeting(json, meetingMain, appsProjectMain, meetingType);
        }
        meetingMainService.save(meetingMain);
        result.put("status", "S");
        result.put("message", meetingMain.getfId());
    }

    /**
     * 更新会议室预定信息（内网当前只更新会议地点和会议日期）
     */
    public void updateSyncMeeting(JSONObject json, JSONObject result) throws Exception {
        //内网id
        String inId = json.getString("inId");
        if (StringUtil.isNull(inId)) {
            result.put("status", "E");
            result.put("message", "未提供内网会议id");
            return;
        }

        //会议记录
        MeetingMain meetingMain = loadMeetingMain(inId);
        if (meetingMain == null) {
            result.put("status", "E");
            result.put("message", "未查询到对应的会议记录，请联系管理员处理");
            return;
        }

        //不是通过内网生成的会议不更新
        if (!"oa".equals(meetingMain.getfFrom())) {
            result.put("status", "E");
            result.put("message", "该会议并非内网发起的会议室预定记录，无法删除");
            return;
        }

        //修改会议类型，所属项目的修改
        boolean changeMeetingTypeOrProj = isChangeMeetingTypeOrProj(json, meetingMain);
        //会议包件保存在meeting_package，会议附件在attachment_main中
        if (changeMeetingTypeOrProj) {
            meetingMain.setfInId(null);
            meetingMainService.save(meetingMain);
            Specification<MasterDataMeetingType> spec = new Specification<MasterDataMeetingType>() {
                @Override
                public Predicate toPredicate(Root<MasterDataMeetingType> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate predicate = criteriaBuilder.equal(root.get("fName"), "其他");
                    return query.where(predicate).getRestriction();
                }
            };
            List<MasterDataMeetingType> list = masterDataMeetingTypeService.findAll(spec);
            if (preAuditMeetingModelName.equals(meetingMain.getfModelName())) {
                AppsPreAuditMeeting pre = appsPreAuditMeetingService.getById(meetingMain.getfId());
                pre.setfMeetingType(list.get(0));
                appsPreAuditMeetingService.save(pre);
            } else if (reportModelName.equals(meetingMain.getfModelName())) {
                AppsReport report = appsReportService.getById(meetingMain.getfId());
                report.setfMeetingType(list.get(0));
                appsReportService.save(report);
            }
            addSyncMeeting(json, result);
        } else if (!changeMeetingTypeOrProj) {
            updateSyncMeetingBasic(json, meetingMain, result);
        }


    }

    /**
     * 删除会议室预定（需要会议来源是内网的才可删除）
     */
    public void deleteSyncMeeting(JSONObject json, JSONObject result) throws Exception {
        //内网id
        String inId = json.getString("inId");
        if (StringUtil.isNull(inId)) {
            result.put("status", "E");
            result.put("message", "未提供内网会议id");
            return;
        }

        //会议记录
        MeetingMain meetingMain = loadMeetingMain(inId);
        if (meetingMain == null) {
            result.put("status", "E");
            result.put("message", "未查询到对应的会议记录");
            return;
        }

        if (!"oa".equals(meetingMain.getfFrom())) {
            result.put("status", "E");
            result.put("message", "该会议并非内网发起的会议室预定记录，无法删除");
            return;
        }

        if (hasPackage(meetingMain.getfId())) {
            result.put("status", "E");
            result.put("message", "该会议已有关联，无法删除");
            return;
        }

        String fId = meetingMain.getfId();
        if (preAuditMeetingModelName.equals(meetingMain.getfModelName())) {
            appsPreAuditMeetingService.delete(fId);
        }
        if (kickoffMeetingModelName.equals(meetingMain.getfModelName())) {
            appsMeetingKickoffService.delete(fId);
        }
        if (reportModelName.equals(meetingMain.getfModelName())) {
            appsReportService.delete(fId);
        }
        meetingMainService.delete(meetingMain.getfId());
        result.put("status", "S");
        result.put("message", fId);
    }

    /**
     * 获取meetingMain
     */
    public MeetingMain loadMeetingMain(String inId) throws Exception {
        Specification<MeetingMain> spec = new Specification<MeetingMain>() {
            @Override
            public Predicate toPredicate(Root<MeetingMain> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.equal(root.get("fInId"), inId);
                return query.where(predicate).getRestriction();
            }
        };
        return meetingMainService.findOne(spec);
    }

    /**
     * 获取项目
     */
    public AppsProjectMain loadProjectMain(String projectNo) throws Exception {
        Specification<AppsProjectMain> spec = new Specification<AppsProjectMain>() {
            @Override
            public Predicate toPredicate(Root<AppsProjectMain> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.equal(root.get("fProtocolNo"), projectNo);
                return query.where(predicate).getRestriction();
            }
        };
        return appsProjectMainService.findOne(spec);
    }

    /**
     * 获取会议类型
     */
    public MasterDataMeetingType getMeetingType(String meetingType) throws Exception {
        Specification<MasterDataMeetingType> spec = new Specification<MasterDataMeetingType>() {
            @Override
            public Predicate toPredicate(Root<MasterDataMeetingType> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> list = new ArrayList<>();
                //list.add(criteriaBuilder.equal(root.get("fMeetingKey"), meetingType));
                list.add(criteriaBuilder.like(root.get("fName"), "%" + MEETING_TYPE_NAME_SET.get(meetingType) + "%"));
                list.add(criteriaBuilder.equal(root.get("fKey"), "16"));
                return query.where(list.toArray(new Predicate[list.size()])).getRestriction();
            }
        };
        return masterDataMeetingTypeService.findOne(spec);
    }

    /**
     * 资格预审会议
     */
    public void savePreMeeting(JSONObject json, MeetingMain meetingMain, AppsProjectMain appsProjectMain) throws Exception {
        AppsPreAuditMeeting appsPreAuditMeeting = new AppsPreAuditMeeting();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date preDate = df.parse(json.getString("meetingTime"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        if (json.containsKey("meetingStartTime")) {
            Date startTime = sdf.parse(json.getString("meetingStartTime"));
            appsPreAuditMeeting.setfMeetingStartTime(startTime);
        } else {
            meetingMain.setfStartTime(preDate);
        }
        if (json.containsKey("meetingEndTime")) {
            Date endTime = sdf.parse(json.getString("meetingEndTime"));
            appsPreAuditMeeting.setfMeetingEndTime(endTime);
        } else {
            meetingMain.setfStartTime(preDate);
        }

        appsPreAuditMeeting.setfId(meetingMain.getfId());
        appsPreAuditMeeting.setfMainId(appsProjectMain);
        appsPreAuditMeeting.setfMeetingPlace(json.getString("meetingPlace"));
        appsPreAuditMeeting.setfMeetingType(getMeetingType("1"));
        appsPreAuditMeetingService.save(appsPreAuditMeeting);
    }

    /**
     * 保存项目启动会
     */
    public void saveKickoffMeeting(JSONObject json, MeetingMain meetingMain, AppsProjectMain appsProjectMain) throws Exception {
        AppsMeetingKickoff appsMeetingKickoff = new AppsMeetingKickoff();
        appsMeetingKickoff.setfId(meetingMain.getfId());
        appsMeetingKickoff.setfMainId(appsProjectMain);
        appsMeetingKickoff.setfIsOpen("1");
        appsMeetingKickoffService.save(appsMeetingKickoff);

        //修改项目是否有启动会标识
        appsProjectMain.setfIsProjectStart(true);
        appsProjectMainService.save(appsProjectMain);
    }

    /**
     * 保存汇报评审
     */
    public void saveReportMeeting(JSONObject json, MeetingMain meetingMain, AppsProjectMain appsProjectMain, String meetingType) throws Exception {
        AppsReport appsReport = new AppsReport();
        appsReport.setfId(meetingMain.getfId());
        appsReport.setfMainId(appsProjectMain);
        appsReport.setfMeetingType(getMeetingType(meetingType));
        appsReport.setfCreateTime(new Date());
        appsReportService.save(appsReport);

        //修改项目是否有汇报评审标识
        appsProjectMain.setfIsProjectReportReview(true);
        appsProjectMainService.save(appsProjectMain);
    }

    /**
     * 是否修改会议类型，所属项目
     */
    public boolean isChangeMeetingTypeOrProj(JSONObject json, MeetingMain meetingMain) {
        boolean changeMeetingTypeOrProj = false;
        if (("1".equals(json.getString("meetingType")) && !preAuditMeetingModelName.equals(meetingMain.getfModelName()))
                || ("2".equals(json.getString("meetingType")) && !kickoffMeetingModelName.equals(meetingMain.getfModelName()))
                || ("3".equals(json.getString("meetingType")) && !reportModelName.equals(meetingMain.getfModelName()))
        ) {
            changeMeetingTypeOrProj = true;
        }
        if ("AppsPreAuditMeeting".equals(meetingMain.getfModelName())) {
            AppsPreAuditMeeting auditMeeting = appsPreAuditMeetingService.getById(meetingMain.getfModelId());
            if (!json.getString("projectNo").equals(auditMeeting.getfMainId().getfId())) {
                changeMeetingTypeOrProj = true;
            }
        }
        if ("AppsMeetingKickoff".equals(meetingMain.getfModelName())) {
            AppsMeetingKickoff kickoffMeeting = appsMeetingKickoffService.getById(meetingMain.getfModelId());
            if (!json.getString("projectNo").equals(kickoffMeeting.getfMainId().getfId())) {
                changeMeetingTypeOrProj = true;
            }
        }
        if ("AppsReport".equals(meetingMain.getfModelName())) {
            AppsReport reportMeeting = appsReportService.getById(meetingMain.getfModelId());
            if (!json.getString("projectNo").equals(reportMeeting.getfMainId().getfId())) {
                changeMeetingTypeOrProj = true;
            }
        }
        return changeMeetingTypeOrProj;
    }

    public void updateSyncMeetingBasic(JSONObject json, MeetingMain meetingMain, JSONObject result) throws Exception {
        AppsPreAuditMeeting appsPreAuditMeeting = null;
        if (preAuditMeetingModelName.equals(meetingMain.getfModelName())) {
            appsPreAuditMeeting = appsPreAuditMeetingService.getById(meetingMain.getfId());
        }

        if (json.containsKey("meetingPlace")) {
            meetingMain.setfPlace(json.getString("meetingPlace"));
            if (appsPreAuditMeeting != null) {
                appsPreAuditMeeting.setfMeetingPlace(json.getString("meetingPlace"));
            }
        }
        if (json.containsKey("meetingTime")) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Date preDate = df.parse(json.getString("meetingTime"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            if (json.containsKey("meetingStartTime")) {
                Date startTime = sdf.parse(json.getString("meetingStartTime"));
                meetingMain.setfStartTime(startTime);
                if (appsPreAuditMeeting != null) {
                    appsPreAuditMeeting.setfMeetingStartTime(startTime);
                }
            } else {
                meetingMain.setfStartTime(preDate);
                if (appsPreAuditMeeting != null) {
                    appsPreAuditMeeting.setfMeetingStartTime(preDate);
                }
            }
            if (json.containsKey("meetingEndTime")) {
                Date endTime = sdf.parse(json.getString("meetingEndTime"));
                meetingMain.setfFinishTime(endTime);
                if (appsPreAuditMeeting != null) {
                    appsPreAuditMeeting.setfMeetingStartTime(endTime);
                }
            } else {
                meetingMain.setfStartTime(preDate);
                if (appsPreAuditMeeting != null) {
                    appsPreAuditMeeting.setfMeetingStartTime(preDate);
                }
            }

        }
        meetingMainService.save(meetingMain);
        if (appsPreAuditMeeting != null) {
            appsPreAuditMeetingService.save(appsPreAuditMeeting);
        }
        result.put("status", "S");
        result.put("message", meetingMain.getfId());
    }

    /**
     * 当前会议是否已有关联包件
     */
    public boolean hasPackage(String meetingId) throws Exception {
        Specification<MeetingPackage> spec = new Specification<MeetingPackage>() {
            @Override
            public Predicate toPredicate(Root<MeetingPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate pre = criteriaBuilder.equal(root.get("fMeetingId").get("fId"), meetingId);
                return query.where(pre).getRestriction();
            }
        };
        List list = meetingPackageService.findAll(spec);
        return !list.isEmpty();
    }
}
