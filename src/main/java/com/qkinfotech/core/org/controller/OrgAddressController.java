package com.qkinfotech.core.org.controller;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.mvc.util.QueryBuilder;
import com.qkinfotech.core.org.model.*;
import com.qkinfotech.core.org.utils.Bean2JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Controller
@RequestMapping("/orgAddress")
@Slf4j
public class OrgAddressController {

    @Autowired
    private SimpleService<OrgElement> orgElementService;

    @Autowired
    private SimpleService<OrgCompany> orgCompanyService;

    @Autowired
    private SimpleService<OrgDept> orgDeptService;

    @Autowired
    private SimpleService<OrgPerson> orgPersonService;

    @Autowired
    private SimpleService<OrgGroup> orgGroupService;

    @Autowired
    private SimpleService<OrgGroupMember> orgGroupMemberService;

    @Autowired
    private SimpleService<OrgPost> orgPostService;

    @Autowired
    private SimpleService<OrgPostMember> orgPostMemberService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Autowired
    ApplicationContext context;

    @Autowired
    protected SimpleResult result;

    @Autowired
    protected Bean2JsonUtil bean2json;

    @RequestMapping("/companyList")
    @ResponseBody
    public void companyList() throws Exception {
        JSONObject body = getPostData();
        QueryBuilder<OrgCompany> qb = QueryBuilder.parse(OrgCompany.class, body);
        Page<OrgCompany> data = orgCompanyService.findAll(qb.specification(), qb.pageable());
        if (body.containsKey("pagesize")) {
            int pagesize = body.getIntValue("pagesize");
            if (pagesize > 500 || pagesize < 15) {
                pagesize = 15;
            }
            int pagenum = body.getIntValue("pagenum");
            if (pagenum < 0) {
                pagenum = 0;
            }
            if (pagesize * pagenum >= data.getTotalElements()) {
                pagenum = data.getTotalPages() - 1;
                if (pagenum < 0) {
                    pagenum = 0;
                }
                qb.setPageable(PageRequest.of(pagenum, pagesize, qb.sort()));
                data = orgCompanyService.findAll(qb.specification(), qb.pageable());
            }
        }
        Page<JSONObject> out = data.map(e -> bean2json.toJson(e));
        result.from(out);
    }

    @RequestMapping("/deptList")
    @ResponseBody
    public void deptList() throws Exception {
        JSONObject body = getPostData();
        QueryBuilder<OrgDept> qb = QueryBuilder.parse(OrgDept.class, body);
        Page<OrgDept> data = orgDeptService.findAll(qb.specification(), qb.pageable());
        if (body.containsKey("pagesize")) {
            int pagesize = body.getIntValue("pagesize");
            if (pagesize > 500 || pagesize < 15) {
                pagesize = 15;
            }
            int pagenum = body.getIntValue("pagenum");
            if (pagenum < 0) {
                pagenum = 0;
            }
            if (pagesize * pagenum >= data.getTotalElements()) {
                pagenum = data.getTotalPages() - 1;
                if (pagenum < 0) {
                    pagenum = 0;
                }
                qb.setPageable(PageRequest.of(pagenum, pagesize, qb.sort()));
                data = orgDeptService.findAll(qb.specification(), qb.pageable());
            }
        }
        Page<JSONObject> out = data.map(e -> bean2json.toJson(e));
        result.from(out);
    }

    @RequestMapping("/personList")
    @ResponseBody
    public void personList() throws Exception {
        Date start = new Date();
        JSONObject body = getPostData();
        QueryBuilder<OrgPerson> qb = QueryBuilder.parse(OrgPerson.class, body);
        Page<OrgPerson> data = orgPersonService.findAll(qb.specification(), qb.pageable());
        if (body.containsKey("pagesize")) {
            int pagesize = body.getIntValue("pagesize");
            if (pagesize > 500 || pagesize < 15) {
                pagesize = 15;
            }
            int pagenum = body.getIntValue("pagenum");
            if (pagenum < 0) {
                pagenum = 0;
            }
            if (pagesize * pagenum >= data.getTotalElements()) {
                pagenum = data.getTotalPages() - 1;
                if (pagenum < 0) {
                    pagenum = 0;
                }
                qb.setPageable(PageRequest.of(pagenum, pagesize, qb.sort()));
                data = orgPersonService.findAll(qb.specification(), qb.pageable());
            }
        }
        Date finSearch = new Date();
        //logger.info("search " + (finSearch.getTime() - start.getTime()));
        data.forEach(e -> {
            System.out.println(e.getfName());
        });
        Page<JSONObject> out = data.map(e -> bean2json.toJson(e));
        //logger.info("tojson " + (new Date().getTime() - finSearch.getTime()));
        result.from(out);
    }

    @RequestMapping("/postList")
    @ResponseBody
    public void postList() throws Exception {
        JSONObject body = getPostData();
        QueryBuilder<OrgPost> qb = QueryBuilder.parse(OrgPost.class, body);
        Page<OrgPost> data = orgPostService.findAll(qb.specification(), qb.pageable());
        if (body.containsKey("pagesize")) {
            int pagesize = body.getIntValue("pagesize");
            if (pagesize > 500 || pagesize < 15) {
                pagesize = 15;
            }
            int pagenum = body.getIntValue("pagenum");
            if (pagenum < 0) {
                pagenum = 0;
            }
            if (pagesize * pagenum >= data.getTotalElements()) {
                pagenum = data.getTotalPages() - 1;
                if (pagenum < 0) {
                    pagenum = 0;
                }
                qb.setPageable(PageRequest.of(pagenum, pagesize, qb.sort()));
                data = orgPostService.findAll(qb.specification(), qb.pageable());
            }
        }
        Page<JSONObject> out = data.map(e -> bean2json.postToJson(e));
        result.from(out);
    }

    @RequestMapping("/groupList")
    @ResponseBody
    public void groupList() throws Exception {
        JSONObject body = getPostData();
        QueryBuilder<OrgGroup> qb = QueryBuilder.parse(OrgGroup.class, body);
        Page<OrgGroup> data = orgGroupService.findAll(qb.specification(), qb.pageable());
        if (body.containsKey("pagesize")) {
            int pagesize = body.getIntValue("pagesize");
            if (pagesize > 500 || pagesize < 15) {
                pagesize = 15;
            }
            int pagenum = body.getIntValue("pagenum");
            if (pagenum < 0) {
                pagenum = 0;
            }
            if (pagesize * pagenum >= data.getTotalElements()) {
                pagenum = data.getTotalPages() - 1;
                if (pagenum < 0) {
                    pagenum = 0;
                }
                qb.setPageable(PageRequest.of(pagenum, pagesize, qb.sort()));
                data = orgGroupService.findAll(qb.specification(), qb.pageable());
            }
        }
        Page<JSONObject> out = data.map(e -> bean2json.groupToJson(e));
        result.from(out);
    }

    private JSONObject getPostData() {
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

}
