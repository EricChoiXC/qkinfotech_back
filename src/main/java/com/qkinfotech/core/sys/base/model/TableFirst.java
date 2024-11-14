package com.qkinfotech.core.sys.base.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * 测试用表
 */
@Getter
@Setter
@Entity
@Table(name = "table_first")
@SimpleModel(url = "/table/first")
public class TableFirst extends BaseEntity{

    @Column
    private String fName;
}
