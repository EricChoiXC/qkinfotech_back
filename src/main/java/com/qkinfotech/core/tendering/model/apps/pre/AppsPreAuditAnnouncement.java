package com.qkinfotech.core.tendering.model.apps.pre;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 资格预审公告
 */
@Getter
@Setter
@Entity
@Table(name = "apps_pre_audit_announcement")
@SimpleModel(url = "apps/pre/audit/announcement")
public class AppsPreAuditAnnouncement extends BaseEntity {
    /**
     * 是够资格预审
     */
    @Column(name = "f_is_prequalification", length = 2)
    private String fIsPrequalification;

    /**
     * 是否邀请入围
     */
    @Column(name = "f_is_invite_shortlists", length = 2)
    private String fIsInviteShortlists;

    /**
     * 公告标题
     */
    @Column(name = "f_announcement_title", length = 2000)
    private String fAnnouncementTitle;

    /**
     * 公告链接
     */
    @Column(name = "f_announcement_url", length = 200)
    private String fAnnouncementUrl;

    /**
     * 公告发布日期
     */
    @Column(name = "f_announcement_published_date", length = 200)
    private Date fAnnouncementPublishedDate;


    /**
     * 标书获取开启日期
     */
    @Column(name = "f_tender_acquisition_start_date", length = 200)
    private Date fTenderAcquisitionStartDate;


    /**
     * 标书获取结束日期
     * 获取标书单位
     */
    @Column(name = "f_tender_acquisition_end_date", length = 200)
    private Date fTenderAcquisitionEndDate;

    /**
     * 获取标书单位
     */
    @Column(name = "f_obtain_bidding_unit", length = 200)
    private String f_obtain_bidding_unit;

    /**
     *申请文件名称
     */
    @Column(name = "f_application_document_name", length = 200)
    private String fApplicationDocumentName;


    /**
     *申请文件链接
     */
    @Column(name = "f_application_document_url", length = 200)
    private String fApplicationDocumentUrl;


    /**
     *申请文件截至日期
     */
    @Column(name = "f_application_document_deadline", length = 200)
    private Date fApplicationDocumentDeadline;


    /**
     *资格预审id
     */
    @JoinColumn(name = "f_prequalification_id")
    @ManyToOne
    private AppsPreAuditMain fPrequalificationId;
}
