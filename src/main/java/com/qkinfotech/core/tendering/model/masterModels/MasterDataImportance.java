package com.qkinfotech.core.tendering.model.masterModels;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.sys.log.model.SysAuditModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 重要性
 */
@Getter
@Setter
@Entity
@Table(name="masterdata_importance")
@SimpleModel(url = "masterdata/importance")
@SysAuditModel
public class MasterDataImportance extends BaseEntity {
    /**
     * 性质名称
     */
    @Column(name = "f_name", length = 36)
    private String fName;

    @Column(name = "f_key", length = 36)
    private String fKey;

//    @JoinColumn(name = "f_creator")
//    @ManyToOne
//    private OrgPerson fCreator;

    @Column(name = "f_create_time", length = 36)
    private Date fCreateTime;

    @Column(name = "f_update_time", length = 36)
    private Date fUpdateTime;
}
