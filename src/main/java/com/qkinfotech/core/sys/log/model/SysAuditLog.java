package com.qkinfotech.core.sys.log.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgPerson;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@SimpleModel(url = "/sys/audit/log")
@Table(name = "sys_audit_log")
@Entity
public class SysAuditLog extends BaseEntity {

    @Column(length = 200)
    private String fModelId;

    @Column(length = 200)
    private String fModelName;

    @Column
    private String fOperation;

    @JoinColumn(name = "f_person_id")
    @ManyToOne(fetch= FetchType.LAZY)
    private OrgPerson fPerson;

    @Column
    private Date fCreateTime;

    @Column(length = 20000)
    private String fAuditMessage;

}
