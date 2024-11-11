package com.qkinfotech.core.tendering.master.model;

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

/**
 * 文本模板
 */
@Getter
@Setter
@Entity
@Table(name = "master_text_template")
@SimpleModel(url = "master/text/template")
public class MasterTextTemplate extends BaseEntity {

    /**
     * 模板名称
     */
    @Column(name = "f_name", length = 200)
    private String fName;

    /**
     * 关键字
     */
    @Column(name = "f_key", length = 200)
    private String fKey;

    /**
     * 模板内容
     */
    @Column(name = "f_content", length = 10000)
    @Convert(converter = JSONArrayConverter.class)
    private JSONArray fContent;

}
