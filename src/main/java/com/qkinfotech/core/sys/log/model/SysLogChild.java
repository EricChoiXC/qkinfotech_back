package com.qkinfotech.core.sys.log.model;

import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name="sys_log_child")
@SimpleModel(url = "/sys/log/child")
public class SysLogChild extends SysLogMain{

    @Column
    private String fChild;

    @Column
    private Date fDate;
}
