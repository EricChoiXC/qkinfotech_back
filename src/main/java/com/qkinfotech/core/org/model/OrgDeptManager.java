package com.qkinfotech.core.org.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="org_dept_manager")
@SimpleModel(url = "org/dept/manager")
public class OrgDeptManager extends BaseEntity {

    /**
     * 部门id
     */
    @Column(name = "f_dept_id",length = 36)
    private String fDeptId;


    /**
     * 部门经理id
     */
    @Column(name = "f_dept_manager_id",length = 36)
    private String fDeptManagerId;

    /**
     * 角色名称
     */
    @Column(name = "f_role_name",length = 100)
    private String fRoleName;

    /**
     * 角色标识
     */
    @Column(name = "f_role_key",length = 36)
    private String fRoleKey;

}
