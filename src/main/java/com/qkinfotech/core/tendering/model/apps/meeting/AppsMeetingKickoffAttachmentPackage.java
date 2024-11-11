package com.qkinfotech.core.tendering.model.apps.meeting;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目启动会 附件包件
 */
@Getter
@Setter
@Entity
@Table(name = "apps_meeting_kickoff_attachment_package")
@SimpleModel(url = "apps/meeting/kickoff/attachment/package")
public class AppsMeetingKickoffAttachmentPackage extends BaseEntity {
    /**
     * 附件包件id
     */
    @JoinColumn(name = "f_meeting_attachment_package_id")
    @ManyToOne
    private MeetingAttachmentPackage fMeetAttachmentPackageId;
}
