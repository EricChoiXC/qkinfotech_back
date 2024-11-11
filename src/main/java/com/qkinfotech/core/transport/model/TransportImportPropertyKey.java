package com.qkinfotech.core.transport.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 导入对象字段的关键字
 */
@Getter
@Setter
@Entity
@Table(name = "transport_import_property_key")
@SimpleModel(url = "transport/import/property/key")
public class TransportImportPropertyKey extends BaseEntity {

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "f_property_id")
    private TransportImportProperties fProperty;

    @Column
    private String fKey;
}
