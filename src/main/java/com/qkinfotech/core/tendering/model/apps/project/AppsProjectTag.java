package com.qkinfotech.core.tendering.model.apps.project;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.masterModels.MasterDataTag;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "apps_project_tag")
@SimpleModel(url = "apps/project/tag")
//f_main_id	项目id
//f_tag_id	标签id
public class AppsProjectTag extends BaseEntity {
    /**
     * 项目文档id
     */
    @JoinColumn(name = "f_main_id")
    @ManyToOne
    private AppsProjectMain fMainId;
    /**
     * 标签id
     */
    @JoinColumn(name = "f_tag_id")
    @ManyToOne
    private MasterDataTag fTagId;

}
