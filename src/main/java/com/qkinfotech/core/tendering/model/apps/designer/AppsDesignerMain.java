package com.qkinfotech.core.tendering.model.apps.designer;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 设计师信息
 */
@Getter
@Setter
@Entity
@Table(name = "apps_designer_main")
@SimpleModel(url = "apps/designer/main")
public class AppsDesignerMain extends BaseEntity {

    /**
     * 联合体公司
     */
    @Column(name = "f_company_name",length = 200)
    private String fCompanyName;

    /**
     * 设计师名称
     */
    @Column(name = "f_name",length = 200)
    private String fName;

    /**
     * 职业资格
     */
    @Column(name = "f_professional_qualification",length = 200)
    private String fProfessionalQualification;

    /**
     * 从业年限
     */
    @Column(name = "f_experience_years",length = 200)
    private String fExperienceYears;

    /**
     * 照片附件id
     */
    @JoinColumn(name = "f_photo_attachment_id")
    @ManyToOne
    private AttachmentMain fPhotoAttachmentId;

    /**
     * 创建时间
     */
    @Column(name = "f_create_time", length = 36)
    private Date fCreateTime;

    /**
     * 资格申请id
     */
    @JoinColumn(name = "f_supplier_id")
    @ManyToOne
    private AppsSupplierMain fSupplierId;

}
