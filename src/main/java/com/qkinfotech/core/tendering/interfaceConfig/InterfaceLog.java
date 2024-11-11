package com.qkinfotech.core.tendering.interfaceConfig;

import com.qkinfotech.core.mvc.BaseEntity;
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
@Table(name = "interface_log")
@SimpleModel(url = "interface/log")
public class InterfaceLog extends BaseEntity {

    /**
     * 创建时间
     */
    @Column(name = "f_create_time")
    private Date fCreateTime;

    /**
     * 接口名称
     */
    @Column(name = "f_interface_name",length = 2000)
    private String fInterfaceName;

    /**
     * 接口地址
     */
    @Column(name = "f_interface_url",length = 2000)
    private String fInterfaceUrl;

    /**
     * 接口返回信息
     */
    @Column(name = "f_interface_info",length = 20000,columnDefinition = "TEXT")
    private String fInterfaceInfo;

    /**
     * 项目编号
     */
    @Column(name = "f_protocol_no",length = 200)
    private String fProtocolNo;

    /**
     * 入参input parameter
     */
    @Column(name = "f_input_parameter",length = 20000,columnDefinition = "TEXT")
    private String fInputParameter;

    /**
     * 状态
     * 1.成功
     * 2.失败
     */
    @Column(name = "f_interface_status",length = 36)
    private String fInterfaceStatus;


}
