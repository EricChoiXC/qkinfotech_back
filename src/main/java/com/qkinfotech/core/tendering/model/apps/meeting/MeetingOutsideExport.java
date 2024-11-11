package com.qkinfotech.core.tendering.model.apps.meeting;

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
 * 外聘专家
 */
@Getter
@Setter
@Entity
@Table(name = "meeting_outside_export")
@SimpleModel(url = "meeting/outside/export")
public class MeetingOutsideExport extends BaseEntity {
    /**
     * 会议id
     */
    @JoinColumn(name = "f_meeting_id")
    @ManyToOne
    private MeetingMain fMeetingId;

    /**
     * 专家
     */
    @JoinColumn(name = "f_element_id")
    @ManyToOne
    private OrgPerson fElementId;
}
