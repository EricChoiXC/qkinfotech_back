package com.qkinfotech.core.org.model;

import java.util.Date;

import com.qkinfotech.core.mvc.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 员工所属
 *
 */
@Getter
@Setter
@Entity
@Table(name="org_person_detail")
public class OrgPersonDetail extends BaseEntity {
	
	@JoinColumn(name = "f_person_id", nullable = false)
	@ManyToOne(fetch=FetchType.LAZY)
	private OrgPerson fPerson;
	
	@JoinColumn(name = "f_dept_id")
	@ManyToOne(fetch=FetchType.LAZY)
	private OrgDept fDept;
	
	@JoinColumn(name = "f_company_id")
	@ManyToOne(fetch=FetchType.LAZY)
	private OrgCompany fCompany;

	@JoinColumn(name = "f_post_id")
	@ManyToOne(fetch=FetchType.LAZY)
	private OrgPost   fPost;
	
	@JoinColumn(name="f_title_id")
	@ManyToOne(fetch=FetchType.LAZY)
	private OrgTitle  fTitle;	
	
	@Column(name = "f_ratio", nullable = false)
	private int   fRatio;
	
	@Column(name = "f_start_date", nullable = false)
	private Date      fStartDate;

	@Column(name = "f_end_date", nullable = false)
	private Date      fEndDate;

	@OneToOne
	@PrimaryKeyJoinColumn
	private OrgElement baseInfo;
}
