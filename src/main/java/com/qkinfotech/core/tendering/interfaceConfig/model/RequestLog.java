package com.qkinfotech.core.tendering.interfaceConfig.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 请求日志
 */
@Getter
@Setter
@Entity
@Table(name = "request_log")
@SimpleModel(url = "request/log")
public class RequestLog extends BaseEntity {

    @Column(name = "f_url", length = 500)
    private String fUrl;

    @Column(name = "f_request", length = 20000)
    private String fRequest;

    @Column(name = "f_response", length = 20000)
    private String fResponse;

    @Column(name = "f_status", length = 20)
    private String fStatus;

    @Column(name = "f_create_time")
    private Date fCreateTime;

    @Column(name = "f_user", length = 500)
    private String fUser;
}
