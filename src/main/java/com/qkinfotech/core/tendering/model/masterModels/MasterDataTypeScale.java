package com.qkinfotech.core.tendering.model.masterModels;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="masterdata_type_scale")
@SimpleModel(url = "masterdata/typeScale")
public class MasterDataTypeScale extends BaseEntity {
    /**
     * 类型
     */
    @JoinColumn(name = "f_masterdata_type_id")
    @ManyToOne
    private MasterDataType fMasterDataTypeId;

    /**
     *  规模
     */
    @JoinColumn(name = "f_masterdata_scale_id")
    @ManyToOne
    private MasterDataScale fMasterDataScaleId;


    @Column(name = "f_key", length = 36)
    private String fKey;

}
