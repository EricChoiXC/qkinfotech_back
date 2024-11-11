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
 * 项目汇报评审外聘专家
 */
@Getter
@Setter
@Entity
@Table(name = "apps_report_outside_export")
@SimpleModel(url = "apps/report/outside/export")
public class AppsReportOutsideExport extends BaseEntity {

    /**
     * 外聘专家id
     */
    @JoinColumn(name = "f_outside_export")
    @ManyToOne
    private OrgPerson fOutSideExport;

    /**
     * 汇报评审id
     */
    @JoinColumn(name = "f_report_id")
    @ManyToOne
    private AppsReport fReportId;


}
