package com.qkinfotech.core.tendering.model.masterModels;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目规模
 */
@Getter
@Setter
@Entity
@Table(name="masterdata_scale")
@SimpleModel(url = "masterdata/scale")
public class MasterDataScale extends BaseEntity {

    /**
     * 规模名称
     */
    @Column(name = "f_name", length = 36)
    private String fName;

    /**
     * key
     */
    @Column(name = "f_key", length = 36)
    private String fKey;

    /**
     * 类型分组
     */
    @Column(name = "f_group", length = 200)
    private String fGroup;

}
