package com.qkinfotech.core.tendering.model.masterModels;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目模式
 */
@Getter
@Setter
@Entity
@Table(name="masterdata_mode")
@SimpleModel(url = "masterdata/mode")
public class MasterDataMode extends BaseEntity {

    /**
     * 模式名称
     */
    @Column(name = "f_name", length = 36)
    private String fName;


    @Column(name = "f_key", length = 36)
    private String fKey;




}
