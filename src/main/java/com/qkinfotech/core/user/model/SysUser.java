package com.qkinfotech.core.user.model;

import java.util.Set;

import com.qkinfotech.core.jpa.convertor.BCryptConverter;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "sys_user")
@Entity
@SimpleModel(url="sys/user")
public class SysUser extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "f_login_name", length = 200)
	private String fLoginName;

	@Column(name = "f_password", length = 256)
	@Convert(converter = BCryptConverter.class)
	private String fPassword;

	@Column(name = "f_alias", length = 32)
	private String fAlias;

	@Column(name = "f_disabled")
	private boolean fDisabled;

	@Column(name = "f_locked")
	private boolean fLocked;

	@Column(name = "f_expired")
	private boolean fExpired;

	@Column(name = "f_failure_count")
	private int fFailureCount = 0;

	@Column(name = "f_change_password")
	private boolean fChangePassword = false;
	
	
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "sys_user_role",  joinColumns = @JoinColumn(name = "f_user_id", referencedColumnName = "f_id"),
		    inverseJoinColumns = @JoinColumn(name = "f_role_id", referencedColumnName = "f_id"),
		    uniqueConstraints = {
		        @UniqueConstraint(name = "unique_user_role", columnNames = {"f_user_id", "f_role_id"})
		    },
		    indexes = {
			    	@Index(name = "idx_sys_user_role_user", columnList = "f_user_id"),	
			    	@Index(name = "idx_sys_user_role_role", columnList = "f_role_id")	
		    })
	private Set<SysRole> fRoles;
	
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "sys_user_authority",  joinColumns = @JoinColumn(name = "f_user_id", referencedColumnName = "f_id"),
		    inverseJoinColumns = @JoinColumn(name = "f_authority_id", referencedColumnName = "f_id"),
		    uniqueConstraints = {
		        @UniqueConstraint(name = "unique_user_authority", columnNames = {"f_user_id", "f_authority_id"})
		    },
		    indexes = {
			    	@Index(name = "idx_sys_user_authority_user", columnList = "f_user_id"),	
			    	@Index(name = "idx_sys_user_authority_authoriy", columnList = "f_authority_id")	
		    })
	private Set<SysAuthority> fAuthorities;

}
