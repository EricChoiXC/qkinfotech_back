package com.qkinfotech.core.tendering.model.apps.collection;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 征集文件附件
 */
@Getter
@Setter
@Entity
@Table(name = "apps_collection_file_attachment")
@SimpleModel(url = "apps/collection/file/attachment")
public class AppsCollectionFileAttachment extends BaseEntity {
    /**
     * 附件id
     */
    @JoinColumn(name = "f_attachment_id")
    @ManyToOne
    private AttachmentMain fAttachmentId;

    /**
     * 审批状态
     */
    @Column(name = "f_status",length = 36)
    private String fStatus;
}
