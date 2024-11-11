package com.qkinfotech.core.user.model;

import java.util.Set;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;

import com.qkinfotech.core.org.model.OrgElement;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Administrator
 */
@Getter
@Setter
@Table(name = "sys_role")
@Entity
@SimpleModel(url="sys/role")
public class SysRole extends BaseEntity {

	public SysRole(String fId, String fName,Boolean fAdminRole, SysRoleGroup fRoleGroup) {
		super();
		this.fId = fId;
		this.fName = fName;
		this.fAdminRole = fAdminRole;
		this.fRoleGroup = fRoleGroup;
	}

	public SysRole() {
		super();
	}

	private static final long serialVersionUID = 1L;

	@Column(name = "f_name", length = 64)
	private String fName;

	@Column(name = "f_admin_role")
	private Boolean fAdminRole;

	@ManyToOne(targetEntity = SysRoleGroup.class)
	@JoinColumn(name = "f_role_group_id")
	private SysRoleGroup fRoleGroup;

	@ManyToMany(mappedBy = "fRoles", fetch = FetchType.LAZY)
	private Set<SysUser> fUsers;

	@Column(name = "f_disabled")
	private boolean fDisabled = false;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "sys_role_authority",  joinColumns = @JoinColumn(name = "f_role_id", referencedColumnName = "f_id"),
		    inverseJoinColumns = @JoinColumn(name = "f_authority_id", referencedColumnName = "f_id"),
		    uniqueConstraints = {
		        @UniqueConstraint(name = "unique_role_authority", columnNames = {"f_role_id", "f_authority_id"})
		    },
		    indexes = {
			    	@Index(name = "idx_sys_role_authority_roler", columnList = "f_role_id"),
			    	@Index(name = "idx_sys_role_authority_authoriy", columnList = "f_authority_id")
		    })
	private Set<SysAuthority> fAuthorities;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "sys_role_elements", joinColumns = @JoinColumn(name = "f_role_id", referencedColumnName = "f_id"),
			inverseJoinColumns = @JoinColumn(name = "f_element_id", referencedColumnName = "f_id"),
			uniqueConstraints = {
				@UniqueConstraint(name = "unique_role_element", columnNames = {"f_role_id", "f_element_id"})
			},
			indexes = {
				@Index(name = "idx_sys_role_element_role", columnList = "f_role_id"),
				@Index(name = "idx_sys_role_element_element", columnList = "f_element_id")
			})
	private Set<OrgElement> fElements;

	public void setfAuthorities(Set<SysAuthority> fAuthorities) {
		this.fAuthorities = fAuthorities;
	}

}
