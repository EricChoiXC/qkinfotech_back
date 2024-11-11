package com.qkinfotech.core.org.model;

import java.util.Set;

import com.qkinfotech.core.mvc.SimpleModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色
 */
@Getter
@Setter
@Entity
@Table(name = "org_auth")
@SimpleModel(url = "/org/auth")
public class OrgAuth extends OrgElement {
	
	/**
	 * 角色名称
	 */
	@Column(name = "f_name")
	private String fName;
	
	/**
	 * 用户指派
	 */
	@ManyToMany(mappedBy = "fAuths", fetch = FetchType.LAZY)
	private Set<OrgElement> fElements;
	 
	
	/**
	 * 权限分配
	 */
	//private Set<OrgRole> fRoles;
	
}
