package com.qkinfotech.core.org.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 群组
 *
 */
@Getter
@Setter
@Entity
@Table(name="org_group_member")
@SimpleModel(url = "org/group/member")
public class OrgGroupMember extends BaseEntity {

	@JoinColumn(name = "f_group_id", nullable = false)
	@ManyToOne(fetch=FetchType.LAZY)
	private OrgGroup fGroup;

	@JoinColumn(name = "f_element_id", nullable = false)
	@ManyToOne(fetch=FetchType.LAZY)
	private OrgElement fElement;

	@Column(name = "f_manager", nullable = false)
	private boolean fManager;
	
	public void setfElement(OrgElement fElement) {
		if(fManager == true) {
			if(fElement!= null && fElement.getfType() != OrgElement.TYPE_PERON) {
				throw new IllegalArgumentException("Group Manager must be a person");
			}
		}
		this.fElement = fElement;
	}
	
	public void setfManager(boolean fManager) {
		if(fManager == true) {
			if(fElement!= null && fElement.getfType() != OrgElement.TYPE_PERON) {
				throw new IllegalArgumentException("Group Manager must be a person");
			}
		}
		this.fManager = fManager;
	}

}
