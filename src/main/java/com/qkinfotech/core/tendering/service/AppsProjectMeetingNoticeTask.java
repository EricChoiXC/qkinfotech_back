package com.qkinfotech.core.tendering.service;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.org.model.OrgCompany;
import com.qkinfotech.core.org.model.OrgDept;
import com.qkinfotech.core.org.model.OrgGroupCate;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.task.ITask;
import com.qkinfotech.core.task.Task;
import com.qkinfotech.core.task.TaskLogger;
import com.qkinfotech.core.tendering.model.apps.meeting.AppsMeetingKickoff;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingMain;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeMain;
import com.qkinfotech.core.tendering.model.apps.pre.AppsPreAuditMeeting;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectDocumentation;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMeetingNotice;
import com.qkinfotech.core.tendering.model.apps.report.AppsReport;
import com.qkinfotech.core.user.model.SysUser;
import com.qkinfotech.util.DateUtils;
import com.qkinfotech.util.IDGenerate;
import com.qkinfotech.util.SqlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;
import java.util.*;

@Task(trigger = "cron:0 0 1 1/1 * ?", group = "Project", name = "定时检测会议是否需要会议通知")
public class AppsProjectMeetingNoticeTask implements ITask {

    @Autowired
    private SimpleService<AppsProjectMain> appsProjectMainService;
    @Autowired
    private SimpleService<AppsNoticeMain> appsNoticeMainService;
    @Autowired
    private SimpleService<AppsProjectMeetingNotice> appsProjectMeetingNoticeService;
    @Autowired
    private SimpleService<AppsPreAuditMeeting> appsPreAuditMeetingService;
    @Autowired
    private SimpleService<AppsMeetingKickoff> appsMeetingKickoffService;
    @Autowired
    private SimpleService<MeetingMain> meetingMainService;
    @Autowired
    private SimpleService<AppsReport> appsReportService;
    @Autowired
    private SimpleService<AppsProjectDocumentation> appsProjectDocumentationService;
    @Autowired
    private PlatformTransactionManager transactionManager;


    @Override
    public void execute(TaskLogger logger, JSONObject parameter) throws Exception {
        //需要添加的会议通知列表
        List<AppsProjectMeetingNotice> appsProjectMeetingNoticeList = new ArrayList<>();
        JSONObject queryJson = new JSONObject();
        //获取今日日期
        String nowDate = DateUtils.getDate();
        //构建参数
        SqlUtil.setParameter(queryJson,"fAuditStatus","1","query","eq");
        //查询项目列表
        List<AppsProjectMain> projectList = appsProjectMainService.findAll(JSONQuerySpecification.getSpecification(queryJson));
        projectList.forEach(project -> {
            //遍历项目列表，检测项目中的各个需要的会议提醒
            String message = "";
            //资格预审公告信息-申请文件截至日期
            boolean isOk = checkApplyFile(project,nowDate);
            if (isOk) {
                //执行添加提醒操作
                message += project.getfName() + "项目申请文件截止日期截止到今天";
                initMeetingNotice(appsProjectMeetingNoticeList,message,project,nowDate);
                isOk = false;
            }

            //资格预审会议信息-会议开始时间
            isOk = checkQualificationMeeting(project,nowDate);
            if (isOk) {
                //执行添加提醒操作
                message += project.getfName() + "项目有资格预审会议";
                initMeetingNotice(appsProjectMeetingNoticeList,message,project,nowDate);
                isOk = false;
            }

            //项目启动会议开始时间
            isOk = checkProjectStartDate(project,nowDate);
            if (isOk) {
                //执行添加提醒操作
                message += project.getfName() + "项目有项目启动会";
                initMeetingNotice(appsProjectMeetingNoticeList,message,project,nowDate);
                isOk = false;
            }

            //汇报评审会-会议开始时间
            isOk = checkReportReviewDate(project,nowDate);
            if (isOk) {
                //执行添加提醒操作
                message += project.getfName() + "项目有汇报评审会";
                initMeetingNotice(appsProjectMeetingNoticeList,message,project,nowDate);
                isOk = false;
            }

            //归档履约信息-基本结束日期
            int resultNum = checkFilePerformanceBasicEnd(project,nowDate);
            if(resultNum == 1){
                //基本结束日期符合条件
                message += project.getfName() + "项目已到达基本结束日期";
                initMeetingNotice(appsProjectMeetingNoticeList,message,project,nowDate);
            }else if(resultNum == 2){
                //限制结束日期符合条件
                message += project.getfName() + "项目已到达限制结束日期";
                initMeetingNotice(appsProjectMeetingNoticeList,message,project,nowDate);
            }
        });

        if(!appsProjectMeetingNoticeList.isEmpty()){
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
            TransactionStatus status = transactionManager.getTransaction(def);

            try {
                //执行添加
                appsProjectMeetingNoticeList.forEach(appsProjectMeetingNotice -> {
                    appsProjectMeetingNoticeService.save(appsProjectMeetingNotice);
                });
                transactionManager.commit(status);
            }catch (Exception e){
                //添加报错
                logger.write(e);
                transactionManager.rollback(status);
            }
        }
    }

