package com.qkinfotech.core.tendering.model.apps.collection;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 征集结果附件
 */
@Getter
@Setter
@Entity
@Table(name = "apps_collection_result_attachment")
@SimpleModel(url = "apps/collection/result/attachment")
public class AppsCollectionResultAttachment extends BaseEntity {

//    f_result_id	文档id  apps_collection_result.fId
//    f_attachmetn_id	附件id attachment.f_id
//    f_status	审批状态
    /**
     * 文档id
     */
    @JoinColumn(name = "f_result_id")
    @ManyToOne
    private AppsCollectionResult fResultId;


    /**
     * 附件id
     */
    @JoinColumn(name = "f_attachmetn_id")
    @ManyToOne
    private AttachmentMain fAttachmetnId;


    /**
     * 审批状态
     */
    @Column(name = "f_status",length = 36)
    private String f_status;






}
