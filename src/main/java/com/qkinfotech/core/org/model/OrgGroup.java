package com.qkinfotech.core.org.model;

import java.util.Set;

import jakarta.persistence.*;

import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.util.StringUtil;

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
@Table(name="org_group")
@SimpleModel(url = "org/group")
public class OrgGroup extends OrgElement {

	public OrgGroup() {
		setfType(OrgElement.TYPE_GROUP);
	}
	
	public OrgGroup(String fId) {
		setfType(OrgElement.TYPE_GROUP);
		if (StringUtil.isNotNull(fId)) {
			setfId(fId);
		} else {
			super.getfId();
		}
	}
	@JoinColumn(name = "f_owner_id")
	@ManyToOne(fetch=FetchType.LAZY)
	private OrgElement fOwner;
	
	@OneToMany(targetEntity=OrgGroupMember.class , mappedBy = "fGroup", cascade = CascadeType.ALL,fetch=FetchType.LAZY)
	private Set<OrgElement> fMembers;

	@JoinColumn(name = "f_group_cate_id")
	@ManyToOne(fetch=FetchType.LAZY)
	private OrgGroupCate fGroupCate;

	private String fFullName;
	public String getfFullName() {
		if (getfOwner() != null) {
			return getfOwner().getfName();
		}
		return "";
	}

}
