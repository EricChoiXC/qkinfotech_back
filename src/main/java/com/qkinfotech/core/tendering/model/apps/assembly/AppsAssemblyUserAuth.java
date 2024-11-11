package com.qkinfotech.core.tendering.model.apps.assembly;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.user.model.SysUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户个人主页设置表
 */
@Getter
@Setter
@Entity
@Table(name = "apps_assembly_user_auth")
@SimpleModel(url = "apps/assembly/user/auth")
public class AppsAssemblyUserAuth extends BaseEntity {

    /**
     * 配置人
     */
    @JoinColumn(name = "f_user_id")
    @ManyToOne(fetch=FetchType.LAZY)
    private SysUser fUser;

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
     * 排序字段
     */
    @Column(name = "f_order", length = 11)
    private Integer fOrder;

    /**
     * 是否显示
     */
    @Column(name = "f_state", length = 1)
    private boolean fState;
    /**
     * 组件名称
     */
    @Column(name = "f_assembly_name", length = 200)
    private String fAssemblyName;
}
