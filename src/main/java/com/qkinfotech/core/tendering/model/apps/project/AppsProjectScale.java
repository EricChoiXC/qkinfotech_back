package com.qkinfotech.core.tendering.model.apps.project;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.masterModels.MasterDataScale;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目规模
 */
@Getter
@Setter
@Entity
@Table(name = "apps_project_scale")
@SimpleModel(url = "apps/project/scale")
public class AppsProjectScale extends BaseEntity {
    /**
     * 项目id
     */
    @JoinColumn(name = "f_main_id")
    @ManyToOne
    private AppsProjectMain fMain;


    /**
     * 规模id
     */
    @JoinColumn(name = "f_scale_id")
    @ManyToOne
    private MasterDataScale fScale;

    /**
     * 实际值
     */
    @Column(name = "f_value", length = 1000)
    private String fValue;

    /**
     * 分组名
     */
    @Column(name = "f_group", length = 1000)
    private String fGroup;
}
