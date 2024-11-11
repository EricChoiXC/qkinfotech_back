package com.qkinfotech.core.tendering.model.apps.notice;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *  供应商 所购买包件 关系
 */

@Getter
@Setter
@Entity
@Table(name = "apps_company_bid_package")
@SimpleModel(url = "apps/company/bid/package")
public class AppsCompanyBidPackage extends BaseEntity {

    /**
     * 项目id
     */
    @JoinColumn(name = "f_project_main_id")
    @ManyToOne
    private AppsProjectMain fProjectMain;


    /**
     * 公告id
     */
    @JoinColumn(name = "f_notice_main_id")
    @ManyToOne
    private AppsNoticeMain fNoticeMain;


    /**
     *供应商id
     */
    @JoinColumn(name = "f_supplier_id")
    @ManyToOne
    private OrgPerson fSupplier;

    /**
     * 包件id
     */
    @JoinColumn(name = "f_package_id")
    @ManyToOne
    private AppsProjectPackage fPackage;

}
