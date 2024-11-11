package com.qkinfotech.core.tendering.model.apps.report;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgPerson;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目汇报评审业主专家
 */
@Getter
@Setter
@Entity
@Table(name = "apps_report_owner_export")
@SimpleModel(url = "apps/report/owner/export")
public class AppsReportOwnerExport extends BaseEntity {
    /**
     * 会议id
     */
    @JoinColumn(name = "f_meeting_id")
    @ManyToOne
    private AppsReport fMeetingId;


    /**
     * 专家id
     */
    @JoinColumn(name = "f_element_id")
    @ManyToOne
    private OrgPerson fElementId;



}
