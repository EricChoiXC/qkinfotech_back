package com.qkinfotech.core.tendering.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.IEntityExtension;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.mvc.util.QueryBuilder;
import com.qkinfotech.core.org.model.*;
import com.qkinfotech.core.tendering.model.apps.assembly.AppsAssemblyAuth;
import com.qkinfotech.core.tendering.model.apps.assembly.AppsAssemblyAuthUsed;
import com.qkinfotech.core.tendering.model.apps.shortcuts.AppsShortcuts;
import com.qkinfotech.core.tendering.model.apps.shortcuts.AppsShortcutsUsed;
import com.qkinfotech.core.user.SysUserService;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/apps/assembly/auth")
public class AppsAssemblyAuthController<T extends BaseEntity> {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected SimpleResult result;

    @Autowired
    protected Json2Bean json2bean;

    @Autowired
    protected Bean2Json bean2json;

    @Autowired(required = false)
    protected List<IEntityExtension> extensions;

    protected Class<AppsAssemblyAuth> modelClass;
    @Autowired
    protected SimpleService<AppsAssemblyAuth> appsAssemblyAuthService;

    @Autowired
    public SysUserService sysUserService;

    @Autowired
    public SimpleService<OrgPerson> orgPersonService;

    @Autowired
    public SimpleService<OrgPostMember> orgPostMemberService;

    @Autowired
    public SimpleService<OrgGroupMember> orgGroupMemberService;

    protected static final String IDS_DELIMITERS = ",; \t\n";

    /**
     * 查询用户绑定的组件列表
     * @throws Exception        抛出异常
     */
    @RequestMapping("/user/list")
    @ResponseBody
    public void userList() throws Exception {
        JSONObject body = getPostData();
        String userId;
        if(body.containsKey("userId")){
            userId = body.getString("userId");
            List<String> userOrgIds = getUserOrgIds(userId);
            JSONArray query = null;
            if(body.containsKey("query")){
                query = JSONArray.parseArray(body.getString("query"));
            }else{
                query = new JSONArray();
            }
            JSONObject orgInJson = new JSONObject();
            JSONObject orgJson = new JSONObject();
            orgJson.put("fUsedList.fId",userOrgIds);
            orgInJson.put("in",orgJson);
            query.add(orgInJson);
            body.put("query",query);
        } else {
            userId = "";
        }
        QueryBuilder<AppsAssemblyAuth> qb = QueryBuilder.parse(modelClass, body);
        List<AppsAssemblyAuth> data = appsAssemblyAuthService.findAll(qb.specification());
        JSONArray out = new JSONArray();
        data.forEach(e -> {
            JSONObject obj = bean2json.toJson(e);
            if(org.apache.commons.lang.StringUtils.isNotBlank(userId)){
                //验证权限
                List<String> orgIds = e.getfUsedList().stream().map(AppsAssemblyAuthUsed::getfUsed).map(OrgElement::getfId).toList();
                if (getIsOk(userId, orgIds)) {
                    //可使用的
                    out.add(bean2json.toJson(e));
                }
            }
            out.add(obj);
        });
        result.from(out);
    }

    @RequestMapping("/list")
    @ResponseBody
    public void list() throws Exception {
        JSONObject body = getPostData();
        //获取所有列表
        QueryBuilder<AppsAssemblyAuth> qb = QueryBuilder.parse(modelClass, body);
        Page<AppsAssemblyAuth> data = appsAssemblyAuthService.findAll(qb.specification(),qb.pageable());
        Page<JSONObject> out = data.map(e -> bean2json.toJson(e));
        result.from(out);
    }

    @RequestMapping("/save")
    @ResponseBody
    public void save() throws Exception {
        JSONObject body = getPostData("save");
        if (body == null) {
            throw new IllegalArgumentException("illegal request body");
        }
        AppsAssemblyAuth target = json2bean.toBean(body, appsAssemblyAuthService.getEntityClass());
        appsAssemblyAuthService.save(target);
        result.ok();
    }

    /**
     * 根据主键删除
     */
    @RequestMapping("/delete")
    @ResponseBody
    public void delete() throws Exception {
        List<String> fId = new ArrayList<>();
        if (fId.isEmpty()) {
            JSONObject data = getPostData("delete");
            if (data != null) {
                List<String> d = data.getList("fId", String.class);
                if (d != null) {
                    fId.addAll(d);
                }
            }
        }
        Set<String> ids = new LinkedHashSet<String>();
        for (Object id : fId) {
            if (id instanceof String s) {
                String[] tokenized = StringUtils.tokenizeToStringArray(s, IDS_DELIMITERS);
                Collections.addAll(ids, tokenized);
            } else {
                throw new IllegalArgumentException("typeof fId is unknow");
            }

        }
        appsAssemblyAuthService.delete(StringUtils.toStringArray(ids));
        result.ok();
    }

    @RequestMapping("/load")
    @ResponseBody
    public void load() throws Exception {
        String fId = request.getParameter("fId");
        if (!StringUtils.hasText(fId)) {
            JSONObject data = getPostData("load");
            if (data != null) {
                fId = data.getString("fId");
            }
        }
        if (!StringUtils.hasText(fId)) {
            throw new IllegalArgumentException("fId is empty");
        }
        AppsAssemblyAuth d = appsAssemblyAuthService.getById(fId);
        result.from(bean2json.toJson(d));
    }

    private JSONObject getPostData() {
        if (!"POST".equals(request.getMethod())) {
            return new JSONObject();
        }
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
            return JSONObject.parseObject(txt);

        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
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
        if(extensions != null) {
            for(int i = 0; i < extensions.size(); ++ i) {
                extensions.get(i).prepare(modelClass, method, data);
            }
        }
        return data;
    }

    public Set<String> getUserHibernateIds(String userId) throws Exception {
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

    public List<String> getUserOrgIds(String userId) {
        List<String> orgIds;
        try {
            Set<String> userOrgIds = getUserHibernateIds(userId);
            orgIds = userOrgIds.stream().toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return orgIds;
    }

    /**
     * 验证用户是否在组织架构内
     *
     * @param userId 用户id
     * @param orgIds 组织架构集合
     * @return 是否在组织架构内
     */
    public boolean getIsOk(String userId, List<String> orgIds) {
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
}
