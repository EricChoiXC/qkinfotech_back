package com.qkinfotech.core.tendering.model.apps.project;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.masterModels.MasterDataNature;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目 性质表
 */
@Getter
@Setter
@Entity
@Table(name = "apps_project_natures")
@SimpleModel(url = "apps/project/natures")
public class AppsProjectNatures extends BaseEntity {
    /**
     * 项目文档id
     */
    @JoinColumn(name = "f_main_id")
    @ManyToOne
    private AppsProjectMain fMainId;


    /**
     * 性质id
     */
    @JoinColumn(name = "f_nature_id")
    @ManyToOne
    private MasterDataNature fNatureId;

}
