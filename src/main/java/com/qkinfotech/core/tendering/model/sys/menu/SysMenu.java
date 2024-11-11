package com.qkinfotech.core.tendering.model.sys.menu;

import com.alibaba.fastjson2.JSONArray;
import com.qkinfotech.core.jpa.convertor.JSONArrayConverter;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Properties;

/**
 * 系统菜单
 */
@Getter
@Setter
@Entity
@Table(name = "sys_menu")
@SimpleModel(url = "sys/menu")
public class SysMenu extends BaseEntity {
    /**
     * 菜单JSON
     */
    @Column(name="f_menu_json",length = Integer.MAX_VALUE)
    @Convert(converter = JSONArrayConverter.class)
    private JSONArray fMenuJson;
    /**
     * 菜单类型
     */
    @Column(name = "f_menu_type", length = 255)
    private String fMenuType;

    /**
     * 菜单名称
     */
    @Column(name = "f_menu_name", length = 200)
    private String fMenuName;
}
