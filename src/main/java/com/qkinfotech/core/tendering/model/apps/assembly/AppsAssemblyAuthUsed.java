package com.qkinfotech.core.tendering.model.apps.assembly;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgElement;
import com.qkinfotech.core.user.model.SysUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "apps_assembly_auth_used")
@SimpleModel(url = "apps/assembly/auth/used")
public class AppsAssemblyAuthUsed extends BaseEntity {

    @JoinColumn(name = "f_used_id")
    @ManyToOne(fetch=FetchType.LAZY)
    private OrgElement fUsed;

}
