package com.qkinfotech.core.transport.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 导入格式关键字
 * @author 蔡咏钦
 */
@Getter
@Setter
@Entity
@Table(name = "transport_import_key")
@SimpleModel(url = "transport/import/key")
public class TransportImportKey extends BaseEntity {

    @Column(name = "f_name")
    private String fName;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "f_main_id")
    private TransportImportMain fMain;

}
