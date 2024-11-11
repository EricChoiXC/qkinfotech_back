package com.qkinfotech.core.tendering.model.apps.report;

import com.alibaba.fastjson2.JSONArray;
import com.qkinfotech.core.jpa.convertor.JSONArrayConverter;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.core.tendering.model.masterModels.MasterDataMeetingType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 项目汇报评审
 */
@Getter
@Setter
@Entity
@Table(name = "apps_report")
@SimpleModel(url = "apps/report")
public class AppsReport extends BaseEntity {
    /**
     * 文档id
     */
    @JoinColumn(name = "f_main_id")
    @ManyToOne
    private AppsProjectMain fMainId;

    /**
     * 创建时间
     */
    @Column(name = "f_create_time")
    private Date fCreateTime;

    /**
     * 是否召开汇报评审
     */
    @Column(name = "f_is_open",length = 2)
    private String fIsOpen;

    /**
     * 会议类型
     */
    @JoinColumn(name = "f_meeting_type")
    @ManyToOne
    private MasterDataMeetingType fMeetingType;

    /**
     * 备注
     */
    @Column(name = "f_remark",length = 2000)
    private String fRemark;

    /**
     * 业主专家
     */
    @Column(name = "f_owner_expert", length = 2000)
    private String fOwnerExpert;

    /**
     * 外聘专家
     */
    @Lob
    @Column(name = "f_outside_expert", length = Integer.MAX_VALUE)
    @Convert(converter = JSONArrayConverter.class)
    private JSONArray fOutsideExpert;
}
