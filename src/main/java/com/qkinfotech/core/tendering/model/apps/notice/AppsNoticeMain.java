package com.qkinfotech.core.tendering.model.apps.notice;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
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
@Table(name = "apps_notice_main")
@SimpleModel(url = "apps/notice/main")
public class AppsNoticeMain extends BaseEntity {
    /**
     * 是否资格预审
     */
    @Column(name = "f_is_prequalification", length = 10)
    private String fIsPrequalification;

    /**
     * 是否接受联合体
     */
    @Column(name = "f_is_union", length = 10)
    private String fIsUnion;

    /**
     * 是否定向邀请
     */
    @Column(name = "f_is_invite_shortlists", length = 10)
    private String fIsInviteShortlists;

    /**
     * 是否模糊匹配
     */
    @Column(name = "f_is_accurate_matching", length = 10)
    private String fIsAccurateMatching;

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
    @Column(name = "f_announcement_published_date")
    private Date fAnnouncementPublishedDate;


    /**
     * 标书获取开启日期（领购开始）
     */
    @Column(name = "f_tender_acquisition_start_date")
    private Date fTenderAcquisitionStartDate;


    /**
     * 标书获取结束日期（领购结束）
     */
    @Column(name = "f_tender_acquisition_end_date")
    private Date fTenderAcquisitionEndDate;

    /**
     * 获取标书单位
     */
    @Column(name = "f_obtain_bidding_unit", length = 200)
    private String f_obtain_bidding_unit;

    /**
     * 申请文件名称
     */
    @Column(name = "f_application_document_name", length = 200)
    private String fApplicationDocumentName;


    /**
     * 申请文件链接
     */
    @Column(name = "f_application_document_url", length = 200)
    private String fApplicationDocumentUrl;

    /**
     *申请文件截至日期(上传截至日期)
     */
    @Column(name = "f_application_document_deadline")
    private Date fApplicationDocumentDeadline;

    /**
     * 开标时间
     */
    @Column(name = "f_open_time")
    private Date fOpenTime;


    /**
     * 售价
     */
    @Column(name = "f_price", length = 200)
    private String fPrice;

    /**
     *  售卖方式 0-整包售卖 1-分包售卖
     */
    @Column(name = "f_quote_way", length = 200)
    private String fQuoteWay;


    /**
     * 创建时间
     */
    @Column(name = "f_create_time")
    private Date fCreateTime;

    /**
     * 项目id
     */
    @JoinColumn(name = "f_project_id")
    @ManyToOne
    private AppsProjectMain fProjectId;

    /**
     * ekp公告fdid
     */
    @Column(name = "f_ekp_id", length = 60)
    private String fEkpId;
}
