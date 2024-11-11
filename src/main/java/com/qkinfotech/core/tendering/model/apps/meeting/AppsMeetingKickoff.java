package com.qkinfotech.core.tendering.model.apps.meeting;
//f_main_id	文档id
//f_create_time	创建时间
//f_is_open	是否召开

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 项目启动会
 */
@Getter
@Setter
@Entity
@Table(name = "apps_meeting_kickoff")
@SimpleModel(url = "apps/meeting/kickoff")
public class AppsMeetingKickoff extends BaseEntity {
    /**
     * 文档id
     */
    @JoinColumn(name = "f_main_id")
    @ManyToOne
    private AppsProjectMain fMainId;

    /**
     * 创建时间
     */
    @Column(name = "f_create_time",length = 200)
    private Date fCreateTime;

    /**
     * 是否召开
     */
    @Column(name="f_is_open",length = 2)
    private String fIsOpen;
}
