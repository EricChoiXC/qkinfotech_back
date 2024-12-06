package com.qkinfotech.core.org.model;

import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.sys.base.model.BaseCategory;
import com.qkinfotech.util.StringUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "org_group_cate")
@SimpleModel(url = "org/group/cate")
public class OrgGroupCate extends BaseCategory {

	public OrgGroupCate() {
		getfId();
	}

	public OrgGroupCate(String fId) {
		if (StringUtil.isNotNull(fId)) {
			setfId(fId);
		} else {
			getfId();
		}
	}
	
	@JoinColumn(name = "f_owner_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private OrgElement fOwner;

	@JoinColumn(name = "f_parent_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private OrgGroupCate fParent;
	
	@Column(name = "f_key_word", length = 200)
    private String fKeyword;

	@Column(name = "f_order", length = 200)
    private Integer fOrder;

	public String getfFullName() {
		if (getfParent() != null) {
			return getfParent().getfFullName() + "-" + getfName();
		}
		return getfName();
	}
}
