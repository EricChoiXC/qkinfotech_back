package com.qkinfotech.core.tendering.model.apps.project;


import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.user.model.SysUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 项目会议通知表
 */
@Getter
@Setter
@Entity
@Table(name = "apps_project_meeting_notice")
@SimpleModel(url = "apps/project/meeting/notice")
public class AppsProjectMeetingNotice  extends BaseEntity {

    /**
     * 会议提示消息
     */
    @Column(name = "f_message", length = 200)
    private String fMessage;

    /**
     * 关联项目
     */
    @JoinColumn(name = "f_project_id")
    @ManyToOne(fetch= FetchType.LAZY)
    private AppsProjectMain fProject;

    /**
     * 关联人员
     */
    @JoinColumn(name = "f_user_id")
    @ManyToOne(fetch= FetchType.LAZY)
    private OrgPerson fUser;

    /**
     * 会议时间(YYYY-MM-dd)
     */
    @Column(name = "f_meeting_date", length = 50)
    private String fMeetingDate;

    /**
     * 创建时间
     */
    @Column(name = "f_create_time")
    private Date fCreateTime;
}
