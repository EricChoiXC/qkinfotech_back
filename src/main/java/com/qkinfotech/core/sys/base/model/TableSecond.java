package com.qkinfotech.core.sys.base.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgPerson;
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
@Table(name = "table_second")
@SimpleModel(url = "/table/second")
public class TableSecond extends BaseEntity {

    @Column
    private String fName;
}
