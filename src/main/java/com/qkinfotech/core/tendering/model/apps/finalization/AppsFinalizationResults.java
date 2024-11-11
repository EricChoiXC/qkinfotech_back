package com.qkinfotech.core.tendering.model.apps.finalization;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 入围名单公告记录
 */
@Getter
@Setter
@Entity
@Table(name = "apps_finalization_results")
@SimpleModel(url = "apps/finalization/results")
public class AppsFinalizationResults extends BaseEntity {

    /**
     * 入围公告名称
     */
    @Column(name = "f_finalist_announcement_name",length = 200)
    private String  fFinalistAnnouncementName;

    /**
     * 入围公告链接
     */
    @Column(name = "f_finalist_announcement_url",length = 2000)
    private String  fFinalistAnnouncementUrl;

    /**
     * 项目id
     */
    @JoinColumn(name = "f_project_id")
    @ManyToOne
    private AppsProjectMain fProject;

    /**
     * 创建时间
     */
    @Column(name = "f_create_time",length = 200)
    private Date fCreateTime;
}
