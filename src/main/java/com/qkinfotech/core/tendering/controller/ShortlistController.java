package com.qkinfotech.core.tendering.controller;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.org.model.OrgCompany;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.tendering.model.apps.finalization.AppsFinalizationResultPackage;
import com.qkinfotech.core.tendering.model.apps.finalization.AppsFinalizationResults;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeCompanyBid;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeMain;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.util.StringUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/project/Shortlist")
public class ShortlistController<T extends BaseEntity>{

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected SimpleService<AppsNoticeMain>  appsNoticeMainService;

    @Autowired
    protected SimpleService<AppsFinalizationResults>  appsFinalizationResultsService;

    @Autowired
    protected SimpleService<AppsNoticeCompanyBid>  appsNoticeCompanyBidService;

    @Autowired
    protected SimpleService<AppsFinalizationResultPackage>  appsFinalizationResultPackageService;
    @Autowired
    protected SimpleResult result;
    @Autowired
    private DataSourceTransactionManagerAutoConfiguration dataSourceTransactionManagerAutoConfiguration;
    @Autowired
    private Bean2Json bean2Json;
    @Autowired
    private Json2Bean json2Bean;


    /**
     * 入围名单公司
     */
    @RequestMapping("/company/list")
    @ResponseBody
    @CrossOrigin("http://localhost:3000")
    public void companyList(@RequestBody String fId) throws Exception {

        //项目Id query
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject fIdEqual = new JSONObject();
        fIdEqual.put("fProjectId.fId", fId);
        queryJson.put("eq",fIdEqual);
        query.put("query",queryJson);


        //找出所有的带有此项目id的 AppsNoticeMain记录
        List<AppsNoticeMain> noticeMains = appsNoticeMainService.findAll(JSONQuerySpecification.getSpecification(query));

        //取最新的一条
        AppsNoticeMain latestNoticeMainObj = noticeMains.stream()
                .max(Comparator.comparing(AppsNoticeMain::getfCreateTime))
                .orElse(null);
        if (latestNoticeMainObj == null) {
            result.from(new JSONObject());
            return;
        }
        //获取noticeMainId query
        String noticeMainId = latestNoticeMainObj.getfId();
        JSONObject queryNoticeMain = new JSONObject();
        JSONObject queryJsonB = new JSONObject();
        JSONObject EqualB = new JSONObject();
        EqualB.put("fMain.fId",noticeMainId);
        queryJsonB.put("eq",EqualB);
        queryNoticeMain.put("query",queryJsonB);


        //对项目id进行查找  AppsFinalizationResultPackage这个表中
        List<AppsFinalizationResultPackage> resultPackageAll = appsFinalizationResultPackageService.findAll(JSONQuerySpecification.getSpecification(query));
        //对应的公司列表
        List<AppsNoticeCompanyBid> noticeCompanyBidServiceAll = appsNoticeCompanyBidService.findAll(JSONQuerySpecification.getSpecification(queryNoticeMain));
        JSONArray companyJSON = JSONArray.parseArray(noticeCompanyBidServiceAll.toString());
        JSONArray resultPackageAllJSON = new JSONArray();
        for (int i=0; i< resultPackageAll.size(); i++) {
            resultPackageAllJSON.add(bean2Json.toJson(resultPackageAll.get(i)));
        }

        //如果无记录 返回AppsNoticeCompanyBid中的记录 公司 包件等
        //有记录 则返回AppsFinalizationResultPackage中的记录
        if (resultPackageAll.isEmpty()){
            result.from(companyJSON);
        }else {
            result.from(resultPackageAllJSON);
        }
    }


    /**
     * 新增 AppsFinalizationResultPackage
     */
    @RequestMapping("/company/save")
    @ResponseBody
    @CrossOrigin("http://localhost:3000")
    public void resultPackageSave() throws Exception {

        JSONObject postData = getPostData();
        String fProjectId = postData.getString("fProjectId");

        //删除原有的项目包件
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject fIdEqual = new JSONObject();
        fIdEqual.put("fProjectId.fId",fProjectId);
        queryJson.put("eq",fIdEqual);
        query.put("query",queryJson);
        List<AppsFinalizationResultPackage> resultPackageServiceAll = appsFinalizationResultPackageService.findAll(JSONQuerySpecification.getSpecification(query));
        resultPackageServiceAll.forEach(i->appsFinalizationResultPackageService.delete(i.getfId()));


        AppsProjectMain project=new AppsProjectMain();
        project.setfId(fProjectId);
        postData.remove("fProjectId");

        // 遍历Map以获取所有条目
        for (Map.Entry<String, Object> entry : postData.entrySet()) {
            String key = entry.getKey();
            OrgPerson company=new OrgPerson();
            company.setfId(key);
            JSONArray value = (JSONArray) entry.getValue();
            if (value != null) {
                for (int i = 0; i < value.size(); i++) {
                    AppsFinalizationResultPackage appsFinalizationResultPackage=new AppsFinalizationResultPackage();
                    appsFinalizationResultPackage.setfCompanyId(company);
                    String fPackageId = value.getString(i);
                    AppsProjectPackage fPackage = new AppsProjectPackage();
                    fPackage.setfId(fPackageId);
                    //包件id
                    appsFinalizationResultPackage.setfPackageId(fPackage);
                    //mainId
                    appsFinalizationResultPackage.setfProjectId(project);
                    appsFinalizationResultPackage.setfCreateTime(new Date());
                    appsFinalizationResultPackageService.save(appsFinalizationResultPackage);
                }
            }
        }
    }


    @RequestMapping("/updatePackageFinalization")
    @ResponseBody
    public void updatePackageFinalization () throws Exception {
        JSONObject json = getPostData();
        String projId = json.getString("projId");
        String packId = json.getString("packId");
        List<String> compIds = json.getList("compIds", String.class);

        if (StringUtil.isNull(packId) || StringUtil.isNull(projId)) {
            response.getWriter().write("未提供包件或项目");
            return;
        }
        //要删除的入围结果
        Specification<AppsFinalizationResultPackage> specification = new Specification<AppsFinalizationResultPackage>() {
            @Override
            public Predicate toPredicate(Root<AppsFinalizationResultPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate projPre = criteriaBuilder.equal(root.get("fProjectId").get("fId"), projId);
                Predicate packPre = criteriaBuilder.equal(root.get("fPackageId").get("fId"), packId);
                return criteriaBuilder.and(projPre, packPre);
            }
        };
        List<AppsFinalizationResultPackage> list = appsFinalizationResultPackageService.findAll(specification);
        list.stream().forEach(o -> {
            if (compIds.indexOf(o.getfCompanyId().getfId()) >= 0) {
                compIds.remove(compIds.indexOf(o.getfCompanyId().getfId()));
            } else {
                appsFinalizationResultPackageService.delete(o);
            }
        });

        System.out.println("add : " + compIds.size());
        for (String company : compIds) {
            AppsFinalizationResultPackage pack = new AppsFinalizationResultPackage();
            OrgPerson orgPerson = new OrgPerson();
            orgPerson.setfId(company);
            pack.setfCompanyId(orgPerson);
            AppsProjectMain proj = new AppsProjectMain();
            proj.setfId(projId);
            pack.setfProjectId(proj);
            AppsProjectPackage app = new AppsProjectPackage();
            app.setfId(packId);
            pack.setfPackageId(app);
            pack.setfCreateTime(new Date());
            appsFinalizationResultPackageService.save(pack);
        }

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

}
