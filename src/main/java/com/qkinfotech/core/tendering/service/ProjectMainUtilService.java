package com.qkinfotech.core.tendering.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.tendering.attachment.controller.AttachmentController;
import com.qkinfotech.core.tendering.iso.model.IsoApproval;
import com.qkinfotech.core.tendering.model.apps.collection.AppsCollectionResult;
import com.qkinfotech.core.tendering.model.apps.collection.AppsCollectionResultPackage;
import com.qkinfotech.core.tendering.model.apps.finalization.AppsFinalizationResultPackage;
import com.qkinfotech.core.tendering.model.apps.meeting.AppsMeetingKickoff;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingMain;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingPackage;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticePackage;
import com.qkinfotech.core.tendering.model.apps.pre.AppsPreAuditMeeting;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.core.tendering.model.apps.report.AppsReport;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import com.qkinfotech.core.tendering.model.attachment.AttachmentPackage;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ProjectMainUtilService {

    @Autowired
    private SimpleService<MeetingMain> meetingMainService;

    @Autowired
    private SimpleService<AppsPreAuditMeeting> appsPreAuditMeetingService;

    @Autowired
    protected SimpleService<AppsMeetingKickoff> appsMeetingKickoffService;

    @Autowired
    protected SimpleService<AppsProjectPackage> appsProjectPackageService;

    @Autowired
    protected SimpleService<AppsCollectionResultPackage> appsCollectionResultPackageService;

    @Autowired
    protected SimpleService<IsoApproval> isoApprovalService;

    @Autowired
    protected SimpleService<AppsNoticePackage> appsNoticePackageService;

    @Autowired
    protected SimpleService<MeetingPackage> meetingPackageService;

    @Autowired
    protected SimpleService<AppsFinalizationResultPackage> appsFinalizationResultPackageService;

    @Autowired
    protected SimpleService<AppsReport> appsReportService;

    @Autowired
    protected SimpleService<AppsCollectionResult> appsCollectionResultService;

    @Autowired
    protected FileMainService fileMainService;

    @Autowired
    protected SimpleService<AttachmentMain> attachmentMainService;

    @Autowired
    protected SimpleService<AttachmentPackage> attachmentPackageService;

    private String companyImportanceName = "公司重点";

    private String projectModelName = "com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain";

    private String noticeModelName = "com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeMain";

    @Autowired
    private AttachmentController attachmentController;

    /**
     * 根据项目id找到项目包件  list
     *
     * @param fMainId 项目id
     * @return
     * @throws Exception
     */
    public List<AppsProjectPackage> getPackagesByMainId(String fMainId) throws Exception {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fMainId.fId", fMainId);
        queryJson.put("eq", equal);
        query.put("query", queryJson);
        List<AppsProjectPackage> lists = appsProjectPackageService.findAll(JSONQuerySpecification.getSpecification(query));
        return lists;
    }

    /**
     * 重点项目，工作计划判断包件是否上传
     *
     * @param main
     * @param packages
     * @return
     * @throws Exception
     */
    public JSONObject getPackagesTime(AppsProjectMain main, List<AppsProjectPackage> packages) throws Exception {
        JSONObject object = new JSONObject();
        List<AppsProjectPackage> packagesbak = new ArrayList<>(packages);
        //如果为重点项目，则工作计划每个包件都要上传。
        List<AppsProjectPackage> list = new ArrayList<>();//附件关联包件 list
        List<AttachmentMain> workPlans = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain", main.getfId(), "workPlan");
        if (!workPlans.isEmpty()) {
            for (AttachmentMain workPlan : workPlans) {
                //每个工作计划附件的 包件关联
                Set<AttachmentPackage> attPackages = workPlan.getfPackages();
                if (!attPackages.isEmpty()) {
                    for (AttachmentPackage pack : attPackages) {
                        //不重复添加id
                        if (!list.contains(pack.getfPackageId())) {
                            list.add(pack.getfPackageId());
                        }
                    }
                }
            }
            //对比 附件包件是否和全包件一致 长度一致代表是全部包件都已经存关系
            if (list.size() != packagesbak.size()) {
                StringBuilder errMsg = new StringBuilder();
                packagesbak.removeAll(list);
                //找出缺少的包件关系,并有提示语
                for (AppsProjectPackage pPackage : packagesbak) {
                    errMsg.append("请检查包件：").append(pPackage.getfName()).append("的工作计划是否上传！");
                }
                object.put("status", "E");
                object.put("msg", errMsg.toString());
            } else {
                Date time = getTime(workPlans);
                object.put("status", "S");
                object.put("msg", time);
            }
        } else {
            object.put("status", "E");
            object.put("msg", "请检查是否未上传工作计划！");
        }
        return object;
    }

    /**
     * 项目启动会：有项目启动会的包件必须上传会议议程附件，取附件的上传时间。 会议结束时间 和 附件时间
     *
     * @param main
     * @return
     * @throws Exception
     */
    public JSONObject getMeetingKickoffTime(AppsProjectMain main, List<AppsProjectPackage> packages) throws Exception {
        JSONObject object = new JSONObject();
        StringBuilder errMsg = new StringBuilder();
        List<AppsProjectPackage> packagesbak = new ArrayList<>(packages);
        List<AppsProjectPackage> list = new ArrayList<>();//附件关联包件 list
        List<AppsMeetingKickoff> kickoffs = getMeetingKickoff(main);
        Date mettingLastTime = null;//会议的结束日
        Date attLastTime = null;//附件上传日期
        boolean flag = true;
        //每个项目启动会 对应的包件 然后找到包件对应的 会议议程 附件
        for (AppsMeetingKickoff kickoff : kickoffs) {
            //找启动会
            MeetingMain meetingMain = meetingMainService.getById(kickoff.getfId());
            if (mettingLastTime == null || mettingLastTime.after(meetingMain.getfFinishTime())) {
                mettingLastTime = meetingMain.getfFinishTime();//会议结束时间
            }
            //会议议程附件记录
            List<AttachmentMain> attachmentMains = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.meeting.AppsMeetingKickoff", kickoff.getfId(), "meetingAgenda");
            if (!attachmentMains.isEmpty()) {
                for (AttachmentMain attMain : attachmentMains) {
                    Set<AttachmentPackage> attPackages = attMain.getfPackages();
                    if (!attPackages.isEmpty()) {
                        for (AttachmentPackage pack : attPackages) {
                            if (!list.contains(pack.getfPackageId())) {//不重复添加
                                list.add(pack.getfPackageId());
                            }
                            if (attLastTime == null || attLastTime.after(pack.getfAttachmentId().getfCreateTime())) {
                                attLastTime = pack.getfAttachmentId().getfCreateTime();//附件创建时间
                            }
                        }
                    }
                }
            } else {
                flag = false;
                errMsg.append("请检查项目启动会的会议议程是否上传！");
            }
        }
        if (flag) {
            object.put("status", "S");
            //比较出最晚时间
            if (mettingLastTime.after(attLastTime)) {
                object.put("msg", mettingLastTime);
            } else {
                object.put("msg", attLastTime);
            }
        } else {
            object.put("status", "E");
            object.put("msg", errMsg.toString());
        }
        return object;
    }

    /**
     * 汇报评审会：有项目启动会的包件必须上传会议议程附件，取附件的上传时间。 会议结束时间 和 附件时间
     *
     * @param main
     * @param packages
     * @return
     * @throws Exception
     */
    public JSONObject getProjectReportTime(AppsProjectMain main, List<AppsProjectPackage> packages) throws Exception {
        JSONObject object = new JSONObject();
        StringBuilder errMsg = new StringBuilder();
        List<AppsProjectPackage> packagesbak = new ArrayList<>(packages);
        List<AppsProjectPackage> list = new ArrayList<>();//附件关联包件 list
        List<AppsReport> reports = getProjectReport(main);
        Date mettingLastTime = null;//会议的结束日
        Date attLastTime = null;//附件上传日期
        boolean flag = true;
        for (AppsReport report : reports) {
            //会议
            MeetingMain meetingMain = meetingMainService.getById(report.getfId());
            if (mettingLastTime == null || mettingLastTime.after(meetingMain.getfFinishTime())) {
                mettingLastTime = meetingMain.getfFinishTime();//会议结束时间
            }
            //会议议程附件记录
            List<AttachmentMain> attachmentMains = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.report.AppsReport", report.getfId(), "meetingAgenda");
            if (!attachmentMains.isEmpty()) {
                for (AttachmentMain attMain : attachmentMains) {
                    Set<AttachmentPackage> attPackages = attMain.getfPackages();
                    if (!attPackages.isEmpty()) {
                        for (AttachmentPackage pack : attPackages) {
                            if (!list.contains(pack.getfPackageId())) {//不重复添加
                                list.add(pack.getfPackageId());
                            }
                            if (attLastTime == null || attLastTime.after(pack.getfAttachmentId().getfCreateTime())) {
                                attLastTime = pack.getfAttachmentId().getfCreateTime();//附件创建时间
                            }
                        }
                    }
                }
            } else {
                flag = false;
                errMsg.append("请检查汇报评审会的会议议程是否上传！");
            }
        }
        if (flag) {
            object.put("status", "S");
            //比较出最晚时间
            if (mettingLastTime.after(attLastTime)) {
                object.put("msg", mettingLastTime);
            } else {
                object.put("msg", attLastTime);
            }
        } else {
            object.put("status", "E");
            object.put("msg", errMsg.toString());
        }
        return object;
    }

    /**
     * 通过项目找到 项目启动会
     *
     * @param main
     * @return
     * @throws Exception
     */
    public List<AppsMeetingKickoff> getMeetingKickoff(AppsProjectMain main) throws Exception {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fMainId.fId", main.getfId());
        queryJson.put("eq", equal);
        query.put("query", queryJson);
        List<AppsMeetingKickoff> lists = appsMeetingKickoffService.findAll(JSONQuerySpecification.getSpecification(query));
        return lists;
    }

    /**
     * 通过项目找到 汇报评审会
     *
     * @param main
     * @return
     * @throws Exception
     */
    public List<AppsReport> getProjectReport(AppsProjectMain main) throws Exception {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fMainId.fId", main.getfId());
        queryJson.put("eq", equal);
        query.put("query", queryJson);
        List<AppsReport> lists = appsReportService.findAll(JSONQuerySpecification.getSpecification(query));
        return lists;
    }

    /**
     * 征集结果：每个包件都需要有征集结果，每个包件都必须有书面报告附件。
     */
    public JSONObject getCollectionResultTime(List<AppsProjectPackage> packages, AppsProjectMain main) throws Exception {
        JSONObject object = new JSONObject();
        List<AppsProjectPackage> packagesbak = new ArrayList<>(packages);
        List<AppsProjectPackage> list = new ArrayList<>();//附件关联包件 list
        StringBuilder errMsg = new StringBuilder();
        Date attlastTime = null;//最晚时间
        for (AppsProjectPackage pPackage : packages) {
            List<AppsCollectionResultPackage> collectionPackage = getCollectionPackage(pPackage.getfId());
            if (collectionPackage.isEmpty()) {
                errMsg.append("请检查包件：").append(pPackage.getfName()).append("的征集结果是否上传！");
                object.put("status", "E");
                object.put("msg", errMsg.toString());
            } else {
                //征集结果 每个包件的确认征集结果日期
                if (pPackage.getfConfirmCollectionResultDate() == null) {
                    errMsg.append("请检查包件：").append(pPackage.getfName()).append("的确认征集结果日期是否填写！");
                } else if (attlastTime == null) {
                    attlastTime = pPackage.getfConfirmCollectionResultDate();
                } else if (!attlastTime.after(pPackage.getfConfirmCollectionResultDate())) {
                    attlastTime = pPackage.getfConfirmCollectionResultDate();
                }
                //书面报告iso审批结束时间
                List<AttachmentMain> attachmentMains = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.collection.AppsCollectionResult", main.getfId(), "writtenReport");
                if (!attachmentMains.isEmpty()) {
                    for (AttachmentMain attMain : attachmentMains) {
                        //附件关联包件
                        Set<AttachmentPackage> attPackages = attMain.getfPackages();
                        if (!attPackages.isEmpty()) {
                            if (attMain.getfIsoFlag() != 1) {
                                errMsg.append("请检查").append(attMain.getfFileName()).append("文件iso审批是否结束！");
                            } else {
                                //审批完才能取时间
                                Date timeByIso = getTimeByIso(attMain.getfId());
                                if (timeByIso != null && (attlastTime == null || !attlastTime.after(timeByIso))) {
                                    attlastTime = timeByIso;
                                }
                            }
                            for (AttachmentPackage pack : attPackages) {
                                //不重复添加id
                                if (!list.contains(pack.getfPackageId())) {
                                    list.add(pack.getfPackageId());
                                }
                            }
                        }
                    }
                    //对比 是否缺少包件关联
                    if (list.size() != packagesbak.size()) {
                        packagesbak.removeAll(list);//列出缺少的包件
                        for (AppsProjectPackage p : packagesbak) {
                            errMsg.append("请检查包件：").append(p.getfName()).append("的书面报告附件是否上传！");
                        }
                        object.put("status", "E");
                        object.put("msg", errMsg.toString());
                    } else if (attlastTime == null) {//没有日期代表 iso没审批结束
                        object.put("status", "E");
                        object.put("msg", errMsg.toString());
                    } else {
                        if (errMsg.isEmpty()) {
                            object.put("status", "S");
                            object.put("msg", attlastTime);
                        } else {
                            object.put("status", "E");
                            object.put("msg", errMsg.toString());
                        }
                    }
                } else {
                    errMsg.append("请检查书面报告附件是否未上传！");
                    object.put("status", "E");
                    object.put("msg", errMsg.toString());
                }
            }
        }
        return object;
    }

    /**
     * 征集结果包件查询
     *
     * @param fId
     * @return
     * @throws Exception
     */
    public List<AppsCollectionResultPackage> getCollectionPackage(String fId) throws Exception {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("appsProjectPackage.fId", fId);
        queryJson.put("eq", equal);
        query.put("query", queryJson);
        List<AppsCollectionResultPackage> lists = appsCollectionResultPackageService.findAll(JSONQuerySpecification.getSpecification(query));
        return lists;
    }

    /**
     * 征集文件：每个包件都需要上传且文件通过ISO流传的附件，取文件流程的结束日期(fFinishTime)。iso取状态1
     */
    public JSONObject getCollectionResultAttachmentTime(List<AppsProjectPackage> packages, AppsProjectMain main) throws Exception {
        JSONObject object = new JSONObject();
        StringBuilder errMsg = new StringBuilder();
        List<AppsProjectPackage> packagesbak = new ArrayList<>(packages);
        List<AppsProjectPackage> list = new ArrayList<>();//附件关联包件 list
        Date lastTime = null;
        List<AttachmentMain> attachmentMains = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain", main.getfId(), "collectionFile");
        if (!attachmentMains.isEmpty()) {
            for (AttachmentMain attMain : attachmentMains) {
                //附件关联包件
                Set<AttachmentPackage> attPackages = attMain.getfPackages();
                if (!attPackages.isEmpty()) {
                    if (attMain.getfIsoFlag() != 1) {
                        errMsg.append("请检查").append(attMain.getfFileName()).append("文件iso审批是否结束！");
                    } else {
                        //审批完才能取时间
                        Date timeByIso = getTimeByIso(attMain.getfId());
                        if (timeByIso != null && (lastTime == null || !lastTime.after(timeByIso))) {
                            lastTime = timeByIso;
                        }
                    }
                    for (AttachmentPackage pack : attPackages) {
                        //不重复添加id
                        if (!list.contains(pack.getfPackageId())) {
                            list.add(pack.getfPackageId());
                        }
                    }
                }
            }
            //对比 附件包件是否和全包件一致 长度一致代表是全部包件都已经存关系
            if (list.size() != packagesbak.size()) {
                packagesbak.removeAll(list);
                //找出缺少的包件关系,并有提示语
                for (AppsProjectPackage pPackage : packagesbak) {
                    errMsg.append("请检查包件：").append(pPackage.getfName()).append("的征集文件是否上传！");
                }
                object.put("status", "E");
                object.put("msg", errMsg.toString());
            } else if (lastTime == null) {//没有日期代表 iso没审批结束
                object.put("status", "E");
                object.put("msg", errMsg.toString());
            } else {
                object.put("status", "S");
                object.put("msg", lastTime);
            }
        } else {
            object.put("status", "E");
            object.put("msg", "请检查是否未上传征集文件！");
        }
        return object;
    }

    /**
     * 资格预审:每个包件都需要发布公告，如果包件发布的公告只有资格预审公告，那么此包件必须要有一个资格预审会议，取资格预审会议结束时间，
     */
    public JSONObject getNoiceTime(List<AppsProjectPackage> packages) {
        JSONObject object = new JSONObject();
        StringBuilder errMsg = new StringBuilder();
        boolean flag = true;
        Date lastTime = null;
        for (AppsProjectPackage projectPackage : packages) {
            List<AppsNoticePackage> noticePackages = getNoticePackages(projectPackage);//全部公告
            List<AppsNoticePackage> preNoticePackages = getPreNoticePackages(projectPackage);//资格预审公告
            if (noticePackages.isEmpty()) {//没有公告提示
                errMsg.append("请检查包件：").append(projectPackage.getfName()).append("是否发起公告！");
            } else if (noticePackages.size() == preNoticePackages.size()) {//全部公告和资格预审公告 对比长度 如果一致 代表全为资格预审公告
                //判断是否有会议，并取最晚会议时间
                List<MeetingPackage> preMeeting = getPreMeeting(projectPackage);
                if (preMeeting.isEmpty()) {//没有会议提示
                    flag = false;
                    errMsg.append("请检查包件：").append(projectPackage.getfName()).append("是否发起资格预审会议！");
                } else {
                    if (lastTime == null || lastTime.after(preMeeting.get(0).getfMeetingId().getfFinishTime())) {
                        lastTime = preMeeting.get(0).getfMeetingId().getfFinishTime();
                    }
                }
            }
        }
        if (flag) {
            object.put("status", "S");
            object.put("msg", lastTime);
        } else {
            object.put("status", "E");
            object.put("msg", errMsg.toString());
        }
        return object;
    }

    /**
     * 入围结果：每一个包件的入围结果都不为空，获取入围结果修改日期。
     */
    public JSONObject getFinalizationResultTime(List<AppsProjectPackage> packages) {
        JSONObject object = new JSONObject();
        StringBuilder errMsg = new StringBuilder();
        boolean flag = true;
        Date lastTime = null;
        //判断每个包件是否都有入围结果记录
        for (AppsProjectPackage projectPackage : packages) {
            List<AppsFinalizationResultPackage> finalizationResult = getFinalizationResult(projectPackage);
            if (finalizationResult.isEmpty()) {
                flag = false;
                errMsg.append("请检查包件：").append(projectPackage.getfName()).append("是否填写入围结果！");
            } else {
                if (lastTime == null || lastTime.after(finalizationResult.get(0).getfCreateTime())) {
                    lastTime = finalizationResult.get(0).getfCreateTime();
                }
            }
        }
        if (flag) {
            object.put("status", "S");
            object.put("msg", lastTime);
        } else {
            object.put("status", "E");
            object.put("msg", errMsg.toString());
        }
        return object;
    }

    /**
     * 资格预审结果公告附件：每个包件都需要有资格预审结果公告，取文件上传时间。
     *
     * @param
     * @return
     */
    public JSONObject getNoticeFileTime(List<AppsProjectPackage> packages, AppsProjectMain main) {
        JSONObject object = new JSONObject();
        StringBuilder errMsg = new StringBuilder();
        List<AppsProjectPackage> packagesbak = new ArrayList<>(packages);
        List<AppsProjectPackage> list = new ArrayList<>();//附件关联包件 list
        Date lastTime = null;
        //查询 资格预审结果公告附件
        List<AttachmentMain> attachmentMains = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeMain", main.getfId(), "noticeFinishMain");
        if (!attachmentMains.isEmpty()) {
            for (AttachmentMain attMain : attachmentMains) {
                Set<AttachmentPackage> attPackages = attMain.getfPackages();
                if (!attPackages.isEmpty()) {
                    for (AttachmentPackage pack : attPackages) {
                        if (!list.contains(pack.getfPackageId())) {//不重复添加
                            list.add(pack.getfPackageId());
                        }
                        if (lastTime == null || lastTime.after(pack.getfAttachmentId().getfCreateTime())) {
                            lastTime = pack.getfAttachmentId().getfCreateTime();//附件创建时间
                        }
                    }
                }
            }
            //对比 是否缺少包件关联
            if (list.size() != packagesbak.size()) {
                packagesbak.removeAll(list);//列出缺少的包件
                for (AppsProjectPackage pPackage : packagesbak) {
                    errMsg.append("请检查包件：").append(pPackage.getfName()).append("的资格预审结果公告附件是否上传！");
                }
                object.put("status", "E");
                object.put("msg", errMsg.toString());
            } else {
                object.put("status", "S");
                object.put("msg", lastTime);
            }
        } else {
            errMsg.append("请检查资格预审结果公告附件是否未上传！");
            object.put("status", "E");
            object.put("msg", errMsg.toString());
        }
        return object;
    }

    /**
     * 获取入围结果
     *
     * @param projectPackage
     * @return
     */
    public List<AppsFinalizationResultPackage> getFinalizationResult(AppsProjectPackage projectPackage) {
        JSONArray array = new JSONArray();
        array.add("fCreateTime desc");
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fPackageId.fId", projectPackage.getfId());
        queryJson.put("eq", equal);
        query.put("query", queryJson);
        query.put("sort", array);
        List<AppsFinalizationResultPackage> finalizationResultPackages = appsFinalizationResultPackageService.findAll(JSONQuerySpecification.getSpecification(query));
        return finalizationResultPackages;
    }

    /**
     * 包件对应会议
     *
     * @param projectPackage
     * @return
     */
    public List<MeetingPackage> getPreMeeting(AppsProjectPackage projectPackage) {
        JSONArray array = new JSONArray();
        array.add("fMeetingId.fFinishTime desc");
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fPackageId.fId", projectPackage.getfId());
        queryJson.put("eq", equal);
        query.put("query", queryJson);
        query.put("sort", array);
        List<MeetingPackage> meetingPackages = meetingPackageService.findAll(JSONQuerySpecification.getSpecification(query));
        return meetingPackages;
    }

    /**
     * 包件对应全部公告
     *
     * @param projectPackage
     * @return
     */
    public List<AppsNoticePackage> getNoticePackages(AppsProjectPackage projectPackage) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fPackage.fId", projectPackage.getfId());
        queryJson.put("eq", equal);
        query.put("query", queryJson);
        List<AppsNoticePackage> appsNoticePackages = appsNoticePackageService.findAll(JSONQuerySpecification.getSpecification(query));

        return appsNoticePackages;
    }

    /**
     * 包件对应资格预审公告
     *
     * @param projectPackage
     * @return
     */
    public List<AppsNoticePackage> getPreNoticePackages(AppsProjectPackage projectPackage) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();
        JSONObject eq = new JSONObject();

        JSONObject packageId = new JSONObject();
        packageId.put("fPackage.fId", projectPackage.getfId());
        eq.put("eq", packageId);
        and.add(eq);

        JSONObject eq1 = new JSONObject();
        JSONObject fIsPrequalification = new JSONObject();
        fIsPrequalification.put("fMain.fIsPrequalification", "true");
        eq1.put("eq", fIsPrequalification);
        and.add(eq1);

        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsNoticePackage> appsNoticePackages = appsNoticePackageService.findAll(JSONQuerySpecification.getSpecification(query));
        return appsNoticePackages;
    }

    /**
     * 取iso审批 结束时间
     *
     * @param fId attmain的fid
     * @return
     */
    public Date getTimeByIso(String fId) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fAttachments.fId", fId);
        queryJson.put("eq", equal);
        query.put("query", queryJson);
        query.put("distance", true);
        List<IsoApproval> isoApprovals = isoApprovalService.findAll(JSONQuerySpecification.getSpecification(query));
        if (isoApprovals.isEmpty()) {
            return null;
        } else {
            return isoApprovals.get(0).getfFinishTime();
        }
    }

    public List<AppsCollectionResult> getResultConfirmTime(AppsProjectMain main) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fMainId.fId", main.getfId());
        queryJson.put("eq", equal);
        query.put("query", queryJson);
        List<AppsCollectionResult> list = appsCollectionResultService.findAll(JSONQuerySpecification.getSpecification(query));
        return list;
    }

    /**
     * 获取最后日期
     *
     * @param attMains
     * @return
     */
    private static Date getTime(List<AttachmentMain> attMains) {
        Date latestDate = new Date();
        //都有关系的情况下 获取最晚的日期
        for (AttachmentMain attMain : attMains) {
            Date currentDate = attMain.getfCreateTime();
            // 比较并更新最新日期
            if (currentDate != null && currentDate.after(latestDate)) {
                latestDate = currentDate;
            }
        }
        return latestDate;
    }

    /**
     * 公司重点项目-》所有包件必须上传工作计划（workPlan）
     */
    public JSONObject checkImportance(AppsProjectMain main, List<AppsProjectPackage> packages) {
        JSONObject result = new JSONObject();
        try {
            //公司重点
            if (companyImportanceName.equals(main.getfProjectImportance().getfName())) {
                //所有工作计划附件
                Specification<AttachmentMain> specification = new Specification<AttachmentMain>() {
                    @Override
                    public Predicate toPredicate(Root<AttachmentMain> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        Predicate eq1 = criteriaBuilder.equal(root.get("fModelName"), projectModelName);
                        Predicate eq2 = criteriaBuilder.equal(root.get("fModelId"), main.getfId());
                        Predicate eq3 = criteriaBuilder.equal(root.get("fKey"), "workPlan");
                        Predicate eq4 = criteriaBuilder.equal(root.get("fDisplay"), "0");
                        return criteriaBuilder.and(eq1, eq2, eq3, eq4);
                    }
                };
                List<AttachmentMain> list = attachmentMainService.findAll(specification);
                Map<String, String> packMap = new HashMap<>();
                packages.forEach(val -> {
                    packMap.put(val.getfId(), val.getfName());
                });
                list.forEach(attMain -> {
                    attMain.getfPackages().forEach(attPack -> {
                        packMap.remove(attPack.getfPackageId().getfId());
                    });
                });
                //没有工作计划的包件
                if (packMap.isEmpty()) {
                    result.put("result", true);
                } else {
                    result.put("result", false);
                    StringBuilder message = new StringBuilder();
                    for (Map.Entry<String, String> entry : packMap.entrySet()) {
                        message.append(entry.getValue() + "没有上传工作计划\n");
                    }
                    result.put("message", message.toString());
                }
            } else {
                result.put("result", true);
            }
        } catch (Exception e) {
            result.put("result", false);
            result.put("message", "校验工作计划发生错误：" + e.getMessage() + "\n");
        }
        return result;
    }

    /**
     * 公告-》只有资格预审公告的包件-》必须上传资格预审公告附件和资格预审公告结果附件
     */
    public JSONObject checkNotice(AppsProjectMain main, List<AppsProjectPackage> packages) {
        JSONObject result = new JSONObject();
        try {
            boolean resultBool = true;

            //所有资格预审公告包件
            Specification<AppsNoticePackage> preNoticePackageSpecification = new Specification<AppsNoticePackage>() {
                @Override
                public Predicate toPredicate(Root<AppsNoticePackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate eq1 = criteriaBuilder.equal(root.get("fMain").get("fIsPrequalification"), "true");
                    Predicate eq2 = criteriaBuilder.equal(root.get("fMain").get("fProjectId").get("fId"), main.getfId());
                    return criteriaBuilder.and(eq1, eq2);
                }
            };
            List<AppsNoticePackage> prePackageList = appsNoticePackageService.findAll(preNoticePackageSpecification);
            //所有非资格预审公告包件
            Specification<AppsNoticePackage> nePreNoticePackageSpecification = new Specification<AppsNoticePackage>() {
                @Override
                public Predicate toPredicate(Root<AppsNoticePackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate eq1 = criteriaBuilder.equal(root.get("fMain").get("fIsPrequalification"), "false");
                    Predicate eq2 = criteriaBuilder.equal(root.get("fMain").get("fProjectId").get("fId"), main.getfId());
                    return criteriaBuilder.and(eq1, eq2);
                }
            };
            List<AppsNoticePackage> nePrePackageList = appsNoticePackageService.findAll(nePreNoticePackageSpecification);
            //只有资格预审公告的包件
            Set<AppsProjectPackage> onlyPrePackage = new HashSet<>();
            prePackageList.forEach(val -> {
                onlyPrePackage.add(val.getfPackage());
            });
            nePrePackageList.forEach(val -> {
                onlyPrePackage.remove(val.getfPackage());
            });

            //资格预审公告附件（noticeMain）
            Specification<AttachmentPackage> noticeMainPackageSpecification = new Specification<AttachmentPackage>() {
                @Override
                public Predicate toPredicate(Root<AttachmentPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate eq1 = criteriaBuilder.equal(root.get("fAttachmentId").get("fModelName"), noticeModelName);
                    Predicate eq2 = criteriaBuilder.equal(root.get("fAttachmentId").get("fModelId"), main.getfId());
                    Predicate eq3 = criteriaBuilder.equal(root.get("fAttachmentId").get("fKey"), "noticeMain");
                    Predicate eq4 = criteriaBuilder.equal(root.get("fAttachmentId").get("fDisplay"), "0");
                    return criteriaBuilder.and(eq1, eq2, eq3, eq4);
                }
            };
            List<AttachmentPackage> noticeMainPackageList = attachmentPackageService.findAll(noticeMainPackageSpecification);
            //没有资格预审公告附件的包件
            Map<String, String> noNoticeMainPackageMap = new HashMap<>();
            onlyPrePackage.forEach(val -> {
                noNoticeMainPackageMap.put(val.getfId(), val.getfName());
            });
            noticeMainPackageList.forEach(val -> {
                noNoticeMainPackageMap.remove(val.getfPackageId().getfId());
            });
            if (!noNoticeMainPackageMap.isEmpty()) {
                resultBool = false;
                StringBuilder message = new StringBuilder();
                for (Map.Entry<String, String> entry : noNoticeMainPackageMap.entrySet()) {
                    message.append(entry.getValue() + "没有上传资格预审公告附件\n");
                }
                result.put("message", message.toString());
            }

            //资格预审结果公告附件（noticeFinishMain）
            Specification<AttachmentPackage> noticeFinishMainPackageSpecification = new Specification<AttachmentPackage>() {
                @Override
                public Predicate toPredicate(Root<AttachmentPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate eq1 = criteriaBuilder.equal(root.get("fAttachmentId").get("fModelName"), noticeModelName);
                    Predicate eq2 = criteriaBuilder.equal(root.get("fAttachmentId").get("fModelId"), main.getfId());
                    Predicate eq3 = criteriaBuilder.equal(root.get("fAttachmentId").get("fKey"), "noticeFinishMain");
                    Predicate eq4 = criteriaBuilder.equal(root.get("fAttachmentId").get("fDisplay"), "0");
                    return criteriaBuilder.and(eq1, eq2, eq3, eq4);
                }
            };
            List<AttachmentPackage> noticeFinishMainPackageList = attachmentPackageService.findAll(noticeFinishMainPackageSpecification);
            //没有资格预审结果公告附件的包件
            Map<String, String> noNoticeFinishMainPackageMap = new HashMap<>();
            onlyPrePackage.forEach(val -> {
                noNoticeFinishMainPackageMap.put(val.getfId(), val.getfName());
            });
            noticeFinishMainPackageList.forEach(val -> {
                noNoticeFinishMainPackageMap.remove(val.getfPackageId().getfId());
            });
            if (!noNoticeFinishMainPackageMap.isEmpty()) {
                resultBool = false;
                StringBuilder message = new StringBuilder();
                for (Map.Entry<String, String> entry : noNoticeFinishMainPackageMap.entrySet()) {
                    message.append(entry.getValue() + "没有上传资格预审结果公告附件\n");
                }
                result.put("message", message.toString());
            }

            result.put("result", resultBool);
        } catch (Exception e) {
            result.put("result", false);
            result.put("message", "校验资格预审公告发生错误：" + e.getMessage() + "\n");
        }
        return result;
    }

    /**
     * 资格预审会议-》会议必须关联包件
     */
    public JSONObject checkPreMeeting(AppsProjectMain main) {
        JSONObject result = new JSONObject();
        try {
            boolean resultBool = true;
            Specification<AppsPreAuditMeeting> preAuditMeetingSpecification = new Specification<AppsPreAuditMeeting>() {
                @Override
                public Predicate toPredicate(Root<AppsPreAuditMeeting> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.equal(root.get("fMainId").get("fId"), main.getfId());
                }
            };
            List<AppsPreAuditMeeting> preAuditMeetingList = appsPreAuditMeetingService.findAll(preAuditMeetingSpecification);
            if (!preAuditMeetingList.isEmpty()) {
                Specification<MeetingMain> meetingSpecification = new Specification<MeetingMain>() {
                    @Override
                    public Predicate toPredicate(Root<MeetingMain> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        Predicate eq1 = criteriaBuilder.equal(root.get("fModelName"), "AppsPreAuditMeeting");
                        CriteriaBuilder.In in = criteriaBuilder.in(root.get("fModelId"));
                        preAuditMeetingList.forEach(val -> {
                            in.value(val.getfId());
                        });
                        return criteriaBuilder.and(eq1, in);
                    }
                };
                List<MeetingMain> meetingMainList = meetingMainService.findAll(meetingSpecification);
                if (!meetingMainList.isEmpty()) {
                    Specification<MeetingPackage> packageSpecification = new Specification<MeetingPackage>() {
                        @Override
                        public Predicate toPredicate(Root<MeetingPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                            CriteriaBuilder.In in = criteriaBuilder.in(root.get("fMeetingId").get("fId"));
                            meetingMainList.forEach(val -> {
                                in.value(val.getfId());
                            });
                            return in;
                        }
                    };
                    List<MeetingPackage> meetingPackageList = meetingPackageService.findAll(packageSpecification);

                    Set<String> noPackageMeetingIds = new HashSet<>();
                    meetingMainList.forEach(val -> {
                        noPackageMeetingIds.add(val.getfId());
                    });
                    meetingPackageList.forEach(val -> {
                        noPackageMeetingIds.remove(val.getfMeetingId().getfId());
                    });

                    resultBool = noPackageMeetingIds.isEmpty();
                    if (!noPackageMeetingIds.isEmpty()) {
                        result.put("message", "请将资格预审会议或其他会议信息补充完整\n");
                    }
                }
            }
            result.put("result", resultBool);
        } catch (Exception e) {
            result.put("result", false);
            result.put("message", "校验资格预审会议发生错误：" + e.getMessage() + "\n");
        }
        return result;
    }

    /**
     * 征集文件-》所有包件必须上传征集文件，并完成ISO流转
     */
    public JSONObject checkCollectionFile(AppsProjectMain main, List<AppsProjectPackage> packages) {
        JSONObject result = new JSONObject();
        try {
            boolean resultBool = true;
            StringBuffer message = new StringBuffer();

            //是否有完成的征集文件ISO
            Specification<IsoApproval> specification = new Specification<IsoApproval>() {
                @Override
                public Predicate toPredicate(Root<IsoApproval> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate eq1 = criteriaBuilder.equal(root.get("fStatus"), "30");
                    Predicate eq2 = criteriaBuilder.equal(root.get("fAchievementName"), "征集文件");
                    Predicate eq3 = criteriaBuilder.equal(root.get("fProjectId"), main.getfId());
                    return criteriaBuilder.and(eq1, eq2, eq3);
                }
            };
            List<IsoApproval> isoApprovals = isoApprovalService.findAll(specification);
            if (isoApprovals.isEmpty()) {
                resultBool = false;
                message.append("未完成征集文件的ISO审批！\n");
            }

            //最后一份通过的ISO，是否包含所有包件
            if (!isoApprovals.isEmpty()) {
                Collections.sort(isoApprovals, new Comparator<IsoApproval>() {
                    @Override
                    public int compare(IsoApproval o1, IsoApproval o2) {
                        return o2.getfFinishTime().compareTo(o1.getfFinishTime());
                    }
                });
                IsoApproval lastIso = isoApprovals.get(0);
                Specification<AttachmentMain> specification1 = new Specification<AttachmentMain>() {
                    @Override
                    public Predicate toPredicate(Root<AttachmentMain> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        Predicate eq1 = criteriaBuilder.equal(root.get("fModelId"), lastIso.getfId());
                        Predicate eq2 = criteriaBuilder.equal(root.get("fModelName"), "com.qkinfotech.core.tendering.iso.model.IsoApproval");
                        Predicate eq3 = criteriaBuilder.equal(root.get("fKey"), "征集文件");
                        return criteriaBuilder.and(eq1, eq2, eq3);
                    }
                };
                List<AttachmentMain> attachments = attachmentMainService.findAll(specification1);
                Map<String, String> noUploadPackageMap = new HashMap<>();
                packages.forEach(val -> {
                    noUploadPackageMap.put(val.getfId(), val.getfName());
                });
                for (AttachmentMain attachment : attachments) {
                    Set<AttachmentPackage> attachmentPackages = attachment.getfPackages();
                    attachmentPackages.forEach(val -> {
                        noUploadPackageMap.remove(val.getfPackageId().getfId());
                    });
                }
                if (!noUploadPackageMap.isEmpty()) {
                    resultBool = false;
                    for (Map.Entry<String, String> entry : noUploadPackageMap.entrySet()) {
                        message.append(entry.getValue() + "没有上传征集文件附件；\n");
                    }
                }
            }


            //未上传征集文件包件
            /*Specification<AttachmentPackage> attachmentPackageSpecification = new Specification<AttachmentPackage>() {
                @Override
                public Predicate toPredicate(Root<AttachmentPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate eq1 = criteriaBuilder.equal(root.get("fAttachmentId").get("fModelName"), projectModelName);
                    Predicate eq2 = criteriaBuilder.equal(root.get("fAttachmentId").get("fModelId"), main.getfId());
                    Predicate eq3 = criteriaBuilder.equal(root.get("fAttachmentId").get("fKey"), "collectionFile");
                    Predicate eq4 = criteriaBuilder.equal(root.get("fAttachmentId").get("fDisplay"), "0");
                    return criteriaBuilder.and(eq1, eq2, eq3, eq4);
                }
            };
            List<AttachmentPackage> attachmentPackageList = attachmentPackageService.findAll(attachmentPackageSpecification);
            Map<String, String> noUploadPackageMap = new HashMap<>();
            packages.forEach(val -> {
                noUploadPackageMap.put(val.getfId(), val.getfName());
            });
            attachmentPackageList.forEach(val -> {
                noUploadPackageMap.remove(val.getfPackageId().getfId());
            });
            if (!noUploadPackageMap.isEmpty()) {
                resultBool = false;
                for (Map.Entry<String, String> entry : noUploadPackageMap.entrySet()) {
                    message.append(entry.getValue() + "没有上传征集文件附件；\n");
                }
            }

            //未流转ISO审批
            Specification<IsoApproval> isoApprovalSpecification = new Specification<IsoApproval>() {
                @Override
                public Predicate toPredicate(Root<IsoApproval> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.equal(root.get("fProjectId"), main.getfId());
                }
            };
            List<IsoApproval> isoApprovalList = isoApprovalService.findAll(isoApprovalSpecification);
            Map<String, String> noIsoMap = new HashMap<>();
            packages.forEach(val -> {
                noIsoMap.put(val.getfId(), val.getfName());
            });
            isoApprovalList.forEach(isoApproval -> {
                isoApproval.getfAttachments().forEach(attachmentMain -> {
                    attachmentMain.getfPackages().forEach(attachmentPackage -> {
                        noIsoMap.remove(attachmentPackage.getfPackageId().getfId());
                    });
                });
            });
            if (!noIsoMap.isEmpty()) {
                resultBool = false;
                for (Map.Entry<String, String> entry : noIsoMap.entrySet()) {
                    message.append(entry.getValue() + "的征集文件未流转\n");
                }
            }

            //ISO审批流转中
            Map<String, String> noFinishMap = new HashMap<>();
            attachmentPackageList.forEach(val -> {
                if (!Integer.valueOf(1).equals(val.getfIsoFlag())) {
                    noFinishMap.put(val.getfPackageId().getfId(), val.getfPackageId().getfName());
                }
            });
            for (Map.Entry<String, String> entry : noIsoMap.entrySet()) {
                noFinishMap.remove(entry.getKey());
            }
            if (!noFinishMap.isEmpty()) {
                resultBool = false;
                for (Map.Entry<String, String> entry : noFinishMap.entrySet()) {
                    message.append(entry.getValue() + "的征集文件流转中\n");
                }
            }*/

            result.put("result", resultBool);
            result.put("message", message.toString());
        } catch (Exception e) {
            result.put("result", false);
            result.put("message", "校验征集文件发生错误：" + e.getMessage() + "\n");
        }
        return result;
    }

    /**
     * 书面报告-》所有包件必须上传书面报告附件，并完成ISO流转
     */
    public JSONObject checkWrittenReport(AppsProjectMain main, List<AppsProjectPackage> packages) {
        JSONObject result = new JSONObject();
        try {
            boolean resultBool = true;
            StringBuffer message = new StringBuffer();

            //是否有完成的征集文件ISO
            Specification<IsoApproval> specification = new Specification<IsoApproval>() {
                @Override
                public Predicate toPredicate(Root<IsoApproval> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate eq1 = criteriaBuilder.equal(root.get("fStatus"), "30");
                    Predicate eq2 = criteriaBuilder.equal(root.get("fAchievementName"), "书面报告");
                    Predicate eq3 = criteriaBuilder.equal(root.get("fProjectId"), main.getfId());
                    return criteriaBuilder.and(eq1, eq2, eq3);
                }
            };
            List<IsoApproval> isoApprovals = isoApprovalService.findAll(specification);
            if (isoApprovals.isEmpty()) {
                resultBool = false;
                message.append("未完成征集文件的ISO审批！\n");
            }

            //最后一份通过的ISO，是否包含所有包件
            if (!isoApprovals.isEmpty()) {
                Collections.sort(isoApprovals, new Comparator<IsoApproval>() {
                    @Override
                    public int compare(IsoApproval o1, IsoApproval o2) {
                        return o2.getfFinishTime().compareTo(o1.getfFinishTime());
                    }
                });
                IsoApproval lastIso = isoApprovals.get(0);
                Specification<AttachmentMain> specification1 = new Specification<AttachmentMain>() {
                    @Override
                    public Predicate toPredicate(Root<AttachmentMain> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        Predicate eq1 = criteriaBuilder.equal(root.get("fModelId"), lastIso.getfId());
                        Predicate eq2 = criteriaBuilder.equal(root.get("fModelName"), "com.qkinfotech.core.tendering.iso.model.IsoApproval");
                        Predicate eq3 = criteriaBuilder.equal(root.get("fKey"), "书面报告");
                        return criteriaBuilder.and(eq1, eq2, eq3);
                    }
                };
                List<AttachmentMain> attachments = attachmentMainService.findAll(specification1);
                Map<String, String> noUploadPackageMap = new HashMap<>();
                packages.forEach(val -> {
                    noUploadPackageMap.put(val.getfId(), val.getfName());
                });
                for (AttachmentMain attachment : attachments) {
                    Set<AttachmentPackage> attachmentPackages = attachment.getfPackages();
                    attachmentPackages.forEach(val -> {
                        noUploadPackageMap.remove(val.getfPackageId().getfId());
                    });
                }
                if (!noUploadPackageMap.isEmpty()) {
                    resultBool = false;
                    for (Map.Entry<String, String> entry : noUploadPackageMap.entrySet()) {
                        message.append(entry.getValue() + "没有上传书面报告附件；\n");
                    }
                }
            }

            //未上传书面报告包件
            /*Specification<AttachmentPackage> attachmentPackageSpecification = new Specification<AttachmentPackage>() {
                @Override
                public Predicate toPredicate(Root<AttachmentPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate eq1 = criteriaBuilder.equal(root.get("fAttachmentId").get("fModelName"), "com.qkinfotech.core.tendering.model.apps.collection.AppsCollectionResult");
                    Predicate eq2 = criteriaBuilder.equal(root.get("fAttachmentId").get("fModelId"), main.getfId());
                    Predicate eq3 = criteriaBuilder.equal(root.get("fAttachmentId").get("fKey"), "writtenReport");
                    Predicate eq4 = criteriaBuilder.equal(root.get("fAttachmentId").get("fDisplay"), "0");
                    return criteriaBuilder.and(eq1, eq2, eq3, eq4);
                }
            };
            List<AttachmentPackage> attachmentPackageList = attachmentPackageService.findAll(attachmentPackageSpecification);
            Map<String, String> noUploadPackageMap = new HashMap<>();
            packages.forEach(val -> {
                noUploadPackageMap.put(val.getfId(), val.getfName());
            });
            attachmentPackageList.forEach(val -> {
                noUploadPackageMap.remove(val.getfPackageId().getfId());
            });
            if (!noUploadPackageMap.isEmpty()) {
                resultBool = false;
                for (Map.Entry<String, String> entry : noUploadPackageMap.entrySet()) {
                    message.append(entry.getValue() + "没有上传书面报告附件；\n");
                }
            }

            //未流转ISO审批
            Specification<IsoApproval> isoApprovalSpecification = new Specification<IsoApproval>() {
                @Override
                public Predicate toPredicate(Root<IsoApproval> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.equal(root.get("fProjectId"), main.getfId());
                }
            };
            List<IsoApproval> isoApprovalList = isoApprovalService.findAll(isoApprovalSpecification);
            Map<String, String> noIsoMap = new HashMap<>();
            packages.forEach(val -> {
                noIsoMap.put(val.getfId(), val.getfName());
            });
            isoApprovalList.forEach(isoApproval -> {
                isoApproval.getfAttachments().forEach(attachmentMain -> {
                    attachmentMain.getfPackages().forEach(attachmentPackage -> {
                        noIsoMap.remove(attachmentPackage.getfPackageId().getfId());
                    });
                });
            });
            if (!noIsoMap.isEmpty()) {
                resultBool = false;
                for (Map.Entry<String, String> entry : noIsoMap.entrySet()) {
                    message.append(entry.getValue() + "的书面报告未流转\n");
                }
            }

            //ISO审批流转中
            Map<String, String> noFinishMap = new HashMap<>();
            attachmentPackageList.forEach(val -> {
                if (!Integer.valueOf(1).equals(val.getfIsoFlag())) {
                    noFinishMap.put(val.getfPackageId().getfId(), val.getfPackageId().getfName());
                }
            });
            for (Map.Entry<String, String> entry : noIsoMap.entrySet()) {
                noFinishMap.remove(entry.getKey());
            }
            if (!noFinishMap.isEmpty()) {
                resultBool = false;
                for (Map.Entry<String, String> entry : noFinishMap.entrySet()) {
                    message.append(entry.getValue() + "的书面报告流转中\n");
                }
            }*/

            result.put("result", resultBool);
            result.put("message", message.toString());
        } catch (Exception e) {
            result.put("result", false);
            result.put("message", "校验书面报告发生错误：" + e.getMessage() + "\n");
        }
        return result;
    }

    /**
     * 项目启动会-》会议必须关联包件
     */
    public JSONObject checkKickOff(AppsProjectMain main) {
        JSONObject result = new JSONObject();
        try {
            boolean resultBool = true;
            Specification<AppsMeetingKickoff> appsMeetingKickoffSpecification = new Specification<AppsMeetingKickoff>() {
                @Override
                public Predicate toPredicate(Root<AppsMeetingKickoff> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.equal(root.get("fMainId").get("fId"), main.getfId());
                }
            };
            List<AppsMeetingKickoff> appsMeetingKickoffList = appsMeetingKickoffService.findAll(appsMeetingKickoffSpecification);
            if (!appsMeetingKickoffList.isEmpty()) {
                Specification<MeetingMain> meetingSpecification = new Specification<MeetingMain>() {
                    @Override
                    public Predicate toPredicate(Root<MeetingMain> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        Predicate eq1 = criteriaBuilder.equal(root.get("fModelName"), "AppsMeetingKickoff");
                        CriteriaBuilder.In in = criteriaBuilder.in(root.get("fModelId"));
                        appsMeetingKickoffList.forEach(val -> {
                            in.value(val.getfId());
                        });
                        return criteriaBuilder.and(eq1, in);
                    }
                };
                List<MeetingMain> meetingMainList = meetingMainService.findAll(meetingSpecification);
                if (!meetingMainList.isEmpty()) {
                    Specification<MeetingPackage> packageSpecification = new Specification<MeetingPackage>() {
                        @Override
                        public Predicate toPredicate(Root<MeetingPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                            CriteriaBuilder.In in = criteriaBuilder.in(root.get("fMeetingId").get("fId"));
                            meetingMainList.forEach(val -> {
                                in.value(val.getfId());
                            });
                            return in;
                        }
                    };
                    List<MeetingPackage> meetingPackageList = meetingPackageService.findAll(packageSpecification);

                    Set<String> noPackageMeetingIds = new HashSet<>();
                    meetingMainList.forEach(val -> {
                        noPackageMeetingIds.add(val.getfId());
                    });
                    meetingPackageList.forEach(val -> {
                        noPackageMeetingIds.remove(val.getfMeetingId().getfId());
                    });

                    resultBool = noPackageMeetingIds.isEmpty();
                    if (!noPackageMeetingIds.isEmpty()) {
                        result.put("message", "请将项目启动会信息补充完整\n");
                    }
                }
            }
            result.put("result", resultBool);
        } catch (Exception e) {
            result.put("result", false);
            result.put("message", "校验项目启动会议发生错误：" + e.getMessage() + "\n");
        }
        return result;
    }

    /**
     * 汇报评审会-》会议必须关联包件
     */
    public JSONObject checkReport(AppsProjectMain main) {
        JSONObject result = new JSONObject();
        try {
            boolean resultBool = true;
            Specification<AppsReport> appsReportSpecification = new Specification<AppsReport>() {
                @Override
                public Predicate toPredicate(Root<AppsReport> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.equal(root.get("fMainId").get("fId"), main.getfId());
                }
            };
            List<AppsReport> appsReportList = appsReportService.findAll(appsReportSpecification);
            if (!appsReportList.isEmpty()) {
                Specification<MeetingMain> meetingSpecification = new Specification<MeetingMain>() {
                    @Override
                    public Predicate toPredicate(Root<MeetingMain> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        Predicate eq1 = criteriaBuilder.equal(root.get("fModelName"), "AppsReport");
                        CriteriaBuilder.In in = criteriaBuilder.in(root.get("fModelId"));
                        appsReportList.forEach(val -> {
                            in.value(val.getfId());
                        });
                        return criteriaBuilder.and(eq1, in);
                    }
                };
                List<MeetingMain> meetingMainList = meetingMainService.findAll(meetingSpecification);
                if (!meetingMainList.isEmpty()) {
                    Specification<MeetingPackage> packageSpecification = new Specification<MeetingPackage>() {
                        @Override
                        public Predicate toPredicate(Root<MeetingPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                            CriteriaBuilder.In in = criteriaBuilder.in(root.get("fMeetingId").get("fId"));
                            meetingMainList.forEach(val -> {
                                in.value(val.getfId());
                            });
                            return in;
                        }
                    };
                    List<MeetingPackage> meetingPackageList = meetingPackageService.findAll(packageSpecification);

                    Set<String> noPackageMeetingIds = new HashSet<>();
                    meetingMainList.forEach(val -> {
                        noPackageMeetingIds.add(val.getfId());
                    });
                    meetingPackageList.forEach(val -> {
                        noPackageMeetingIds.remove(val.getfMeetingId().getfId());
                    });

                    resultBool = noPackageMeetingIds.isEmpty();
                    if (!noPackageMeetingIds.isEmpty()) {
                        result.put("message", "请将汇报评审会议或其他会议信息补充完整\n");
                    }
                }
            }
            result.put("result", resultBool);
        } catch (Exception e) {
            result.put("result", false);
            result.put("message", "校验汇报评审会议发生错误：" + e.getMessage() + "\n");
        }
        return result;
    }

    /**
     * 征集结果-》所有包件必须填写征集结果日期
     */
    public JSONObject checkCollectionResult(List<AppsProjectPackage> packages) {
        JSONObject result = new JSONObject();
        try {
            StringBuffer message = new StringBuffer();
            AtomicReference<Date> baseFinishDate = new AtomicReference<>(new Date(0));
            packages.forEach(appsProjectPackage -> {
                if (appsProjectPackage.getfConfirmCollectionResultDate() != null) {
                    if (appsProjectPackage.getfConfirmCollectionResultDate().after(baseFinishDate.get())) {
                        baseFinishDate.set(appsProjectPackage.getfConfirmCollectionResultDate());
                    }
                } else {
                    message.append(appsProjectPackage.getfName() + "未填写征集结果日期\n");
                }
            });
            result.put("result", message.isEmpty());
            result.put("message", message.toString());
            result.put("baseFinishDate", baseFinishDate.get());
        } catch (Exception e) {
            result.put("result", false);
            result.put("message", "校验征集结果发生错误：" + e.getMessage() + "\n");
        }
        return result;
    }

    /**
     * 获取最后一次终期汇报会议的日期作为基本结束日期
     */
    public Date getLastTime(AppsProjectMain main) {
        Date lastTime = null;
        Specification<AppsReport> appsReportSpecification = new Specification<AppsReport>() {
            @Override
            public Predicate toPredicate(Root<AppsReport> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate eq1 = criteriaBuilder.equal(root.get("fMainId").get("fId"), main.getfId());
                Predicate eq2 = criteriaBuilder.equal(root.get("fMeetingType").get("fMeetingKey"), "2");
                Predicate eq3 = criteriaBuilder.equal(root.get("fMeetingType").get("fName"), "终期汇报会");
                return criteriaBuilder.and(eq1, eq2, eq3);
            }
        };
        List<AppsReport> appsReportList = appsReportService.findAll(appsReportSpecification);
        if (!appsReportList.isEmpty()) {
            Specification<MeetingMain> meetingSpecification = new Specification<MeetingMain>() {
                @Override
                public Predicate toPredicate(Root<MeetingMain> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate eq1 = criteriaBuilder.equal(root.get("fModelName"), "AppsReport");
                    CriteriaBuilder.In in = criteriaBuilder.in(root.get("fModelId"));
                    appsReportList.forEach(val -> {
                        in.value(val.getfId());
                    });
                    query.orderBy(criteriaBuilder.desc(root.get("fStartTime")));

                    return criteriaBuilder.and(eq1, in);
                }
            };
            List<MeetingMain> meetingMainList = meetingMainService.findAll(meetingSpecification);
            if (!meetingMainList.isEmpty()) {
                lastTime = meetingMainList.get(0).getfStartTime();
            }
        }
        return lastTime;
    }


}
