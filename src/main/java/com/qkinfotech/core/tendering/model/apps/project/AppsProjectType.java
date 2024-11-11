package com.qkinfotech.core.tendering.model.apps.project;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.masterModels.MasterDataScale;
import com.qkinfotech.core.tendering.model.masterModels.MasterDataType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目类型
 */
@Getter
@Setter
@Entity
@Table(name = "apps_project_type")
@SimpleModel(url = "apps/project/type")
public class AppsProjectType extends BaseEntity {
    /**
     * 项目id
     */
    @JoinColumn(name = "f_main_id")
    @ManyToOne
    private AppsProjectMain fMainId;


    /**
     * 类型id
     */
    @JoinColumn(name = "f_type_id")
    @ManyToOne
    private MasterDataType fTypeId;


    /**
     * 规模id
     */
    @JoinColumn(name = "f_scale_id")
    @ManyToOne
    private MasterDataScale fScaleId;


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
