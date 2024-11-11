package com.qkinfotech.core.tendering.model.apps.project;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 归档信息
 */
@Getter
@Setter
@Entity
@Table(name = "apps_project_documentation")
@SimpleModel(url = "apps/project/documentation")
public class AppsProjectDocumentation extends BaseEntity {
    /**
     * 项目id
     */
    @JoinColumn(name = "f_project_id")
    @ManyToOne
    private AppsProjectMain fProjectId;

    /**
     * 投标人数量
     */
    @Column(name = "f_tender_num",length =200 )
    private int fTenderNum;

    /**
     * 公司电子采购平台进场招标
     */
    @Column(name = "f_is_company_procurement",length = 2)
    private String fIsCompanyProcurement;


    /**
     * 使用语言
     */
    @Column(name = "f_language",length = 200)
    private String fLanguage;

    /**
     * 翻译成果校对
     */
    @Column(name = "f_translate_result",length = 200)
    private String fTranslateResult;

    /**
     * 履约奖
     */
    @Column(name = "f_perpormance_price",length = 200)
    private String fPerpormancePrice;

    /**
     * 履约奖发奖日期
     */
    @Column(name = "f_perpormance_time",length = 200)
    private Date fPerpormanceTime;

    /**
     * 开拓奖
     */
    @Column(name = "f_develop_price",length = 200)
    private String fDevelopPrice;

    /**
     * 开拓发奖日期
     */
    @Column(name = "f_develop_time",length = 200)
    private Date fDevelopTime;

    /**
     * 实际代理费
     */
    @Column(name = "f_agency_fee",length = 200)
    private String fAgencyFee;

    /**
     * 项目登记日期
     */
    @Column(name = "f_project_register_time",length = 200)
    private Date fProjectRegisterTime;

    /**
     * 基本结束日期
     */
    @Column(name = "f_base_finish_time",length = 200)
    private Date fBaseFinishTime;

    /**
     * 限制结束日期
     */
    @Column(name = "f_limit_finish_time",length = 200)
    private Date fLimitFinishTime;

    /**
     * 达标日期
     */
    @Column(name = "f_quelify_time",length = 200)
    private Date fQuelifyTime;


    /**
     * 协议归档编号
     */
    @Column(name = "f_protocol_documentation_no",length = 200)
    private String fProtocolDocumentationNo;

    /**
     * 协议归档日期
     */
    @Column(name = "f_protocol_documentation_time",length = 200)
    private Date fProtocolDocumentationTime;

    /**
     * 履约评价表编号
     */
    @Column(name = "f_evaluation_no",length = 200)
    private String fEvaluationNo;

    /**
     * 履约上交日期
     */
    @Column(name = "f_evaluation_time",length = 200)
    private Date fEvaluationTime;


    /**
     * 归档编号
     */
    @Column(name = "f_documentation_no",length = 200)
    private String fDocumentationNo;

    /**
     * 归档日期
     */
    @Column(name = "f_documentation_time",length = 200)
    private Date fDocumentationTime;


    /**
     * 财务结算日期
     */
    @Column(name = "f_finance_balance_time",length = 200)
    private Date fFinanceBalanceTime;

    /**
     * 费用结算日期
     */
    @Column(name = "f_fee_balance_time",length = 200)
    private Date fFeeBalanceTime;

    /**
     * 电子归档查看url
     */
    @Column(name = "f_documentation_url",length = 500)
    private String fDocumentationUrl;

    /**
     * 归档申请状态 0-未归档  1-申请归档 2-已归档
     */
    @Column(name = "f_documentation_status",length = 200)
    private String fDocumentationStatus = "0";


}
