package com.qkinfotech.core.tendering.model.apps.notice;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 公告对应包件
 */
@Getter
@Setter
@Entity
@Table(name = "apps_notice_package")
@SimpleModel(url = "apps/notice/package")
public class AppsNoticePackage extends BaseEntity {

    /**
     * 包件id
     */
    @JoinColumn(name = "f_package_id")
    @ManyToOne
    private AppsProjectPackage fPackage;

    /**
     *  公告id
     */
    @JoinColumn(name = "f_main_id")
    @ManyToOne
    private AppsNoticeMain fMain;

}
