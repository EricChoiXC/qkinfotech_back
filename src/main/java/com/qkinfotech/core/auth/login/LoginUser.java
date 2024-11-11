package com.qkinfotech.core.auth.login;

import java.util.*;
import java.util.stream.Collectors;

import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.org.model.*;
import com.qkinfotech.util.SpringUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.qkinfotech.core.user.model.SysAuthority;
import com.qkinfotech.core.user.model.SysRole;
import com.qkinfotech.core.user.model.SysUser;
import com.qkinfotech.util.DESUtil;

public class LoginUser implements UserDetails {

	private static final long serialVersionUID = 1L;

	private String fLoginName;

	private String fLoginUserId;

	private String fPassword;

	private boolean fDisabled;

	private boolean fLocked;

	private boolean fExpired;

	private Collection<GrantedAuthority> fAuthorities;

	public LoginUser(SysUser user) {
		this.fLoginName = user.getfLoginName();
		this.fLoginUserId = user.getfId();
		this.fPassword = DESUtil.encrypt(user.getfPassword());
		this.fDisabled = user.getfDisabled();
		this.fLocked = user.getfLocked();
		this.fExpired = user.getfExpired();
		this.fAuthorities = new HashSet<GrantedAuthority>();

		if ("admin".equals(this.fLoginName)) {
			//管理员拥有所有权限
			SimpleService<SysAuthority> sysAuthorityService = (SimpleService<SysAuthority>) SpringUtil.getContext().getBean("sysAuthorityService");
			for (SysAuthority sysAuthority : sysAuthorityService.findAll()) {
				fAuthorities.add(new LoginUserGrantedAuthority(sysAuthority));
			}
		} else {
			for (SysRole role : user.getfRoles()) {
				for (SysAuthority authority : role.getfAuthorities()) {
					fAuthorities.add(new LoginUserGrantedAuthority(authority));
				}
			}

			for (SysAuthority authority : user.getfAuthorities()) {
				fAuthorities.add(new LoginUserGrantedAuthority(authority));
			}
			try {
				Set<String> orgIds = getUserHibernateIds(user.getfId());
				SimpleService<SysRole> sysRoleService = (SimpleService<SysRole>) SpringUtil.getContext().getBean("sysRoleService");
				for (SysRole role : sysRoleService.findAll()) {
					for (OrgElement element : role.getfElements()) {
						if (orgIds.contains(element.getfId())) {
							for (SysAuthority authority : role.getfAuthorities()) {
								fAuthorities.add(new LoginUserGrantedAuthority(authority));
							}
							break;
						}
					}
				}
			} catch (Exception e) {

			}
		}
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return fAuthorities;
	}

	@Override
	public String getPassword() {
		return fPassword;
	}

	@Override
	public String getUsername() {
		return fLoginName;
	}

	@Override
	public boolean isAccountNonExpired() {
		return !fExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return !fLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return !fDisabled;
	}

	@Override
	public String toString() {
		return fLoginName;
	}

	public String getFLoginUserId(){
		return fLoginUserId;
	}




	public Set<String> getUserHibernateIds (String userId) throws Exception {
		SimpleService<OrgPerson> orgPersonService = (SimpleService<OrgPerson>) SpringUtil.getContext().getBean("orgPersonService");
		SimpleService<OrgPostMember> orgPostMemberService = (SimpleService<OrgPostMember>) SpringUtil.getContext().getBean("orgPostMemberService");
		SimpleService<OrgGroupMember> orgGroupMemberService = (SimpleService<OrgGroupMember>) SpringUtil.getContext().getBean("orgGroupMemberService");
		Set<String> result = new HashSet<>();
		OrgPerson person = orgPersonService.getById(userId);
		if (person != null) {

			Specification<OrgPostMember> spec = new Specification<OrgPostMember>() {
				@Override
				public Predicate toPredicate(Root<OrgPostMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
					Predicate predicate = criteriaBuilder.equal(root.get("fElement").get("fId"), userId);
					return query.where(predicate).getRestriction();
				}
			};
			List<OrgPostMember> posts = orgPostMemberService.findAll(spec);
			result.addAll(posts.stream().map(OrgPostMember::getfId).collect(Collectors.toList()));

			Specification<OrgGroupMember> spec2 = new Specification<OrgGroupMember>() {
				@Override
				public Predicate toPredicate(Root<OrgGroupMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
					Predicate predicate = criteriaBuilder.equal(root.get("fElement").get("fId"), userId);
					return query.where(predicate).getRestriction();
				}
			};
			List<OrgGroupMember> groups = orgGroupMemberService.findAll(spec2);
			result.addAll(groups.stream().map(OrgGroupMember::getfId).collect(Collectors.toList()));

			result.addAll(getUserHibernateIds(person));
		}
		return result;
	}


	public List<String> getUserHibernateIds(OrgPerson person) throws Exception {
		List<String> ids = new ArrayList<>();
		ids.add(person.getfId());
		OrgDept dept = person.getfParent();
		OrgCompany company = null;
		while (dept != null) {
			ids.add(dept.getfId());
			company = dept.getfCompany();
			dept = dept.getfParent();
		}
		while (company != null) {
			ids.add(company.getfId());
			company = company.getfParent();
		}
		return ids;
	}
}
