package com.qkinfotech.core.tendering.model.apps.supplier;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeMain;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 供应商 资格申请
 */

@Getter
@Setter
@Entity
@Table(name = "apps_supplier_main")
    @SimpleModel(url = "apps/supplier/main")
public class AppsSupplierMain extends BaseEntity {

    /**
     * 是否联合体
     */
    @Column(name = "f_is_union")
    private boolean fIsUnion;


    /**
     * 创建时间
     */
    @Column(name = "f_create_time")
    private Date fCreateTime;

    /**
     * 当前状态（0/1/2）
     */
    @Column(name = "f_current_status",length = 2)
    private int fCurrentStatus;

    /**
     * 是否有效/是否合作结束
     */
    @Column(name = "f_effectiveness_or_finish_cooperation",length = 2)
    private String fEffectivenessOrFinishCooperation;

    /**
     * 所属供应商账号
     */
    @JoinColumn(name = "f_supplier_id")
    @ManyToOne
    private OrgPerson fSupplier;

    /**
     * 项目id
     */
    @JoinColumn(name = "f_project_id")
    @ManyToOne
    private AppsProjectMain fProjectId;

    /**
     *  公告id
     */
    @JoinColumn(name = "f_notice_id")
    @ManyToOne
    private AppsNoticeMain fNotice;

    /**
     * 递交时间
     */
    @Column(name = "f_sub_time")
    private Date fSubTime;

    /**
     * 递交ip
     */
    @Column(name = "f_ip")
    private String fIp;

}
