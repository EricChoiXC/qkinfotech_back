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
@Table(name="masterdata_tag")
@SimpleModel(url = "masterdata/tag")
public class MasterDataTag extends BaseEntity {
    /**
     * 标签名称
     */
    @Column(name = "f_name", length = 36)
    private String fName;

    @Column(name = "f_key", length = 36)
    private String fKey;
}
