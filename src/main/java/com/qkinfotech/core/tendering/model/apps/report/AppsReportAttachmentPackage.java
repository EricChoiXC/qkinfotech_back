package com.qkinfotech.core.tendering.model.apps.report;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingAttachmentPackage;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目汇报评审 附件包件
 */

@Getter
@Setter
@Entity
@Table(name = "apps_report_attachment_package")
@SimpleModel(url = "apps/report/attachment/package")
public class AppsReportAttachmentPackage extends BaseEntity {
    /**
     * 会议包件id
     */
    @JoinColumn(name = "f_meeting_attachment_package_id")
    @ManyToOne
    private MeetingAttachmentPackage fMeetingAttachmentPackageId;

}
