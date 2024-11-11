package com.qkinfotech.core.tendering.model.apps.finalization;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgCompany;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 入围名单对应包件
 */
@Getter
@Setter
@Entity
@Table(name = "apps_finaliaztion_result_package")
@SimpleModel(url = "apps/finaliaztion/result/package")
public class AppsFinalizationResultPackage extends BaseEntity {

    /**
     * 公司
     */
    @JoinColumn(name = "f_company_id")
    @ManyToOne
    private OrgPerson fCompanyId;


    /**
     * 项目id
     */
    @JoinColumn(name = "f_project_id")
    @ManyToOne
    private AppsProjectMain fProjectId;



    /**
     * 包件id
     */
    @JoinColumn(name = "f_package_id")
    @ManyToOne
    private AppsProjectPackage fPackageId;


    @Column(name = "f_create_time")
    private Date fCreateTime;
}
