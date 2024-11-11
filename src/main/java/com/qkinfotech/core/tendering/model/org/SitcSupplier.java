package com.qkinfotech.core.tendering.model.org;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 供应商
 */
@Getter
@Setter
@Entity
@Table(name = "sitc_supplier")
@SimpleModel(url = "sitc/supplier")
public class SitcSupplier extends BaseEntity {

    @Column(name = "f_org_id", length = 200)
    private String fOrgId;

    private String fName;

    private String fEmail;

    private String fSupplierType;

    private String fCreditCode;

    private String fFullName;

    private String fArea;

    private String fAttest;

    private String fCode;

    private String fPrincipal;

    private String fContactPerson;

    private String fWeixin;

    private String fTaxpayerType;

    private String fBillingAddress;

    private String fBillingPhone;

    private String fAddress;

    private String fPostcode;

    private String fTelephone;

    private String fFax;

    @Column(name = "f_business", length = 5000)
    private String fBusiness;

    private String fWebsite;

    private String fTaxpayer;

    private String fBank;

    private String fAccount;

    private String fDepositBank;

    private String fDepositAccount;

    private String fAccountCode;

    private Integer fUpperLimit;

    private String fLinkmanTel;

    private String fParentId;

    private String fNumber;

    private String fOpenId;

    private String fNickName;

    private String fDentify = "0";

    private String fSupplierId;

    private String fPapersNumber;

    private String fSupplierIndustry;

}
