package com.qkinfotech.core.tendering.model.masterModels;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="masterdata_nature")
@SimpleModel(url = "masterdata/nature")
public class MasterDataNature extends BaseEntity {
//    f_id	id
//    f_name	性质名称
    /**
     * 性质名称
     */
    @Column(name = "f_name", length = 36)
    private String fName;

    @Column(name = "f_key", length = 36)
    private String fKey;

    @Column(name = "f_note", length = 1000)
    private String fNote;

    /**
     * 前置性质
     */
    @Column(name = "f_pre_nature", length = 2000)
    private String fPreNature;

    /**
     * 排序号
     */
    @Column(name = "f_order")
    private Integer fOrder = 0;


}
