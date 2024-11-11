package com.qkinfotech.core.tendering.model.apps.designer;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 主创同类设计业绩
 */
@Getter
@Setter
@Entity
@Table(name = "apps_design_performance")
@SimpleModel(url = "apps/design/performance")
public class AppsDesignPerformance extends BaseEntity {
    /**
     * 同类项目名称
     */
    @Column(name = "f_same_category_project_name",length = 200)
    private String fSameCategoryProjectName;

    /**
     * 设计师id
     */
    @JoinColumn(name = "f_designer_id")
    @ManyToOne
    private AppsDesignerMain fDesignerId;

    /**
     * 创建时间
     */
    @Column(name = "f_create_time",length = 200)
    private Date fCreateTime;


}
