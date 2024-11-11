package com.qkinfotech.core.tendering.model.apps.designer;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 项目业绩信息
 */
@Getter
@Setter
@Entity
@Table(name = "apps_designer_achievement")
@SimpleModel(url = "apps/designer/achievement")
public class AppsDesignerAchievement extends BaseEntity {

    /**
     * 项目名称
     */
    @Column(name = "f_name", length = 2000)
    private String fName;

    /**
     * 项目地点
     */
    @Column(name = "f_project_place", length =200)
    private String fProjectPlace;

    /**
     * 业主名称
     */
    @Column(name = "f_owner_name", length =200)
    private String fOwnerName;

    /**
     * 功能
     */
    @Column(name = "f_functionality", length =2000)
    private String fFunctionality;

    /**
     * 规格
     */
    @Column(name = "f_norm", length =200)
    private String fNorm;

    /**
     * 服务开始日期
     */
    @Column(name = "f_service_start_time", length =200)
    private Date fServiceStartTime;
    /**
     * 服务结束日期
     */
    @Column(name = "f_service_end_time", length =200)
    private Date fServiceEndTime;

    /**
     * 项目现状
     */
    @Column(name = "f_project_status", length =200)
    private String fProjectStatus;

    /**
     * 承担工作内容
     */
    @Column(name = "f_undertaking_work", length =2000)
    private String fUndertakingWork;

    /**
     * 创建时间
     */
    @Column(name = "f_create_time",length = 200)
    private Date fCreateTime;

    /**
     * 主创设计师
     */
    @JoinColumn(name = "f_designer_id")
    @ManyToOne
    private AppsDesignerMain fDesigner;

    /**
     * 资格申请id
     */
    @JoinColumn(name = "f_supplier_id")
    @ManyToOne
    private AppsSupplierMain fSupplierId;

}
