package com.qkinfotech.core.tendering.model.apps.pre;

import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 会议包件
 */
@Getter
@Setter
@Entity
@Table(name = "apps_pre_audit_meeting_package")
@SimpleModel(url = "apps/pre/audit/meeting/package")
public class AppsPreAuditMeetingPackage extends BaseEntity {


    /**
     * 会议id
     */
    @JoinColumn(name = "f_meeting_id")
    @ManyToOne
    private AppsPreAuditMeeting fMeetingId;

    /**
     * 包件id
     */
    @JoinColumn(name = "f_package_id")
    @ManyToOne
    private AppsProjectPackage fPackageId;



}
