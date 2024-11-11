package com.qkinfotech.core.tendering.model.apps.designer;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 曾获国内或者国际设计奖项
 */
@Getter
@Setter
@Entity
@Table(name = "apps_awards")
@SimpleModel(url = "apps/awards")
public class AppsDesignerAwards extends BaseEntity {
    /**
     * 项目奖项
     */
    @Column(name = "f_project_awards",length = 200)
    private String fProjectAwards;

    /**
     * 获奖年份
     */
    @Column(name = "f_award_year",length = 200)
    private String fAwardYear;

    /**
     * 项目简称
     */
    @Column(name = "f_project_abbreviation",length = 200)
    private String fProjectAbbreviation;

    /**
     * 设计师id
     */
    @JoinColumn(name = "f_designer_id")
    @ManyToOne
    private AppsDesignerMain fDesignerId;

    /**
     * 创建时间
     */
    @Column(name = "f_create_time",length = 200)
    private Date fCreateTime;




}
