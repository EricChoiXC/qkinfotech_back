package com.qkinfotech.core.tendering.model.apps.project;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目业主
 */
@Getter
@Setter
@Entity
@Table(name = "apps_project_owner")
@SimpleModel(url = "apps/project/owner")
public class AppsProjectOwner extends BaseEntity {
    /**
     * 主文档id
     */
    @JoinColumn(name = "f_main_id")
    @ManyToOne
    private AppsProjectMain fMainId;

    /**
     *  业主
     */
    @Column(name = "f_name", length = 36)
    private String fName;
}
