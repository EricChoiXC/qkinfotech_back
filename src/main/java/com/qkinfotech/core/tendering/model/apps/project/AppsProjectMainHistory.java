package com.qkinfotech.core.tendering.model.apps.project;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "apps_project_main_history")
@SimpleModel(url = "apps/project/main/history")
public class AppsProjectMainHistory extends BaseEntity {

    /**
     * 当前处理人id
     */
    @Column(name = "f_current_processor_id", length = 36)
    private String fCurrentProcessorId;

    /**
     * 当前处理人名称
     */
    @Column(name = "f_current_processor_name", length = 50)
    private String fCurrentProcessorName;

    /**
     * 当前节点名称
     */
    @Column(name = "f_current_processor_node", length = 50)
    private String fCurrentProcessorNode;

    /**
     * 创建时间
     */
    @Column(name = "f_create_time", length = 2000)
    private Date fCreateTime;
}
