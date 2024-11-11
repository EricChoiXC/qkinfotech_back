package com.qkinfotech.core.tendering.iso.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgPerson;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "iso_approval_handler")
@SimpleModel(url = "iso/approval/handler")
public class IsoApprovalHandler extends BaseEntity {

    /*审批文档*/
    @JoinColumn(name = "f_main_id", nullable = false)
    @ManyToOne(fetch= FetchType.LAZY)
    private IsoApproval fMain;

    /*审批人id*/
    @Column(name = "f_handler_id")
    private String fHandlerId;

    /*审批人id*/
    @JoinColumn(name = "f_handler_target")
    @ManyToOne(fetch= FetchType.LAZY)
    private OrgPerson fHandlerTarget;

    /*审批节点*/
    @Column(name = "f_node")
    private String fNode;

    /*是否已审批：0，未审批；1，已审批*/
    @Column(name = "f_status")
    private String fStatus = "0";


}
