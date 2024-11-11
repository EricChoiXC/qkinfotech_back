package com.qkinfotech.core.tendering.model.apps.finalization;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeMain;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 入围公告对应包件
 */
@Getter
@Setter
@Entity
@Table(name = "apps_finalization_results_notice_package")
@SimpleModel(url = "apps/finalization/results/notice/package")
public class AppsFinalizationResultsNoticePackage extends BaseEntity {

    /**
     * 包件id
     */
    @JoinColumn(name = "f_package_id")
    @ManyToOne
    private AppsProjectPackage fPackage;

    /**
     *  入围公告id
     */
    @JoinColumn(name = "f_results_main_id")
    @ManyToOne
    private AppsFinalizationResults fResultsMain;

}
