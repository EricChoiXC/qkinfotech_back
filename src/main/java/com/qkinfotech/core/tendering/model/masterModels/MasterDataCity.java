package com.qkinfotech.core.tendering.model.masterModels;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.sys.log.model.SysAuditModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="masterdata_city")
@SimpleModel(url = "masterdata/city")
@SysAuditModel(modelName = "masterDataCity")
public class MasterDataCity extends BaseEntity {

    /**
     * 城市
     */
    @Column(name = "f_name", length = 36)
    private String fName;
    /**
     * 所属国家
     */
    @JoinColumn(name = "f_parent_id")
    @ManyToOne
    private MasterDataCountry fParentId;

    /**
     * key
     */
    @Column(name = "f_key", length = 36)
    private String fKey;

    /**
     * 城市编码
     */
    @Column(name = "f_city_Num", length = 36)
    private String fCityNum;




}
