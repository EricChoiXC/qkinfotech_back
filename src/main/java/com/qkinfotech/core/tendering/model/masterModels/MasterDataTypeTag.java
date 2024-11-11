package com.qkinfotech.core.tendering.model.masterModels;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="master_data_type_tag")
@SimpleModel(url = "master/data/type/tag")
public class MasterDataTypeTag extends BaseEntity {

    /**
     * 类型
     */
    @JoinColumn(name = "f_masterdata_type_id")
    @ManyToOne
    private MasterDataType fMasterDataType;

    /**
     *  标签
     */
    @JoinColumn(name = "f_masterdata_tag_id")
    @ManyToOne
    private MasterDataTag fMasterDataTag;

}
