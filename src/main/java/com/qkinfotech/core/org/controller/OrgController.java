package com.qkinfotech.core.org.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.org.model.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping
public class OrgController {

    @Autowired
    private SimpleService<OrgGroup> orgGroupService;

    @Autowired
    private SimpleService<OrgGroupMember> orgGroupMemberService;

    @Autowired
    private SimpleService<OrgElement> orgElementService;

    @Autowired
    private SimpleService<OrgPerson> orgPersonService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Autowired
    protected SimpleResult result;

    @Autowired
    protected Bean2Json bean2json;

    @Autowired
    protected Json2Bean json2bean;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionStatus status = null;

    @PostMapping("/orgGroup/save")
    @ResponseBody
    public void orgGroupSave() throws Exception {
        JSONObject result = new JSONObject();
        try {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
            status = transactionManager.getTransaction(def);

            JSONObject body = getPostData();
            String fId = body.getString("fId");
            OrgGroup group = orgGroupService.getById(fId);
            if (group == null) {
                group = new OrgGroup();
                group.setfId(fId);
            }
            group.setfName(body.getString("fName"));
            group.setfType(OrgElement.TYPE_GROUP);
            group.setfValid(true);
            orgGroupService.save(group);
            status.flush();

            Specification<OrgGroupMember> specification = new Specification<OrgGroupMember>() {
                @Override
                public Predicate toPredicate(Root<OrgGroupMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.equal(root.get("fGroup").get("fId"), fId);
                }
            };
            orgGroupMemberService.delete(specification);
            status.flush();

            JSONArray members = body.getJSONArray("fMembers");
            if (!members.isEmpty()) {
                for (int i = 0; i < members.size(); i++) {
                    JSONObject memberJson = members.getJSONObject(i);
                    OrgElement element = orgElementService.getById(memberJson.getString("fId"));
                    if (element != null) {
                        OrgGroupMember member = new OrgGroupMember();
                        member.setfGroup(group);
                        member.setfElement(element);
                        member.setfManager(false);
                        orgGroupMemberService.save(member);
                    }
                }
            }
            transactionManager.commit(status);
            result.put("result", true);
            result.put("status", 200);
        } catch(Exception e) {
            e.printStackTrace();
            transactionManager.rollback(status);
            result.put("result", false);
            result.put("status", 302);
        } finally {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().print(result.toJSONString());
        }
    }

    @PostMapping("/orgGroup/list")
    @ResponseBody
    public void orgGroupList() throws Exception {
        /*群组列出方法，针对部门群组，公共群组，公司群组进行特定查询的list调用*/
        JSONObject body = getPostData();
        String key = body.getString("fKey");
        String userId = "";
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("id")) {
                userId = cookie.getValue();
            }
        }
        OrgPerson person = orgPersonService.getById(userId);
        if (person != null) {
            List<OrgGroup> list = new ArrayList<>();
            if ("dept".equals(key)) {
                Set<OrgDept> deptSet = getPersonDept(person);
                Specification<OrgGroup> specification = new Specification<OrgGroup>() {
                    @Override
                    public Predicate toPredicate(Root<OrgGroup> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        CriteriaBuilder.In in = criteriaBuilder.in(root.get("fOwner").get("fId"));
                        CriteriaBuilder.In in2 = criteriaBuilder.in(root.get("fGroupCate").get("fOwner").get("fId"));
                        for (OrgDept dept : deptSet) {
                            in.value(dept.getfId());
                            in2.value(dept.getfId());
                        }
                        return criteriaBuilder.or(in, in2);
                    }
                };
                list = orgGroupService.findAll(specification);
            } else if ("company".equals(key)) {
                Set<OrgCompany> companySet = getPersonCompany(person);
                Specification<OrgGroup> specification = new Specification<OrgGroup>() {
                    @Override
                    public Predicate toPredicate(Root<OrgGroup> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        CriteriaBuilder.In in = criteriaBuilder.in(root.get("fOwner").get("fId"));
                        CriteriaBuilder.In in2 = criteriaBuilder.in(root.get("fGroupCate").get("fOwner").get("fId"));
                        for (OrgCompany company : companySet) {
                            in.value(company.getfId());
                            in2.value(company.getfId());
                        }
                        return criteriaBuilder.or(in, in2);
                    }
                };
                list = orgGroupService.findAll(specification);
            }
            JSONArray listJson = new JSONArray();
            list.forEach(orgGroup -> listJson.add(bean2json.toJson(orgGroup)));
            result.from(listJson);
        }
    }

    private JSONObject getPostData() {
        JSONObject data = new JSONObject();
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
        return data;
    }

    public Set<OrgDept> getPersonDept(OrgPerson person) {
        Set<OrgDept> deptSet = new HashSet<>();
        if (person != null && person.getfParent() != null) {
            OrgDept curDept = person.getfParent();
            deptSet.add(curDept);
            while(curDept.getfParent() != null) {
                curDept = curDept.getfParent();
                deptSet.add(curDept);
            }
        }
        return deptSet;
    }

    public Set<OrgCompany> getPersonCompany(OrgPerson person) {
        Set<OrgCompany> companySet = new HashSet<>();
        if (person != null && person.getfParent() != null) {
            OrgDept curDept = person.getfParent();
            while(curDept.getfParent() != null) {
                curDept = curDept.getfParent();
            }
            OrgCompany curCompany = curDept.getfCompany();
            while(curCompany != null) {
                companySet.add(curCompany);
                curCompany = curCompany.getfParent();
            }
        }
        return companySet;
    }
}
