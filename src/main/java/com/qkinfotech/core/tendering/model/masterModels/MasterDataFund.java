package com.qkinfotech.core.tendering.model.masterModels;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 资金来源
 */
@Getter
@Setter
@Entity
@Table(name="masterdata_fund")
@SimpleModel(url = "masterdata/fund")
public class MasterDataFund extends BaseEntity {
    /**
     * 资金来源
     */
    @Column(name = "f_name", length = 36)
    private String fName;

    /**
     * key
     */
    @Column(name = "f_key", length = 36)
    private String fKey;
}
