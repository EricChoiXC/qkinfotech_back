package com.qkinfotech.core.app.config;

import com.alibaba.fastjson2.JSONObject;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.org.model.*;
import com.qkinfotech.core.user.model.SysAuthority;
import com.qkinfotech.core.user.model.SysRole;
import com.qkinfotech.core.user.model.SysUser;
import com.qkinfotech.util.StringUtil;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.domain.Specification;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class JetcacheConfig {

    @Autowired
    private CacheManager cacheManager;

    private Cache<String, Object> userCache;

    @Autowired
    public SimpleService<SysUser> sysUserService;

    @Autowired
    public SimpleService<SysRole> sysRoleService;

    @Autowired
    public SimpleService<OrgPerson> orgPersonService;

    @Autowired
    public SimpleService<OrgPostMember> orgPostMemberService;

    @Autowired
    public SimpleService<OrgGroupMember> orgGroupMemberService;

    @PostConstruct
    public void init() {
        QuickConfig qc = QuickConfig.newBuilder("userCache:")
                .expire(Duration.ofSeconds(3600))
                .cacheType(CacheType.BOTH)
                .syncLocal(false)
                .build();

        userCache = cacheManager.getOrCreateCache(qc);
    }

    @Bean
    public Cache<String, Object> getUserCache() {
        return userCache;
    }

    public JSONObject getCache(String id) throws Exception {
        if (StringUtil.isNull(id)) {
            return null;
        }
        JSONObject val = (JSONObject) userCache.get(id);
        if (StringUtil.isNotNull(id)) {
            SysUser user = sysUserService.getById(id);
            if (user != null) {
                val = new JSONObject();
                Set<String> authNames = new HashSet<>();
                Set<SysAuthority> authorities = user.getfAuthorities();
                Set<SysRole> roles = getUserRoles(user);
                listAuthNames(authNames, authorities, roles);
                val.put("result", true);
                val.put("auths", authNames.toArray());
                userCache.put(id, val);
            }
        }
        return val;
    }

    public Set<SysRole> getUserRoles (SysUser user) throws Exception {
        if (user != null) {
            Specification<SysRole> spec = new Specification<SysRole>() {
                @Override
                public Predicate toPredicate(Root<SysRole> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate predicate = null;
                    try {
                        predicate = root.get("fElements").get("fId").in(getUserHibernateIds(user));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return query.where(predicate).getRestriction();
                }
            };
            List<SysRole> list = sysRoleService.findAll(spec);
            return new HashSet<>(list);
        }
        return new HashSet<SysRole>();
    }

    public void listAuthNames(Set<String> authNames, Set<SysAuthority> authorities, Set<SysRole> roles) {
        for (SysAuthority auth : authorities) {
            if (!authNames.contains(auth.getfName())) {
                authNames.add(auth.getfName());
            }
        }
        for (SysRole role : roles) {
            if (role.getfAuthorities() != null && !role.getfAuthorities().isEmpty()) {
                for (SysAuthority auth : role.getfAuthorities()) {
                    if (!authNames.contains(auth.getfName())) {
                        authNames.add(auth.getfName());
                    }
                }
            }
        }
    }

    public Set<String> getUserHibernateIds (SysUser user) throws Exception {
        Set<String> result = new HashSet<>();
        OrgPerson person = orgPersonService.getById(user.getfId());
        if (person != null) {

            Specification<OrgPostMember> spec = new Specification<OrgPostMember>() {
                @Override
                public Predicate toPredicate(Root<OrgPostMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate predicate = criteriaBuilder.equal(root.get("fElement").get("fId"), user.getfId());
                    return query.where(predicate).getRestriction();
                }
            };
            List<OrgPostMember> posts = orgPostMemberService.findAll(spec);
            result.addAll(posts.stream().map(OrgPostMember::getfId).collect(Collectors.toList()));

            Specification<OrgGroupMember> spec2 = new Specification<OrgGroupMember>() {
                @Override
                public Predicate toPredicate(Root<OrgGroupMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate predicate = criteriaBuilder.equal(root.get("fElement").get("fId"), user.getfId());
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
