package com.qkinfotech.core.org.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.org.model.OrgElement;
import com.qkinfotech.core.org.model.OrgGroup;
import com.qkinfotech.core.org.model.OrgGroupMember;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;

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
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

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
}
