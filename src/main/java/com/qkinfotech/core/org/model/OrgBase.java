package com.qkinfotech.core.org.model;

import java.util.Date;
import java.util.Set;

import jakarta.persistence.*;
import org.springframework.util.StringUtils;

import com.qkinfotech.core.mvc.BaseEntity;

@MappedSuperclass
public class OrgBase extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

	@OneToOne(optional=false, cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT))
	private OrgElement baseInfo = new OrgElement();

	public OrgElement getBaseInfo() {
		return baseInfo;
	}

	public void setBaseInfo(OrgElement baseInfo) {
		this.baseInfo = baseInfo;
	}

	public String getfId() {
//		if(!StringUtils.hasText(this.fId)) {
			this.fId = baseInfo.getfId();
//		}
		return this.fId;
	}

	public void setfId(String fId) {
//		if(StringUtils.hasText(fId)) {
			this.fId = fId;
			baseInfo.setfId(fId);
//		}
	}

	@Transient
	public Set<OrgAuth> getfAuths() {
		return baseInfo.getfAuths();
	}

	@Transient
	public String getfCode() {
		return baseInfo.getfCode();
	}

	@Transient
	public Date getfCreateTime() {
		return baseInfo.getfCreateTime();
	}

	@Transient
	public String getfName() {
		return baseInfo.getfName();
	}

	@Transient
	public String getfNamePinYin() {
		return baseInfo.getfNamePinYin();
	}

	@Transient
	public String getfNameSimplePinYin() {
		return baseInfo.getfNameSimplePinYin();
	}

	@Transient
	public String getfNo() {
		return baseInfo.getfNo();
	}

	@Transient
	public String getfOrder() {
		return baseInfo.getfOrder();
	}

	@Transient
	public int getfType() {
		return baseInfo.getfType();
	}

	@Transient
	public boolean getfValid() {
		return baseInfo.getfValid();
	}

	public void setfAuths(Set<OrgAuth> fAuths) {
		baseInfo.setfAuths(fAuths);
	}

	public void setfCode(String fCode) {
		baseInfo.setfCode(fCode);
	}

	public void setfCreateTime(Date fCreateTime) {
		baseInfo.setfCreateTime(fCreateTime);
	}

	public void setfName(String fName) {
		baseInfo.setfName(fName);
	}

	public void setfNamePinYin(String fNamePinYin) {
		baseInfo.setfNamePinYin(fNamePinYin);
	}

	public void setfNameSimplePinYin(String fNameSimplePinYin) {
		baseInfo.setfNameSimplePinYin(fNameSimplePinYin);
	}

	public void setfNo(String fNo) {
		baseInfo.setfNo(fNo);
	}

	public void setfOrder(String fOrder) {
		baseInfo.setfOrder(fOrder);
	}

	public void setfType(int fType) {
		baseInfo.setfType(fType);
	}

	public void setfValid(boolean fValid) {
		baseInfo.setfValid(fValid);
	}

}
