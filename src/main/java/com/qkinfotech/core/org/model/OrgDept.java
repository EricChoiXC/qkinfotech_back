package com.qkinfotech.core.org.model;

import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.mvc.models.ITreeEntity;
import com.qkinfotech.util.StringUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 机构数树
 */

@Getter
@Setter
@Entity
@Table(name="org_dept")
@SimpleModel(url = "org/dept")
public class OrgDept extends OrgElement implements  ITreeEntity<OrgDept> {

	public OrgDept() {
		//this.baseInfo = new OrgElement();
	}
	
	public OrgDept(String fId) {
		//this.baseInfo = new OrgElement();
		if (StringUtil.isNotNull(fId)) {
			setfId(fId);
		} else {
			super.getfId();
		}
	}
	
	@JoinColumn(name = "f_company_id")
	@ManyToOne(fetch=FetchType.LAZY)
	private OrgCompany fCompany;

	@Column(name = "f_tel", length = 36)
	private String fTel;

	@Column(name = "f_addr", length = 254)
	private String fAddr;

	@Column(name = "f_post_zip", length = 36)
	private String fPostZip;

	@JoinColumn(name = "f_parent_id")
	@ManyToOne(fetch=FetchType.LAZY)
	private OrgDept fParent;

	@Column(name = "f_full_id_path", length = 2000)
	private String fFullIdPath;

	@Column(name = "f_full_name_path", length = 2000)
	private String fFullNamePath;

	//private String fFullName;
	public String getfFullName() {
		if (getfParent() != null) {
			return getfParent().getfFullName() + "_" + getfName();
		} else if (getfCompany() != null) {
			return getfCompany().getfFullName() + "_" + getfName();
		}
		return getfName();
	}
}
