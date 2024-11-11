package com.qkinfotech.core.tendering.model.apps.pre;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 资格预审会议对应附件
 */
@Getter
@Setter
@Entity
@Table(name = "apps_pre_audit_att")
@SimpleModel(url = "apps/pre/audit/att")
public class AppsPreAuditAtt extends BaseEntity {
    /**
     * 会议id
     */
    @JoinColumn(name = "f_meeting_id")
    @ManyToOne
    private AppsPreAuditMeeting fMeetingId;


    /**
     * 附件id
     */
    @JoinColumn(name = "f_attachment_id")
    @ManyToOne
    private AttachmentMain fAttachmentId;


}
