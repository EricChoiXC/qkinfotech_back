package com.qkinfotech.core.org.model;

import com.qkinfotech.core.mvc.SimpleModel;
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
 * 
 * 岗位
 *
 */
@Getter
@Setter
@Entity
@Table(name="org_post")
@SimpleModel(url = "org/post")
public class OrgPost extends OrgElement{
	
	public OrgPost() {
		setfType(OrgElement.TYPE_POST);
	}
	
	public OrgPost(String fId) {
		setfType(OrgElement.TYPE_POST);
		if (StringUtil.isNotNull(fId)) {
			setfId(fId);
		} else {
			super.getfId();
		}
	}

	@JoinColumn(name = "f_owner_id")
	@ManyToOne(fetch=FetchType.LAZY)
	private OrgElement fOwner;
	
	@Column(name = "f_level", nullable = false)
	private int fLevel;

	private String fFullName;
	public String getfFullName() {
		if (getfOwner() != null) {
			return getfOwner().getfName();
		}
		return "";
	}

}
