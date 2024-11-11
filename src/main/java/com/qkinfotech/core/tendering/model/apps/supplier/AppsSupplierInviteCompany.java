package com.qkinfotech.core.tendering.model.apps.supplier;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgCompany;
import com.qkinfotech.core.org.model.OrgPerson;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 联合体公司
 */

@Getter
@Setter
@Entity
@Table(name = "apps_supplier_invite_company")
@SimpleModel(url = "apps/supplier/invite/company")
public class AppsSupplierInviteCompany extends BaseEntity {
    /**
     * 资格申请
     */
    @JoinColumn(name = "f_supplier_id")
    @ManyToOne
    private AppsSupplierMain fSupplierId;

    /**
     * 公司名称
     */
    @Column(name = "f_company_name")
    private String fCompanyName;

    /**
     * 公司id
     */
    @JoinColumn(name = "f_company_id")
    @ManyToOne
    private OrgPerson fCompanyId;

    /**
     * 是否主体
     */
    @Column(name = "f_is_main")
    private String fIsMain;



    /**
     * 是否申请公司填写
     */
    @Column(name = "f_is_create")
    private boolean fIsCreate;

    /**
     * 国别
     */
    @Column(name = "f_country")
    private String fCountry;
    /**
     * 设计师名称
     */
    @Column(name = "f_designer_name")
    private String fDesignerName;

}
