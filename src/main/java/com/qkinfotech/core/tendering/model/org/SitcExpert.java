package com.qkinfotech.core.tendering.model.org;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 专家
 */
@Getter
@Setter
@Entity
@Table(name = "sitc_expert")
@SimpleModel(url = "sitc/expert")
public class SitcExpert extends BaseEntity {

    @Column(name = "f_org_id", length = 200)
    private String fOrgId;

    private String fName;

    private String fSex;

    private String fPapersType;

    private String fPapersNumber;

    private String fGraduateSchool;

    private String fMajor;

    private String fEducation;

    private String fDegree;

    private String fWorkUnit;

    private String fOccupation;

    private String fEmail;

    private String fWexin;

    private String fPostal;

    private String fHomePhone;

    private String fPostalCode;

    private String fInstancy;

    private String fPresence;

    private String fIssuingBank;

    private String fBankNumber;

    private String fIsExpert;

    private String fBuileType;

    private String fWorking;

    @Column(name = "f_experience", length = 3000)
    private String fExperience;

    private String fAttication;

    private Date fBirthdate;

    private Integer fAge;

    private String fSenior;

    private Boolean fIsEme = Boolean.FALSE;

    private String fNumber;

    private String fOriginalId;

    private Double fRemarks;

    private String fPersonId;

    private String fDentify = "0";

    private String fOpenId;

    private String fNickName;

    private String fSeat;



}
