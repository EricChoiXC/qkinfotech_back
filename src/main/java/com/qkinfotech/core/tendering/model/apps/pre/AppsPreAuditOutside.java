package com.qkinfotech.core.tendering.model.apps.pre;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 资格预审会议对应外聘专家
 */
@Getter
@Setter
@Entity
@Table(name = "apps_pre_audit_outside")
@SimpleModel(url = "apps/pre/audit/outside")
public class AppsPreAuditOutside extends BaseEntity {

    /**
     * 会议id
     */
    @JoinColumn(name = "f_meeting_id")
    @ManyToOne
    private AppsPreAuditMeeting fMeetingId;

    /**
     * 内部专家id（组织机构）
     * todo
     */
    @Column(name = "f_internal_expert_id",length = 36)
    private String fInternalExpertId;
}
