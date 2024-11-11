package com.qkinfotech.core.tendering.model.apps.supplier;

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
 * 资格申请包件关联
 */

@Getter
@Setter
@Entity
@Table(name = "apps_supplier_package")
@SimpleModel(url = "apps/supplier/package")
public class AppsSupplierPackage extends BaseEntity {
    /**
     * 供应商 main
     */
    @JoinColumn(name = "f_supplier_id")
    @ManyToOne
    private AppsSupplierMain fSupplier;

    /**
     * 包件id
     */
    @JoinColumn(name = "f_package_id")
    @ManyToOne
    private AppsProjectPackage fPackageId;




}
