package com.qkinfotech.core.org.model;

import com.qkinfotech.core.user.model.SysUser;

import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.util.StringUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 人员
 *
 */
@Getter
@Setter
@Entity
@Table(name="org_person")
@SimpleModel(url = "/org/person")
public class OrgPerson extends OrgElement {

	public OrgPerson() {
	}
	
	public OrgPerson(String fId) {
		if (StringUtil.isNotNull(fId)) {
			setfId(fId);
		} else {
			super.getfId();
		}
	}
	
	@Column(name = "f_gender", length = 16)
	private String fGender;

	@Column(name = "f_mobile", length = 36)
	private String fMobile;

	@Column(name = "f_tel", length = 36)
	private String fTel;

	@Column(name = "f_email", length = 200)
	private String fEmail;
	
	@Column(name = "f_is_business", nullable = true)
	private Boolean fIsBusiness;

	@JoinColumn(name = "f_parent_id")
	@ManyToOne(fetch=FetchType.LAZY)
	private OrgDept fParent;
	
	@Transient
	public int getfType() {
		return OrgElement.TYPE_PERON;
	}

	@OneToOne(fetch = FetchType.LAZY)
	private SysUser fUser;

	public String getfFullName() {
		if (getfParent() != null) {
			return getfParent().getfFullName();
		}
		return "";
	}

	/**
	 * 用户类型，枚举：
	 * 1，内部员工；2.专家； 3.供应商
	 * */
	@Column(name = "f_ekp_user_type", length = 255)
	private String fEkpUserType;

	/**
	 * 供应商类型，枚举：
	 * 1.境内单位；2。国内个人/国内设计团队；3.境外团队（含港澳台）
	 */
	@Column(name = "f_supplier_type", length = 255)
	private String fSupplierType;

	/**
	 * 供应商统一社会信用代码
	 */
	@Column(name = "f_supplier_code", length = 255)
	private String fSupplierCode;

	/**
	 * 供应商法人名称
	 */
	@Column(name = "f_supplier_leader", length = 255)
	private String fSupplierLeader;

	/**
	 * 供应商联系人名称
	 */
	@Column(name = "f_supplier_contacts", length = 255)
	private String fSupplierContacts;

	/**
	 * 专家身份证号
	 */
	@Column(name = "f_expert_code", length = 255)
	private String fExpertCode;

	/**
	 * 专家银行卡号
	 */
	@Column(name = "f_expert_bank_num", length = 255)
	private String fExpertBankNum;

	/**
	 * 随后一次更新的id
	 */
	@Column(name = "f_update_id", length = 255)
	private String fUpdateId;
}
