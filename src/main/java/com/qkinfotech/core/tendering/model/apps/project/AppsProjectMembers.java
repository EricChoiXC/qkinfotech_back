package com.qkinfotech.core.tendering.model.apps.project;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgPerson;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目组成员
 */
@Getter
@Setter
@Entity
@Table(name = "apps_project_members")
@SimpleModel(url = "apps/project/members")
public class AppsProjectMembers extends BaseEntity {
    /**
     * 项目文档id
     */
    @JoinColumn(name = "f_main_id")
    @ManyToOne
    private AppsProjectMain fMainId;


    /**
     * 人员id
     */
    @JoinColumn(name = "f_person_id")
    @ManyToOne
    private OrgPerson fPersonId;




}
