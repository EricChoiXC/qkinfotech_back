package com.qkinfotech.core.user.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.mvc.models.ITreeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "sys_role_group")
@Entity
@SimpleModel(url="sys/role/group")
public class SysRoleGroup extends BaseEntity implements ITreeEntity<SysRoleGroup> {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "f_name", length = 32)
	private String fName;

	@Column(name = "f_description", length = 256)
	private String fDescription;

}
