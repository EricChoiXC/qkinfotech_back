package com.qkinfotech.core.sys.base.service;

import com.qkinfotech.core.org.model.OrgElement;
import com.qkinfotech.util.OrganizationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;

@Controller
@RequestMapping("/table")
public class TableService {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/test")
    public void test(){
        OrganizationUtil util = new OrganizationUtil();
        Set<OrgElement> set = util.getOrgElements("192dc059944be4b5fdb336a40d3b495b");
        System.out.println(set.size());
    }

}
