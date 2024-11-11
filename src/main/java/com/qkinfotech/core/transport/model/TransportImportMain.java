package com.qkinfotech.core.transport.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.user.model.SysUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

/**
 * 导入格式
 * @author 蔡咏钦
 */
@Getter
@Setter
@Entity
@Table(name = "transport_import_main")
@SimpleModel(url = "transport/import/main")
public class TransportImportMain extends BaseEntity {

    @Column(name = "f_name")
    private String fName;

    @Column(name = "f_model_name", length = 200)
    private String fModelName;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "f_creator_id")
    private SysUser fCreator;

    @Column(name = "f_create_time")
    private Date fCreatTime;

    @OneToMany(cascade = { CascadeType.ALL })
    @JoinColumn(name="f_main_id")
    private Set<TransportImportKey> fKey;

    @OneToMany(cascade = { CascadeType.ALL })
    @JoinColumn(name="f_main_id")
    private Set<TransportImportProperties> fProperties;

}
