package com.qkinfotech.core.user.controller;

import com.alibaba.fastjson2.JSONObject;
import com.alicp.jetcache.Cache;
import com.qkinfotech.core.app.config.JetcacheConfig;
import com.qkinfotech.core.mvc.IEntityExtension;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.org.model.*;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import com.qkinfotech.core.user.SysUserService;
import com.qkinfotech.core.user.model.SysAuthority;
import com.qkinfotech.core.user.model.SysRole;
import com.qkinfotech.core.user.model.SysUser;
import com.qkinfotech.util.StringUtil;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/userController")
public class UserController {

    @Autowired
    public SysUserService sysUserService;

    @Autowired
    public SimpleService<OrgPerson> orgPersonService;

    @Resource
    public SimpleService<OrgPostMember> orgPostMemberService;

    @Resource
    public SimpleService<OrgGroupMember> orgGroupMemberService;

    @Autowired
    public SimpleService<SysRole> sysRoleService;

    @Autowired
    JetcacheConfig jetcacheConfig;

    @Autowired
    private Cache<String, Object> userCache;

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected Bean2Json bean2Json;

    /**
     * 获取用户权限，传入user.fId
     * @return
     * @throws Exception
     */
    @RequestMapping("/loadUserAuths")
    @ResponseBody
    public String loadUserAuths () throws Exception {
        JSONObject json = new JSONObject();
        String fId = getFidFromCookies();

        if (StringUtil.isNotNull(fId)) {
            SysUser user = sysUserService.getById(fId);
            if (user != null) {
                json = (JSONObject) jetcacheConfig.getCache(fId);
                userCache.put(fId, json);
            } else {
                json.put("result", false);
                json.put("message", "用户不存在");
            }
        } else {
            json.put("result", false);
            json.put("message", "请传入用户id");
        }
        return json.toString();
    }

    public Set<SysRole> getUserRoles (SysUser user) throws Exception {
        if (user != null) {
            Specification<SysRole> spec = new Specification<SysRole>() {
                @Override
                public Predicate toPredicate(Root<SysRole> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate predicate = criteriaBuilder.equal(root.get("fUsers").get("fId"), user.getfId());
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

    @PostMapping("updateAuths")
    public Boolean updateAuths() throws Exception {
        String fId = getFidFromCookies();
        Set<SysAuthority> auths = new HashSet<>();
        if (StringUtil.isNotNull(fId)) {
            userCache.put(fId, auths);
            return true;
        } else {
            return false;
        }
    }

    @PostMapping("deleteAuths")
    public Boolean deleteAuths() throws Exception {
        String fId = getFidFromCookies();
        if (StringUtil.isNotNull(fId)) {
            userCache.remove(fId);
            return true;
        } else {
            return false;
        }
    }

    public String getFidFromCookies () {
        String fId = "";
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if ("id".equals(cookie.getName())) {
                fId = cookie.getValue();
            }
        }
        return fId;
    }


    @PostMapping("updateRoleAuths")
    public void updateRoleAuths() throws Exception {
        System.out.println("updateRoleAuths");
        JSONObject json = getPostData("updateRoleAuths");
    }

    @PostMapping("updateRoleElements")
    public void updateRoleElements() throws Exception {
        System.out.println("updateRoleElements");
    }



    private JSONObject getPostData(String method) {
        JSONObject data = new JSONObject();
        if ("POST".equals(request.getMethod())) {
            try {
                InputStream in = request.getInputStream();
                byte[] b = FileUtil.readAsByteArray(in);
                String enc = request.getCharacterEncoding();
                if (!StringUtils.hasText(enc)) {
                    enc = "UTF-8";
                }
                String txt = new String(b, enc);
                if (!StringUtils.hasText(txt)) {
                    return new JSONObject();
                }
                data = JSONObject.parseObject(txt);

            } catch (IOException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
        return data;
    }

    /**
     * 通过loginName获取用户
     */
    @RequestMapping("/getUser")
    @ResponseBody
    public SysUser getUser () throws Exception {
        String loginName = request.getParameter("loginName");
        //根据登录用户获取用户
        return sysUserService.findByLoginName(loginName);
    }

    /**
     * 判断某用户是否归属于某组织架构集合中
     */
    @RequestMapping("/checkInOrg")
    @ResponseBody
    public Boolean checkInOrg () throws Exception {
        JSONObject json = getPostData("checkInOrg");
        String userId = json.getString("userId");
        List<String> orgIds = json.getList("orgIds", String.class);
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
            result.addAll(posts
                            .stream()
                            .map(orgPostMember -> orgPostMember.getfPost())
                            .map(OrgPost::getfId)
                            .collect(Collectors.toList()));

            Specification<OrgGroupMember> spec2 = new Specification<OrgGroupMember>() {
                @Override
                public Predicate toPredicate(Root<OrgGroupMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Predicate predicate = criteriaBuilder.equal(root.get("fElement").get("fId"), userId);
                    return query.where(predicate).getRestriction();
                }
            };
            List<OrgGroupMember> groups = orgGroupMemberService.findAll(spec2);
            result.addAll(groups
                            .stream()
                            .map(orgGroupMember -> orgGroupMember.getfGroup())
                            .map(OrgGroup::getfId)
                            .collect(Collectors.toList()));

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

    @RequestMapping("/roleSearchPerson")
    @ResponseBody
    public JSONObject roleSearchPerson() throws Exception {
        JSONObject body = getPostData("roleSearchPerson");
        String userId = body.getString("fId");
        JSONObject res = new JSONObject();
        Set<String> auths = new HashSet<>();
        if (StringUtil.isNotNull(userId)) {
            Set<String> orgIds = getUserHibernateIds(userId);
            Specification<SysRole> specification = new Specification<SysRole>() {
                @Override
                public Predicate toPredicate(Root<SysRole> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    CriteriaBuilder.In in = criteriaBuilder.in(root.get("fElements").get("fId"));
                    for (String orgId : orgIds) {
                        in.value(orgId);
                    }
                    return in;
                }
            };
            List<SysRole> sysRoleList = sysRoleService.findAll(specification);
            for (SysRole sysRole : sysRoleList) {
                sysRole.getfAuthorities().forEach(authority -> {
                    auths.add(authority.getfName());
                });
            }
            res.put("auths", auths);
            res.put("info", bean2Json.toJson(orgPersonService.getById(userId)));
        }
        return res;
    }

}
