package com.qkinfotech.core.tendering.model.masterModels;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目类别
 */
@Getter
@Setter
@Entity
@Table(name="masterdata_category")
@SimpleModel(url = "masterdata/category")
public class MasterDataCategory extends BaseEntity {
    /**
     * 名称
     */
    @Column(name = "f_name", length = 200)
    private String fName;
    /**
     * 编号
     */
    @Column(name = "f_number", length = 36)
    private String fNumber;



    @Column(name = "f_key", length = 36)
    private String fKey;
}
