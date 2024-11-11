package com.qkinfotech.core.tendering.model.apps.project;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目业主联系人
 */
@Getter
@Setter
@Entity
@Table(name = "apps_project_contacts")
@SimpleModel(url = "apps/project/contacts")
public class AppsProjectContacts extends BaseEntity {

    /**
     * 项目文档id
     * todo
     */
    @JoinColumn(name = "f_main_id")
    @ManyToOne
    private AppsProjectMain fMainId;
    /**
     * 联系人名称
     */
    @Column(name = "f_name", length = 36)
    private String fName;
    /**
     * 联系方式
     */
    @Column(name = "f_phone", length = 36)
    private String fPhone;
    /**
     * 备注
     */
    @Column(name = "f_notes", length = 36)
    private String fNotes;
    /**
     * 业主id
     */
    @Column(name = "f_owner_id", length = 36)
    private String fOwnerId;

}
