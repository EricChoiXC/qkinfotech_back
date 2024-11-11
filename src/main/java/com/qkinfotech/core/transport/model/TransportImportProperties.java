package com.qkinfotech.core.transport.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 导入格式字段
 * @author 蔡咏钦
 */
@Getter
@Setter
@Entity
@Table(name = "transport_import_properties")
@SimpleModel(url = "transport/import/properties")
public class TransportImportProperties extends BaseEntity {

    @Column(name = "f_name")
    private String fName;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "f_main_id")
    private TransportImportMain fMain;

    @Column(name = "f_order")
    private Integer fOrder;

    @OneToMany(cascade = { CascadeType.ALL })
    @JoinColumn(name = "f_property_id")
    private List<TransportImportPropertyKey> fKeys;
}
