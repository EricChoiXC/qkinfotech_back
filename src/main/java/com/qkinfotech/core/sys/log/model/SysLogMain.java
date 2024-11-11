package com.qkinfotech.core.sys.log.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="sys_log_main")
@SimpleModel(url = "/sys/log/main")
@Inheritance(strategy = InheritanceType.JOINED)
public class SysLogMain extends BaseEntity {

    @Column
    private String fMain;
}
