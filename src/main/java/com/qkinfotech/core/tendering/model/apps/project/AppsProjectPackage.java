package com.qkinfotech.core.tendering.model.apps.project;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 项目包件
 */
@Getter
@Setter
@Entity
@Table(name = "apps_project_package")
@SimpleModel(url = "apps/project/package")
public class AppsProjectPackage extends BaseEntity {
    /**
     * 项目id
     */
    @JoinColumn(name = "f_main_id")
    @ManyToOne
    private AppsProjectMain fMainId;

    /**
     * 包件名称
     */
    @Column(name = "f_name", length = 200)
    private String fName;

    /**
     *  index
     */
    @Column(name = "f_index", length = 200)
    private Integer fIndex;

    /**
     * 确认征集结果日期
     */
    @Column
    private Date fConfirmCollectionResultDate;
}
