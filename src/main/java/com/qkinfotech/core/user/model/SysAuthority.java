package com.qkinfotech.core.user.model;

import java.util.Set;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "sys_authority")
@Entity
@SimpleModel(url="sys/authority")
public class SysAuthority extends BaseEntity {

	public SysAuthority(String fId, String fName, String fModule, String fGroup) {
		super();
		this.fId = fId;
		this.fName = fName;
		this.fModule = fModule;
		this.fGroup = fGroup;
	}
	
	public SysAuthority() {
		super();
	}

	private static final long serialVersionUID = 1L;

	@Column(name = "f_name", length = 64)
	private String fName;

	@Column(name = "f_module", length = 64)
	private String fModule;

	@Column(name = "f_group", length = 64)
	private String fGroup;

	@ManyToMany(mappedBy = "fAuthorities", fetch = FetchType.LAZY)
	private Set<SysUser> fUsers;


}
