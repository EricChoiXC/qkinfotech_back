package com.qkinfotech.core.tendering.model.apps.meeting;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 会议附件包件
 */
@Getter
@Setter
@Entity
@Table(name = "meeting_attachment_package")
@SimpleModel(url = "meeting/attachment/package")
public class MeetingAttachmentPackage extends BaseEntity {

    /**
     * 附件id
     */
    @JoinColumn(name = "f_attachment_id")
    @ManyToOne
    private AttachmentMain fAttachmentId;



    /**
     * 包件id
     */
    @JoinColumn(name = "f_package_id")
    @ManyToOne
    private AppsProjectPackage fProjectId;}
