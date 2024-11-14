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
import com.qkinfotech.core.tendering.model.apps.assembly.AppsAssemblyUserAuth;
import com.qkinfotech.core.user.SysUserService;
import com.qkinfotech.util.IDGenerate;
import com.qkinfotech.util.SqlUtil;
import com.qkinfotech.util.StringUtil;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/apps/assembly/user/auth")
public class AppsAssemblyUserAuthController<T extends BaseEntity> {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected SimpleResult result;

    @Autowired
    protected Json2Bean json2bean;

    @Autowired
    protected Bean2Json bean2json;

    @Autowired(required = false)
    protected List<IEntityExtension> extensions;

    protected Class<AppsAssemblyUserAuth> modelClass = AppsAssemblyUserAuth.class;
    protected Class<AppsAssemblyAuth> authModelClass = AppsAssemblyAuth.class;
    @Autowired
    protected SimpleService<AppsAssemblyUserAuth> appsAssemblyUserAuthService;
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
     *
     * @throws Exception 抛出异常
     */
    @RequestMapping("/user/list")
    @ResponseBody
    public void userList() throws Exception {
        JSONObject body = getPostData();
        JSONObject data = new JSONObject();
        JSONArray out = new JSONArray();
        //需要新增的列表
        List<AppsAssemblyUserAuth> addList = new ArrayList<>();
        //需要删除的列表
        List<String> delList = new ArrayList<>();
        //结果集
        List<AppsAssemblyUserAuth> resultList = new ArrayList<>();
        String userId;
        if (body.containsKey("userId")) {
            userId = body.getString("userId");
        } else {
            userId = "";
        }
        if (StringUtil.isNull(userId)) {
            data.put("message", "当前用户未登录");
            output(500, data);
        } else {
            //判断当前用户是否还拥有所持有的组件权限
            QueryBuilder<AppsAssemblyAuth> qb = QueryBuilder.parse(authModelClass, new JSONObject());
            List<AppsAssemblyAuth> dataList = appsAssemblyAuthService.findAll(qb.specification());
            if (!dataList.isEmpty()) {
                Map<String, AppsAssemblyUserAuth> userKeyMap = getUserAuthMap(userId);
                if (!userKeyMap.isEmpty()) {
                    dataList.forEach(dataModel -> {
                        List<String> orgIds = dataModel.getfUsedList().stream().map(AppsAssemblyAuthUsed::getfUsed).map(OrgElement::getfId).toList();
                        if (getIsOk(userId, orgIds)) {
                            //用户拥有当前组织权限,判断原权限Map是否存在
                            if (!userKeyMap.containsKey(dataModel.getfComponentKey())) {
                                addUserAuth(addList, resultList, userId, dataModel);
                            } else {
                                AppsAssemblyUserAuth userAuth = userKeyMap.get(dataModel.getfComponentKey());
                                userAuth.setfAssemblyName(dataModel.getfName());
                                resultList.add(userAuth);
                            }
                            userKeyMap.remove(dataModel.getfComponentKey());
                        }
                    });
                } else {
                    dataList.forEach(dataModel -> {
                        List<String> orgIds = dataModel.getfUsedList().stream().map(AppsAssemblyAuthUsed::getfUsed).map(OrgElement::getfId).toList();
                        if (getIsOk(userId, orgIds)) {
                            addUserAuth(addList, resultList, userId, dataModel);
                        }
                    });
                }
                //判断当前Map中存在的已经变更的组件权限，需要删除掉
                userKeyMap.keySet().forEach(userKey -> {
                    delList.add(userKeyMap.get(userKey).getfId());
                });
            } else {
                result.from(out);
            }
        }
        //执行数据库操作
        if(!addList.isEmpty()){
            addList.forEach(dataModel -> {
                appsAssemblyUserAuthService.save(dataModel);
            });
        }
        if(!delList.isEmpty()){
            delList.forEach(dataModel -> {
                appsAssemblyAuthService.delete(dataModel);
            });
        }
        if(!resultList.isEmpty()){
            List<AppsAssemblyUserAuth> outList = resultList.stream()
                    .sorted(Comparator.comparing(AppsAssemblyUserAuth::getfOrder, Comparator.nullsLast(Integer::compareTo)))
                    .toList();
            outList.forEach(dataModel -> {
                out.add(bean2json.toJson(dataModel));
            });
        }
        result.from(out);
    }

    private void addUserAuth(List<AppsAssemblyUserAuth> addList, List<AppsAssemblyUserAuth> resultList, String userId, AppsAssemblyAuth dataModel) {
        AppsAssemblyUserAuth userAuth = new AppsAssemblyUserAuth();
        userAuth.setfComponentKey(dataModel.getfComponentKey());
        userAuth.setfAssemblyComponentDataKey(dataModel.getfAssemblyComponentDataKey());
        userAuth.setfAssemblyName(dataModel.getfName());
        userAuth.setfUser(sysUserService.getById(userId));
        userAuth.setfOrder(dataModel.getfOrder());
        userAuth.setfState(true);
        userAuth.setfId(IDGenerate.generate());
        addList.add(userAuth);
        resultList.add(userAuth);
    }

    private Map<String, AppsAssemblyUserAuth> getUserAuthMap(String userId) {
        Map<String, AppsAssemblyUserAuth> map = new HashMap<>();
        JSONObject body = new JSONObject();
        SqlUtil.setParameter(body,"fUser.fId",userId,"query","eq");
        QueryBuilder<AppsAssemblyUserAuth> qb = QueryBuilder.parse(modelClass, body);
        List<AppsAssemblyUserAuth> dataList = appsAssemblyUserAuthService.findAll(qb.specification());
        for (AppsAssemblyUserAuth data : dataList) {
            map.put(data.getfComponentKey(), data);
        }
        return map;
    }



    @RequestMapping("/list")
    @ResponseBody
    public void list() throws Exception {
        JSONObject body = getPostData();
        //获取所有列表
        QueryBuilder<AppsAssemblyUserAuth> qb = QueryBuilder.parse(modelClass, body);
        Page<AppsAssemblyUserAuth> data = appsAssemblyUserAuthService.findAll(qb.specification(), qb.pageable());
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
        AppsAssemblyUserAuth target = json2bean.toBean(body, appsAssemblyUserAuthService.getEntityClass());
        appsAssemblyUserAuthService.save(target);
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
        appsAssemblyUserAuthService.delete(StringUtils.toStringArray(ids));
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
        AppsAssemblyUserAuth d = appsAssemblyUserAuthService.getById(fId);
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
        if (extensions != null) {
            for (int i = 0; i < extensions.size(); ++i) {
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

    public void output(int code, JSONObject data) throws Exception {
        data.put("status", code);

        String callback = request.getParameter("callback");
        if (StringUtils.hasText(callback)) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            String script = data.toJSONString();
            String result = callback + "(" + script + ")";
            response.getWriter().print(result);
        } else {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().print(data.toJSONString());
        }
    }

    /**
     * 调整排序
     */
    @RequestMapping("/adjustSort")
    @ResponseBody
    public void adjustSort() throws Exception {
        JSONObject body = getPostData();
        if(body.containsKey("editList")){
            //直接保存所有需要变动的排序数据
            JSONArray editList = body.getJSONArray("editList");
            List<AppsAssemblyUserAuth> userAuthList = editList.toJavaList(AppsAssemblyUserAuth.class);
            if(!userAuthList.isEmpty()){
                userAuthList.forEach(userAuth -> {
                    appsAssemblyUserAuthService.save(userAuth);
                });
            }
        }
        result.ok();
    }
}
