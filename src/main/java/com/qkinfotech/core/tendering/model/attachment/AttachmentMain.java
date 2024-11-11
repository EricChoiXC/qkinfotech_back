package com.qkinfotech.core.tendering.model.attachment;

import com.qkinfotech.core.file.SysFile;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

/**
 * 附件
 */
@Getter
@Setter
@Entity
@Table(name = "attachment_main")
@SimpleModel(url = "attachment/main")
public class AttachmentMain extends BaseEntity {
    /**
     * 所属表
     */
    @Column(name = "f_model_name", length = 200)
    private String fModelName;


    /**
     * 所属记录
     */
    @Column(name = "f_model_id", length = 200)
    private String fModelId;
    /**
     * 关键字
     */
    @Column(name = "f_Key", length = 200)
    private String fKey;

    /**
     * 文件名称
     */
    @Column(name = "f_file_name", length = 200)
    private String fFileName;

    /**
     * 文件路径
     */
    @Column(name = "f_file_link", length = 500)
    private String fFileLink;


    /**
     * 文件大小
     */
    @Column(name = "f_file_size", length = 36)
    private String fFileSize;

    @ManyToOne
    @JoinColumn(name = "f_file_f_id")
    private SysFile fFile;

    @OneToMany(cascade = { CascadeType.ALL })
    @JoinColumn(name="f_attachment_id")
    private Set<AttachmentPackage> fPackages;

    /**
     * 包件iso 审批标记
     */
    @Column(name = "f_iso_flag", length = 2 )
    private Integer fIsoFlag = 0;

    /**
     * 是否归档
     */
    @Column(name = "f_documentation", length = 2)
    private Integer fDocumentation = 0;

    /**
     * 是否隐藏。用于归档后附件不可删除场景处理
     */
    @Column(name = "f_display", length = 2)
    private Integer fDisplay = 0;

    /**
     * 创建时间
     */
    @Column(name = "f_create_time")
    private Date fCreateTime;
}
