package com.qkinfotech.core.tendering.iso.model;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.jpa.convertor.JSONArrayConverter;
import com.qkinfotech.core.jpa.convertor.JSONObjectConverter;
import com.qkinfotech.core.jpa.convertor.PropertyConverter;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "iso_approval")
@SimpleModel(url = "iso/approval")
public class IsoApproval extends BaseEntity {

    //文档状态
    @Column(name = "f_status", nullable = false)
    private String fStatus = "10";

    //文档标号
    @Column(name = "f_no")
    private String fNo;

    //版本号
    @Column(name = "f_version", nullable = false)
    private String fVersion;

    //创建人
    @JoinColumn(name = "f_creator_id", nullable = false)
    @ManyToOne(fetch= FetchType.LAZY)
    private OrgPerson fCreator;

    @Column(name = "f_creator_name")
    private String fCreatorName;

    //创建时间
    @Column(name = "f_create_time", nullable = false)
    private Date fCreateTime;

    //完成审批时间
    @Column(name = "f_finish_time")
    private Date fFinishTime;

    //成果产品名称-标题
    @Column(name = "f_subject", nullable = false)
    private String fSubject;

    //项目id
    @Column(name = "f_project_id")
    private String fProjectId;

    //项目编号
    @Column(name = "f_project_no")
    private String fProjectNo;

    //项目名称
    @Column(name = "f_project_name")
    private String fProjectName;

    //成果产品名称
    @Column(name = "f_achievement_name")
    private String fAchievementName;

    //招标文本模板
    @Column(name = "f_model_name")
    private String fModelName;

    //成果产品编制人
    @JoinColumn(name = "f_achievement_prepared_id")
    @ManyToOne(fetch= FetchType.LAZY)
    private OrgPerson fAchievementPrepared;

    @Column(name = "f_achievement_prepared_name")
    private String fAchievementPreparedName;

    //成果语言
    @Column(name = "f_achievement_language", length = 200)
    private String fAchievementLanguage;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "iso_approval_attachment",  joinColumns = @JoinColumn(name = "f_iso_id", referencedColumnName = "f_id"),
            inverseJoinColumns = @JoinColumn(name = "f_attachment_id", referencedColumnName = "f_id"),
            uniqueConstraints = {
                    @UniqueConstraint(name = "unique_iso_attachment", columnNames = {"f_iso_id", "f_attachment_id"})
            },
            indexes = {
                    @Index(name = "idx_iso_approval_attachment_iso", columnList = "f_iso_id"),
                    @Index(name = "idx_iso_approval_attachment_attachment", columnList = "f_attachment_id")
            })
    private Set<AttachmentMain> fAttachments;

    //备注
    @Column(name = "f_notes")
    private String fNotes;

    //ekp流程Id
    @Column(name = "f_ekp_id")
    private String fEkpId;

    //自检情况
    @Column(name = "f_first_check", length = 2000)
    @Convert(converter = JSONObjectConverter.class)
    private JSONObject fFirstCheck;

    //复检
    @Column(name = "f_re_check", length = 2000)
    @Convert(converter = JSONObjectConverter.class)
    private JSONObject fReCheck;

    //审检
    @Column(name = "f_approval", length = 2000)
    @Convert(converter = JSONObjectConverter.class)
    private JSONObject fApproval;

    //相关部门审批
    @Column(name = "f_dept_approval", length = 2000)
    @Convert(converter = JSONArrayConverter.class)
    private JSONArray fDeptApproval;

    //终审
    @Column(name = "f_final_approval", length = 2000)
    @Convert(converter = JSONObjectConverter.class)
    private JSONObject fFinalApproval;


    @OneToMany(cascade = { CascadeType.ALL })
    @JoinColumn(name="f_main_id")
    private Set<IsoApprovalHandler> fHandlers;

}
