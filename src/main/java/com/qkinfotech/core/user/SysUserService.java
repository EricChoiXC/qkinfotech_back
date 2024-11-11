package com.qkinfotech.core.user;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.org.model.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qkinfotech.core.auth.login.LoginUser;
import com.qkinfotech.core.mvc.SimpleRepository;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.user.model.SysUser;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class SysUserService extends SimpleService<SysUser>{

	@Autowired
	public SimpleService<OrgPerson> orgPersonService;

	@Resource
	public SimpleService<OrgPostMember> orgPostMemberService;

	@Resource
	public SimpleService<OrgGroupMember> orgGroupMemberService;
	
	public SysUserService(SimpleRepository<SysUser> sysUserRepository) {
		super(sysUserRepository);
	}

	public UserDetails getLoginUser(String username) {
		SysUser user = findByLoginName(username);
		if (user == null) {
			return null;
		}
		return	new LoginUser(user);
	}
	
	public SysUser findByLoginName(String loginName) {
		
		Specification<SysUser> spec = new Specification<SysUser>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<SysUser> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				
				Predicate predicate = cb.equal(root.get("fLoginName"), loginName);

				return query.where(predicate).getRestriction();
			}
		};
		
		return repository.findOne(spec).orElse(null);
		
	}

	/**
	 * 判断某用户是否归属于某组织架构集合中
	 */
	public Boolean checkInOrg (String userId, List<String> orgIds) throws Exception {
		try {
			Set<String> userOrgs = getUserHibernateIds(userId);
			for (String org : userOrgs) {
				if (orgIds.contains(org)) {
					return true;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}


	public Set<String> getUserHibernateIds (String userId) throws Exception {
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