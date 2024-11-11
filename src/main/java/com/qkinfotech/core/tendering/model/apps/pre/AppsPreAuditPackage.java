package com.qkinfotech.core.tendering.model.apps.pre;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 资格预审包件
 */
@Getter
@Setter
@Entity
@Table(name = "apps_pre_audit_package")
@SimpleModel(url = "apps/pre/audit/package")
public class AppsPreAuditPackage extends BaseEntity {
    /**
     * 资格预审id
     */
    @JoinColumn(name = "f_prequalification_id")
    @ManyToOne
    private AppsPreAuditMain fPrequalificationId;
    /**
     * 包件id
     */
    @JoinColumn(name = "f_package_id")
    @ManyToOne
    private AppsProjectPackage fPackageId;
}
