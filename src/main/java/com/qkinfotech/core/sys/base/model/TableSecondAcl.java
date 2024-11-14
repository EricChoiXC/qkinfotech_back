package com.qkinfotech.core.sys.base.model;


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
@Table(name = "table_second_acl")
public class TableSecondAcl extends AccessControl{
}
