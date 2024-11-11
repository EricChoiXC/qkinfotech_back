package com.qkinfotech.core.tendering.model.apps.meeting;

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
 * 会议附件
 */
@Getter
@Setter
@Entity
@Table(name = "meeting_attachment")
@SimpleModel(url = "meeting/attachment")
public class MeetingAttachment extends BaseEntity {
    /**
     * 附件id
     */
    @JoinColumn(name = "f_attachment_id")
    @ManyToOne
    private AttachmentMain fAttachmentId;

    /**
     * 会议id
     */
    @JoinColumn(name = "f_meeting_id")
    @ManyToOne
    private MeetingMain fMeetingId;
}
