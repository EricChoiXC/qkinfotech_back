package com.qkinfotech.core.org.model;

import java.util.Date;
import java.util.Set;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 组织架构单元
 *
 */
@Getter
@Setter
@Entity
@Table(name="org_element")
@SimpleModel(url = "org/element")
@Inheritance(strategy = InheritanceType.JOINED)
public class OrgElement extends BaseEntity {
	
	public static final int TYPE_COMPANY = 1;
	public static final int TYPE_DEPT = 2;
	public static final int TYPE_POST = 3;
	public static final int TYPE_TITLE = 4;
	public static final int TYPE_GROUP = 5;
	
	public static final int TYPE_PERON = 80;
	public static final int TYPE_PERSON_DETAIL = 81;
	
	
	@Column(name = "f_code", length = 32)
	protected String fCode;

	@Column(name = "f_name", length = 200)
	protected String fName;

	@Column(name = "f_type", nullable = false)
	private int fType;

	@Column(name = "f_valid", nullable = false)
	private boolean fValid = true;
	
	@Column(name = "f_create_time")
	private Date fCreateTime;
	
	@Column(name = "f_name_pin_yin")
	private String fNamePinYin;
	
	@Column(name = "f_name_simple_pin_yin")
	private String fNameSimplePinYin;
	
	@Column(name = "f_no")
	private String fNo;
	
	@Column(name = "f_order")
	private String fOrder;
	
	@ManyToMany
	@JoinTable(name = "org_auth_element",
			joinColumns = {@JoinColumn(name = "f_element_id")},
			inverseJoinColumns = {@JoinColumn(name = "f_role_id")})
	private Set<OrgAuth> fAuths;


	@Column(name = "f_hibernate_ids", length = 2000)
	private String fHibernateIds;

}
