package com.qkinfotech.core.sys.base.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgElement;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/***
 * 权限功能基类
 */
@MappedSuperclass
public class AccessControl extends BaseEntity {

    /**
     * 相关权限用户
     */
    @JoinColumn(name = "f_org_id")
    @ManyToOne(fetch= FetchType.LAZY)
    private OrgElement fOrg;

    /**
     * 所属权限文档id
     */
    @Column
    private String fDocId;

    /**
     * 权限来源
     */
    @Column
    private String fKey;

    /**
     * 所属权限附件
     */
    @JoinColumn(name = "f_attachment_id")
    @ManyToOne(fetch= FetchType.LAZY)
    private AttachmentMain fAttachment;

    /**
     * 权限
     */
    @Column
    private String fAuth;

    public OrgElement getfOrg() {
        return fOrg;
    }

    public void setfOrg(OrgElement fOrg) {
        this.fOrg = fOrg;
    }

    public String getfDocId() {
        return fDocId;
    }

    public void setfDocId(String fDocId) {
        this.fDocId = fDocId;
    }

    public String getfKey() {
        return fKey;
    }

    public void setfKey(String fKey) {
        this.fKey = fKey;
    }

    public AttachmentMain getfAttachment() {
        return fAttachment;
    }

    public void setfAttachment(AttachmentMain fAttachment) {
        this.fAttachment = fAttachment;
    }

    public String getfAuth() {
        return fAuth;
    }

    public void setfAuth(String fAuth) {
        this.fAuth = fAuth;
    }
}
