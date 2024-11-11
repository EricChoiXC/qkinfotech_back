package com.qkinfotech.core.tendering.model.apps.collection;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingPackage;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 征集结果包件
 */
@Getter
@Setter
@Entity
@Table(name = "apps_collection_result_package")
@SimpleModel(url = "apps/collection/result/package")
public class AppsCollectionResultPackage extends BaseEntity {
    /**
     * 征集结果细节
     */
    @JoinColumn(name = "f_apps_collection_result_package")
    @ManyToOne
    private AppsCollectionResultDetail appsCollectionResultDetail;
    /**
     * 包件id
     */
    @JoinColumn(name = "f_apps_project_package")
    @ManyToOne
    private AppsProjectPackage appsProjectPackage;
}