    /**
     * 判断申请文件是否符合条件
     * @param projectMain
     * @param nowDate
     * @return
     */
    private boolean checkApplyFile(AppsProjectMain projectMain,String nowDate){
        boolean flag = false;
        JSONObject queryJson = new JSONObject();
        SqlUtil.setParameter(queryJson,"fProjectId.fId",projectMain.getfId(),"query","eq");
        AppsNoticeMain appsNoticeMain = appsNoticeMainService.findOne(JSONQuerySpecification.getSpecification(queryJson));
        if(null != appsNoticeMain){
            String applyDate = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD,appsNoticeMain.getfApplicationDocumentDeadline());
            if(nowDate.equals(applyDate)){
                //符合条件，需要添加提醒
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 判断资格预审会议开始时间
     * @param projectMain
     * @param nowDate
     * @return
     */
    private boolean checkQualificationMeeting(AppsProjectMain projectMain,String nowDate){
        boolean flag = false;
        JSONObject queryJson = new JSONObject();
        SqlUtil.setParameter(queryJson,"fProjectId.fId",projectMain.getfId(),"query","eq");
        AppsPreAuditMeeting appsPreAuditMeeting = appsPreAuditMeetingService.findOne(JSONQuerySpecification.getSpecification(queryJson));
        if(null != appsPreAuditMeeting){
            String applyDate = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD,appsPreAuditMeeting.getfMeetingStartTime());
            if(nowDate.equals(applyDate)){
                //符合条件，需要添加提醒
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 判断项目启动会议开始时间
     * @param projectMain
     * @param nowDate
     * @return
     */
    private boolean checkProjectStartDate(AppsProjectMain projectMain,String nowDate){
        boolean flag = false;
        JSONObject queryJson = new JSONObject();
        SqlUtil.setParameter(queryJson,"fMainId.fId",projectMain.getfId(),"query","eq");
        AppsMeetingKickoff appsMeetingKickoff = appsMeetingKickoffService.findOne(JSONQuerySpecification.getSpecification(queryJson));
        if(null != appsMeetingKickoff){
            //再查询会议总表
            queryJson = new JSONObject();
            SqlUtil.setParameter(queryJson,"fModelName",appsMeetingKickoff.getClass().getName(),"query","eq");
            SqlUtil.setParameter(queryJson,"fModelId",appsMeetingKickoff.getfId(),"query","eq");
            MeetingMain meetingMain = meetingMainService.findOne(JSONQuerySpecification.getSpecification(queryJson));
            if(null != meetingMain){
                String checkDate = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD,meetingMain.getfStartTime());
                if(nowDate.equals(checkDate)){
                    //符合条件，需要添加提醒
                    flag = true;
                }
            }
        }
        return flag;
    }

    /**
     * 判断汇报评审会议开始时间
     * @param projectMain
     * @param nowDate
     * @return
     */
    private boolean checkReportReviewDate(AppsProjectMain projectMain,String nowDate){
        boolean flag = false;
        JSONObject queryJson = new JSONObject();
        SqlUtil.setParameter(queryJson,"fMainId.fId",projectMain.getfId(),"query","eq");
        AppsReport appsReport = appsReportService.findOne(JSONQuerySpecification.getSpecification(queryJson));
        if(null != appsReport){
            //再查询会议总表
            queryJson = new JSONObject();
            SqlUtil.setParameter(queryJson,"fModelName",appsReport.getClass().getName(),"query","eq");
            SqlUtil.setParameter(queryJson,"fModelId",appsReport.getfId(),"query","eq");
            MeetingMain meetingMain = meetingMainService.findOne(JSONQuerySpecification.getSpecification(queryJson));
            if(null != meetingMain){
                String checkDate = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD,meetingMain.getfStartTime());
                if(nowDate.equals(checkDate)){
                    //符合条件，需要添加提醒
                    flag = true;
                }
            }
        }
        return flag;
    }

    /**
     * 判断归档履约信息-基本结束日期/限制结束日期
     * @param projectMain
     * @param nowDate
     * @return
     */
    private int checkFilePerformanceBasicEnd(AppsProjectMain projectMain,String nowDate){
        int flag = 0;
        JSONObject queryJson = new JSONObject();
        SqlUtil.setParameter(queryJson,"fProjectId.fId",projectMain.getfId(),"query","eq");
        AppsProjectDocumentation appsProjectDocumentation = appsProjectDocumentationService.findOne(JSONQuerySpecification.getSpecification(queryJson));
        if(null != appsProjectDocumentation){
            //判断基本结束日期
            String applyDate = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD,appsProjectDocumentation.getfBaseFinishTime());
            if(nowDate.equals(applyDate)){
                //符合条件，需要添加提醒
                flag = 1;
            }
            //判断限制结束日期
            applyDate = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD,appsProjectDocumentation.getfLimitFinishTime());
            if(nowDate.equals(applyDate)){
                //符合条件，需要添加提醒
                flag = 2;
            }

        }
        return flag;
    }

    /**
     * 向需要添加的会议提醒列表中添加数据
     * @param appsProjectMeetingNoticeList
     * @param message
     * @param fProject
     * @param nowDate
     */
    private void initMeetingNotice(List<AppsProjectMeetingNotice> appsProjectMeetingNoticeList,String message,AppsProjectMain fProject,String nowDate){
        AppsProjectMeetingNotice meetingNotice = new AppsProjectMeetingNotice();
        meetingNotice.setfId(IDGenerate.generate());
        meetingNotice.setfUser(fProject.getfDeptManager());
        meetingNotice.setfMessage(message);
        meetingNotice.setfProject(fProject);
        meetingNotice.setfMeetingDate(nowDate);
        meetingNotice.setfCreateTime(new Date());
        appsProjectMeetingNoticeList.add(meetingNotice);
    }

}
