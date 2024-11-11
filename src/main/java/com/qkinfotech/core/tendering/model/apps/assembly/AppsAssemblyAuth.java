package com.qkinfotech.core.tendering.model.apps.assembly;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.user.model.SysUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "apps_assembly_auth")
@SimpleModel(url = "apps/assembly/auth")
public class AppsAssemblyAuth extends BaseEntity {

    /**
     * 组件名称
     */
    @Column(name = "f_name", length = 200, nullable = false)
    private String fName;

    /**
     * 组件key
     */
    @Column(name = "f_component_key", length = 200, nullable = false)
    private String fComponentKey;

    /**
     * 组件数据key
     */
    @Column(name = "f_assembly_component_data_key", length = 200, nullable = false)
    private String fAssemblyComponentDataKey;

    /**
     * 可使用者
     */
    @OneToMany(cascade = { CascadeType.ALL },fetch=FetchType.EAGER)
    @JoinColumn(name="f_assembly_id")
    private List<AppsAssemblyAuthUsed> fUsedList;

    /**
     * 排序字段
     */
    @Column(name = "f_order", length = 11)
    private Integer fOrder;

    /**
     * 创建时间
     */
    @Column(name = "f_create_time")
    private Date fCreateTime;
}
