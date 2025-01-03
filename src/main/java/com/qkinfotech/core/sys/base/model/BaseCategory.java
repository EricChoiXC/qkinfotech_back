package com.qkinfotech.core.sys.base.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.org.model.OrgElement;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.util.Date;

/**
 * 基础分类
 */
@MappedSuperclass
public class BaseCategory extends BaseEntity {

    /*类名*/
    @Column
    private String fName;

    /*类型值*/
    @Column
    private String fType;

    /*创建时间*/
    @Column
    private Date fCreateTime;

    /*修改时间*/
    @Column
    private Date fAlterTime;

    /*创建人*/
    @JoinColumn(name = "f_creator_id")
    @ManyToOne
    private OrgElement fCreator;

    /*有效性*/
    @Column
    private Boolean fAvailable = true;

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getfType() {
        return fType;
    }

    public void setfType(String fType) {
        this.fType = fType;
    }

    public Date getfCreateTime() {
        return fCreateTime;
    }

    public void setfCreateTime(Date fCreateTime) {
        this.fCreateTime = fCreateTime;
    }

    public Date getfAlterTime() {
        return fAlterTime;
    }

    public void setfAlterTime(Date fAlterTime) {
        this.fAlterTime = fAlterTime;
    }

    public OrgElement getfCreator() {
        return fCreator;
    }

    public void setfCreator(OrgElement fCreator) {
        this.fCreator = fCreator;
    }

    public Boolean getfAvailable() {
        return fAvailable;
    }

    public void setfAvailable(Boolean fAvailable) {
        this.fAvailable = fAvailable;
    }
}
