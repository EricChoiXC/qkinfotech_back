package com.qkinfotech.core.tendering.model.attachment;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 附件包件
 */
@Getter
@Setter
@Entity
@Table(name = "attachment_package")
@SimpleModel(url = "attachment/package")
public class AttachmentPackage extends BaseEntity {

    /**
     * 附件id
     */
    @JoinColumn(name = "f_attachment_id")
    @ManyToOne
    private AttachmentMain fAttachmentId;


    /**
     * 所属包件
     */
    @JoinColumn(name = "f_package_id")
    @ManyToOne
    private AppsProjectPackage fPackageId;


    /**
     * 包件iso 审批标记
     */
    @Column(name = "f_iso_flag", length = 2 )
    private Integer fIsoFlag = 0;

}
