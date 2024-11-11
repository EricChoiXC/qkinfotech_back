package com.qkinfotech.core.org.model;

import com.qkinfotech.core.mvc.SimpleModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 职级
 *
 */
@Getter
@Setter
@Entity
@Table(name="org_title")
@SimpleModel(url = "/org/title")
public class OrgTitle extends OrgElement {

	public OrgTitle() {
	}
	
	@JoinColumn(name = "f_owner_id")
	@ManyToOne(fetch=FetchType.LAZY)
	private OrgElement fOwner;
	
	@Column(name = "f_level", nullable = false)
	private int fLevel;
	
	@Transient
	public int getfType() {
		return OrgElement.TYPE_TITLE;
	}
}
