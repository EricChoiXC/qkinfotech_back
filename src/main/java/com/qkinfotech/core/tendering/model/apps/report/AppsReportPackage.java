package com.qkinfotech.core.tendering.model.apps.report;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingPackage;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目汇报评审 包件
 */
@Getter
@Setter
@Entity
@Table(name = "apps_report_package")
@SimpleModel(url = "apps/report/package")
public class AppsReportPackage extends BaseEntity {
    /**
     * 会议包件id
     */
    @JoinColumn(name = "f_meeting_package_id")
    @ManyToOne
    private MeetingPackage fMeetingPackageId;


}
