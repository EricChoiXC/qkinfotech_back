package com.qkinfotech.util;

import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.org.model.*;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 组织架构工具类，提供组织架构拆解
 */
public class OrganizationUtil {

    private SimpleService<OrgElement> orgElementService = SpringUtil.getContext().getBean("orgElementService", SimpleService.class);

    private SimpleService<OrgCompany> orgCompanyService = SpringUtil.getContext().getBean("orgCompanyService", SimpleService.class);

    private SimpleService<OrgDept> orgDeptService = SpringUtil.getContext().getBean("orgDeptService", SimpleService.class);

    private SimpleService<OrgPerson> orgPersonService = SpringUtil.getContext().getBean("orgPersonService", SimpleService.class);

    private SimpleService<OrgTitle> orgTitleService = SpringUtil.getContext().getBean("orgTitleService", SimpleService.class);

    private SimpleService<OrgGroup> orgGroupService = SpringUtil.getContext().getBean("orgGroupService", SimpleService.class);

    private SimpleService<OrgPost> orgPostService = SpringUtil.getContext().getBean("orgPostService", SimpleService.class);

    private SimpleService<OrgGroupMember> orgGroupMemberService = SpringUtil.getContext().getBean("orgGroupMemberService", SimpleService.class);

    private SimpleService<OrgPostMember> orgPostMemberService = SpringUtil.getContext().getBean("orgPostMemberService", SimpleService.class);

    public Set<OrgElement> getOrgElements(String orgId) {
        return getOrgElements(orgElementService.getById(orgId));
    }


    public Set<OrgElement> getOrgElements(OrgElement orgElement) {
        if (orgElement == null) {
            return new HashSet<>();
        }
        Set<OrgElement> orgElements = new HashSet<OrgElement>();
        getHierarchy(orgElement, orgElements);
        getGroups(orgElements);
        getPosts(orgElements);
        return orgElements;
    }

    private Set<OrgElement> getHierarchy(OrgElement orgElement, Set<OrgElement> orgElements) {
        if (orgElement == null) {
            return orgElements;
        }
        if (orgElement instanceof OrgCompany company) {
            orgElements.add(orgElement);
            getHierarchy(company.getfParent(), orgElements);
        } else if (orgElement instanceof OrgDept dept) {
            orgElements.add(orgElement);
            getHierarchy(dept.getfParent(), orgElements);
            getHierarchy(dept.getfCompany(), orgElements);
        } else if (orgElement instanceof OrgPerson person) {
            orgElements.add(orgElement);
            getHierarchy(person.getfParent(), orgElements);
        }
        return orgElements;
    }

    private Set<OrgElement> getGroups(Set<OrgElement> orgElements) {
        Specification<OrgGroupMember> spec = new Specification<OrgGroupMember>() {
            @Override
            public Predicate toPredicate(Root<OrgGroupMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate eq = criteriaBuilder.equal(root.get("fGroup").get("fValid"), true);
                CriteriaBuilder.In in = criteriaBuilder.in(root.get("fElement").get("fId"));
                for (OrgElement orgElement : orgElements) {
                    in.value(orgElement.getfId());
                }
                return criteriaBuilder.and(eq, in);
            }
        };
        List<OrgGroupMember> list = orgGroupMemberService.findAll(spec);
        for (OrgGroupMember member : list) {
            if (!orgElements.contains(member)) {
                orgElements.add(member.getfGroup());
            }
        }
        return orgElements;
    }

    private Set<OrgElement> getPosts(Set<OrgElement> orgElements) {
        Specification<OrgPostMember> spec = new Specification<OrgPostMember>() {
            @Override
            public Predicate toPredicate(Root<OrgPostMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate eq = criteriaBuilder.equal(root.get("fPost").get("fValid"), true);
                CriteriaBuilder.In in = criteriaBuilder.in(root.get("fElement").get("fId"));
                for (OrgElement orgElement : orgElements) {
                    in.value(orgElement.getfId());
                }
                return criteriaBuilder.and(eq, in);
            }
        };
        List<OrgPostMember> list = orgPostMemberService.findAll(spec);
        for (OrgPostMember member : list) {
            if (!orgElements.contains(member)) {
                orgElements.add(member.getfPost());
            }
        }
        return orgElements;
    }

}
