package com.qkinfotech.core.tendering.model.apps.pre;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目资格主记录表
 */
@Getter
@Setter
@Entity
@Table(name = "apps_pre_audit_main")
@SimpleModel(url = "apps/pre/audit/main")
public class AppsPreAuditMain extends BaseEntity {

    /**
     * 项目id
     */
    @JoinColumn(name = "f_project_id")
    @ManyToOne
    private AppsProjectMain projectId;

    /**
     * 创建时间
     */
    @Column(name = "f_create_time", length = 36)
    private String fCreateTime;

}
