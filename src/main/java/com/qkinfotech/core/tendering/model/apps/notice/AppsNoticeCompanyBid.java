package com.qkinfotech.core.tendering.model.apps.notice;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 公告信息同步 标书单位信息
 */
@Getter
@Setter
@Entity
@Table(name = "apps_notice_company_bid")
@SimpleModel(url = "apps/notice/company/bid")
public class AppsNoticeCompanyBid extends BaseEntity {
    /**
     * 单位名称
     */
    @Column(name = "f_name",length = 200)
    private String fName;

    /**
     * 联系人
     */
    @Column(name = "f_contacts",length = 200)
    private String fContacts;

    /**
     * 申请人账号id
     */
    @Column(name = "f_apply_id",length = 200)
    private String fApplyId;


    /**
     * 申请人账号名称
     */
    @Column(name = "f_apply_name",length = 200)
    private String fApplyName;


    /**
     * 手机
     */
    @Column(name = "f_phone",length = 200)
    private String fPhone;

    /**
     * 邮箱
     */
    @Column(name = "f_email",length = 200)
    private String fEmail;

    /**
     * 流程状态
     */
    @Column(name = "f_status",length = 200)
    private String fStatus;

    /**
     * 业务状态
     * enums.fd_business_status.00=受理中
     * enums.fd_business_status.01=驳回
     * enums.fd_business_status.02=已受理
     * enums.fd_business_status.03=无需审核
     */
    @Column(name = "f_business_status",length = 200)
    private String fBusinessStatus;

    /**
     * 领购单位
     */
    @Column(name = "f_unit_name",length = 200)
    private String fUnitName;

    /**
     * 所选包件名称
     */
    @Column(name = "f_package_name",length = 2000)
    private String fPackageName;

    /**
     * 所选包件编号
     */
    @Column(name = "f_package_code",length = 2000)
    private String fPackageCode;

    /**
     * 快递地址
     */
    @Column(name = "f_address",length = 200)
    private String fAddress;

    /**
     * 详细快递地址
     */
    @Column(name = "f_detailed_address",length = 400)
    private String fDetailedAddress;

    /**
     * 开票抬头
     */
    @Column(name = "f_invoice_title" ,length = 200)
    private String fInvoiceTitle;

    /**
     * 开票类型 62-全电普票  61-全电专票
     */
    @Column(name = "f_invoice_type" ,length = 200)
    private String fInvoiceType;

    /**
     * 开票发票统一社会信用代码
     */
    @Column(name = "f_invoice_code" ,length = 200)
    private String fInvoiceCode;


    /**
     * 创建时间
     */
    @Column(name = "f_create_time",length = 200)
    private Date fCreateTime;


    /**
     * 领购id
     */
    @Column(name = "f_ekp_id",length = 200)
    private String fEkpid;


    /**
     * 资格预审公告
     */
    @JoinColumn(name = "f_main_id")
    @ManyToOne
    private AppsNoticeMain fMain;

}
