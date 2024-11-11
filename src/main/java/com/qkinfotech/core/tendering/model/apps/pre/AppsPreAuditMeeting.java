package com.qkinfotech.core.tendering.model.apps.pre;

import com.alibaba.fastjson2.JSONArray;
import com.qkinfotech.core.jpa.convertor.JSONArrayConverter;
import com.qkinfotech.core.jpa.convertor.JSONObjectConverter;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.core.tendering.model.masterModels.MasterDataMeetingType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.C;

import java.util.Date;

/**
 * 资格预审会议
 */
@Getter
@Setter
@Entity
@Table(name = "apps_pre_audit_meeting")
@SimpleModel(url = "apps/pre/audit/meeting")
public class AppsPreAuditMeeting extends BaseEntity {

    /**
     * 会议类型
     */
    @JoinColumn(name = "f_meeting_type")
    @ManyToOne
    private MasterDataMeetingType fMeetingType;

    /**
     * 会议地点
     */
    @Column(name = "f_meeting_place",length = 200)
    private String fMeetingPlace;

    /**
     * 会议开始时间
     */
    @Column(name = "f_meeting_start_time",length = 200)
    private Date fMeetingStartTime;

    /**
     * 会议结束时间
     */
    @Column(name = "f_meeting_end_time",length = 200)
    private Date fMeetingEndTime;

    /**
     * 备注
     */
    @Column(name = "f_remark",length = 2000)
    private String fRemark;

    /**
     * 关联项目id
     */
    @JoinColumn(name = "f_main_id")
    @ManyToOne
    private AppsProjectMain fMainId;

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
