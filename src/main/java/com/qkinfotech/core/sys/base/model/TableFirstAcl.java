package com.qkinfotech.core.sys.base.model;

import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 测试用表
 */
@Getter
@Setter
@Entity
@Table(name = "table_first_acl")
@SimpleModel(url = "/table/first/acl")
public class TableFirstAcl extends AccessControl{


}
