package com.qkinfotech.core.tendering.model.apps.shortcuts;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.user.model.SysUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 快捷方式
 */
@Getter
@Setter
@Entity
@Table(name = "apps_shortcuts")
@SimpleModel(url = "apps/shortcuts")
public class AppsShortcuts  extends BaseEntity {

    /**
     * 快捷方式名称
     */
    @Column(name = "f_name", length = 500, nullable = false)
    private String fName;

    /**
     * 快捷方式打开类型（1.跳转  2.导航内容）
     */
    @Column(name = "f_type", length = 2)
    private Integer fType;

    /**
     * 快捷方式跳转地址
     */
    @Column(name = "f_url", length = 500, nullable = false)
    private String fUrl;

    /**
     * 快捷方式导航key
     */
    @Column(name = "f_page_key", length = 500)
    private String fPageKey;

    /**
     * 快捷方式图标显示url
     */
    @Column(name = "f_icon_url", length = 500, nullable = false)
    private String fIconUrl;

    /**
     * 可使用者
     */
    @OneToMany(cascade = { CascadeType.ALL },fetch=FetchType.EAGER)
    @JoinColumn(name="f_shortcuts_id")
    private List<AppsShortcutsUsed> fUsedList;

    /**
     * 快捷方式排序字段
     */
    @Column(name = "f_order", length = 11)
    private Integer fOrder;

    /**
     * 快捷方式创建人
     */
    @JoinColumn(name = "f_creator_id")
    @ManyToOne(fetch=FetchType.LAZY)
    private SysUser fCreator;

    /**
     * 创建时间
     */
    @Column(name = "f_create_time")
    private Date fCreateTime;
}
