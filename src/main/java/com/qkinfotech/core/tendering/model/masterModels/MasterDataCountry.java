package com.qkinfotech.core.tendering.model.masterModels;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name="masterdata_country")
@SimpleModel(url = "masterdata/country")
public class MasterDataCountry extends BaseEntity {

    /**
     * 国家名称
     */
    @Column(name = "f_name", length = 200)
    private String fName;

    /**
     * key
     */
    @Column(name = "f_key", length = 36)
    private String fKey;

    /**
     * 国家编码
     */
    @Column(name = "f_country_Num", length = 36)
    private String fCountryNum;


}
