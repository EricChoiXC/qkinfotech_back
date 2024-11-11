package com.qkinfotech.core.tendering.model.apps.supplier;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 供应商 资格申请 基本信息
 */

@Getter
@Setter
@Entity
@Table(name = "apps_supplier_info")
@SimpleModel(url = "apps/supplier/info")
public class AppsSupplierInfo extends BaseEntity {

    /**
     * 资格申请id
     */
    @JoinColumn(name = "f_supplier_id")
    @ManyToOne
    private AppsSupplierMain fSupplierId;

    /**
     * 对应联合体公司id
     */
    @JoinColumn(name = "f_invite_company_id")
    @ManyToOne
    private AppsSupplierInviteCompany fInviteCompany;

    /**
     * 公司名称
     */
    @Column(name = "f_company_name",length = 200)
    private String fCompanyName;

    /**
     * 国别
     */
    @Column(name = "f_country",length = 200)
    private String fCountry;

    /**
     * 法定代表人
     */
    @Column(name = "f_legal_representative",length = 200)
    private String fLegalRepresentative;

    /**
     * 公司注册地址
     */
    @Column(name = "f_company_registered_address",length = 200)
    private String fCompanyRegisteredAddress;

    /**
     * 公司成立日期
     */
    @Column(name = "f_incorporation_time",length = 200)
    private String fIncorporationTime;

    /**
     * 公司电话
     */
    @Column(name = "f_company_phone",length = 200)
    private String fCompanyPhone;

    /**
     * 官网地址
     */
    @Column(name = "f_official_website_address",length = 200)
    private String fOfficialWebsiteAddress;

    /**
     * 设计人员总数
     */
    @Column(name = "f_designers_total",length = 200)
    private int fDesignersTotal;

    /**
     * 其中注册建筑/景观设计师
     */
    @Column(name = "f_registered_architects_or_landscape_architects",length = 200)
    private int fRegisteredArchitectsOrLandscapeArchitects;

    /**
     * 商业登记/营业执照编号
     */
    @Column(name = "f_business_registration_or_business_license_number",length = 200)
    private String fBusinessregistrationbusinesslicensenumber;

    /**
     * 设计资格或资质的种类/级别
     */
    @Column(name = "f_design_qualification_type_or_level",length = 200)
    private String fDesignQualificationTypeOrLevel;

    /**
     * 本项目联系人
     */
    @Column(name = "f_contact_person",length = 200)
    private String fContactPerson;

    /**
     * 职务
     */
    @Column(name = "f_duties",length = 200)
    private String fDuties;

    /**
     * 电话
     */
    @Column(name = "f_phone",length = 200)
    private String fPhone;

    /**
     * 邮箱
     */
    @Column(name = "f_email",length = 200)
    private String fEmail;

    /**
     * 通信地址及邮编
     */
    @Column(name = "f_mailing_address_and_postcode",length = 200)
    private String fMailingAddressAndPostcode;

    /**
     * 公司介绍
     */
    @Column(name = "f_company_profile",length = 1000)
    private String fCompanyProfile;


    /**
     * 创建时间
     */
    @Column(name = "f_create_time",length = 200)
    private Date fCreateTime;



}
