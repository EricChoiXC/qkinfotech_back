package com.qkinfotech.core.tendering.model.apps.project;

import com.alibaba.fastjson.annotation.JSONField;
import com.qkinfotech.core.org.model.OrgDept;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.tendering.model.masterModels.*;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 项目表
 */
@Getter
@Setter
@Entity
@Table(name = "apps_project_main")
@SimpleModel(url = "apps/project/main")
public class AppsProjectMain extends BaseEntity {
//

    /**
     * 项目名称
     */
    @Column(name = "f_name", length = 2000)
    private String fName;

    /**
     * 项目类型
     */
    @JoinColumn(name = "f_project_type")
    @ManyToOne
    private MasterDataType fProjectType;

    /**
     * 项目简称
     */
    @Column(name = "f_simple_name", length = 2000)
    private String fSimpleName;


    /**
     * 项目英文名称
     */
    @Column(name = "f_eng_name", length = 2000)
    private String fEngName;

    /**
     * 协议编号
     */
    @Column(name = "f_protocol_number", length = 200)
    private String fProtocolNumber;

    /**
     * 项目编号
     */
    @Column(name = "f_protocol_no", length = 200)
    private String fProtocolNo;

    /**
     * 项目模式
     */
    @JoinColumn(name = "f_project_mode")
    @ManyToOne
    private MasterDataMode fProjectMode;

    /**
     * 申请日期
     */
    @Column(name = "f_create_time", length = 2000)
    private Date fCreateTime;

    /**
     * 业主
     */
    @Column(name = "f_owner", length = 2000)
    private String fOwner;

    /**
     * 部门
     */
    @JSONField(serialize = false)
    @JoinColumn(name = "f_dept")
    @ManyToOne
    private OrgDept fDept;

    /**
     * 项目经理
     */
    @JSONField(serialize = false)
    @JoinColumn(name = "f_dept_manager")
    @ManyToOne
    private OrgPerson fDeptManager;

    /**
     * 项目性质
     */
    @JoinColumn(name = "f_project_nature")
    @ManyToOne
    private MasterDataNature fProjectNature;

    /**
     * 其他项目性质
     */
    @Column(name = "f_project_nature_other", length = 200)
    private String fProjectNatureOther;


    //    fProjectManagers fFundingSource "fCountryVal": "CHN",
    //    "fcityVal":  fProjectTags  fProjectScale fProjectPackage

    /**
     * 项目重要性
     */
    @JoinColumn(name = "f_project_importance")
    @ManyToOne
    private MasterDataImportance fProjectImportance;


    /**
     * 项目预算
     */
    @Column(name = "f_project_budget", length = 200)
    private float fProjectBudget;

    /**
     * 计划用汇
     */
    @Column(name = "f_plan_foregin_exchange", length = 200)
    private float fPlanForeignExchange;


    /**
     * 资金来源
     */
    @JoinColumn(name = "f_capital_source")
    @ManyToOne
    private MasterDataFund fCapitalSource;

    /**
     * 其他资金来源
     */
    @Column(name = "f_capital_source_other", length = 200)
    private String fCapitalSourceOther;

    /**
     * 预期收入
     */
    @Column(name = "f_plan_income", length =200)
    private float fPlanIncome;

//    /**
//     * 项目典型性
//     */
//    @Column(name = "f_typical", length =200)
//    private String fTypical;

    /**
     * 开拓信息
     */
    @Column(name = "f_develop_info", length =200)
    private String fDevelopInfo;

    /**
     * 项目执行地 城市
     */
    @JoinColumn(name = "f_execution_city")
    @ManyToOne
    private MasterDataCity fExecutionCity;

    /**
     * 项目执行地  国家
     */
    @JoinColumn(name = "f_execution_Country")
    @ManyToOne
    private MasterDataCountry fExecutionCountry;


    /**
     * 是否可公布
     */
    @Column(name = "f_can_publish", length =10   )
    private String fCanPublish;

    /**
     * 限制时间
     */
    @Column(name = "f_publish_time", length =200   )
    private Date fPublishTime;


    /**
     * 是否涉密
     */
    @Column(name = "f_is_classified"  )
    private Boolean fIsClassified;

    /**
     * 项目地点
     */
    @Column(name = "f_project_place", length =200   )
    private String fProjectPlace;

    /**
     * 审核状态
     * 0 : 待审
     * 1 : 流程审核通过/项目进行中
     * 2 : 流程审核驳回
     * 3 : 项目取消
     * 4 : 项目终止
     */
    @Column(name = "f_audit_status", length =200)
    private String fAuditStatus;


    /**
     * 项目类别
     */
    @JoinColumn(name = "f_project_category"  )
    @ManyToOne
    private MasterDataCategory fProjectCategory;

    /**
     * 当前处理人名称
     */
    @Column(name = "f_current_processor_name", length =50   )
    private String fCurrentProcessorName;

    /**
     * 当前处理人id
     */
    @Column(name = "f_current_processor_id", length =32   )
    private String fCurrentProcessorId;

    /**
     * 流程id
     */
    @Column(name = "f_review_Id", length =200  )
    private String fReviewId;

    /**
     * 送审时间
     */
    @Column(name = "f_submittal_time")
    private Date fSubmittalTime;


    /**
     * 基本结束时间
     */
    @Column(name = "f_base_finish_time")
    private Date fBaseFinishTime;


    /**
     * 限制结束日期
     */
    @Column(name = "f_limit_finish_time")
    private Date fLimitFinishTime;


    /**
     * 达标日期
     */
    @Column(name = "f_qualify_time")
    private Date fQualifyTime;



    @OneToMany(cascade = { CascadeType.ALL })
    @JoinColumn(name="f_project_main_id")
    private List<AppsProjectMainHistory> fAppsProjectMainHistoryList;


    /**
     * 成果语言
     */
    @Column(name = "f_achievement_language", length=200)
    private String fAchievementLanguage;


    /**
     * 是否有项目启动会
     */
    @Column(name = "f_is_project_start"  )
    private Boolean fIsProjectStart;

    /**
     * 是否有汇报评审
     */
    @Column(name = "f_is_project_report_review"  )
    private Boolean fIsProjectReportReview;

    /**
     * 项目备注
     */
    @Column(name = "f_notes", length = 2000)
    private String fNotes;

    /**
     * 其他项目类型
     */
    @Column(name = "f_type_other", length = 200)
    private String fTypeOther;

    /**
     * 其他项目标签
     */
    @Column(name = "f_tag_other", length = 200)
    private String fTagOther;

    /**
     * 征集文件截止日期
     */
    @Column(name = "f_col_file_fin_time")
    private Date fColFileFinTime;
}
