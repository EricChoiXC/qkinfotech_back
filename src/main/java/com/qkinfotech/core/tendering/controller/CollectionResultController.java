package com.qkinfotech.core.tendering.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.tendering.model.apps.collection.AppsCollectionResult;
import com.qkinfotech.core.tendering.model.apps.collection.AppsCollectionResultDetail;
import com.qkinfotech.core.tendering.model.apps.collection.AppsCollectionResultPackage;
import com.qkinfotech.core.tendering.model.apps.notice.AppsCompanyBidPackage;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeCompanyBid;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.core.tendering.model.masterModels.MasterDataFund;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.util.FileUtil;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/collection/result")
public class CollectionResultController {

    @Autowired
    protected HttpServletRequest request;
    @Autowired
    protected SimpleResult result;
    @Autowired
    protected SimpleService<AppsNoticeCompanyBid> appsNoticeCompanyBidService;
    @Autowired
    protected SimpleService<AppsCompanyBidPackage> appsCompanyBidPackageService;
    @Autowired
    protected SimpleService<AppsCollectionResultPackage> appsCollectionResultPackageService;
    @Autowired
    protected SimpleService<AppsCollectionResultDetail> appsCollectionResultDetailService;

    @Autowired
    protected SimpleService<AppsProjectPackage> appsProjectPackageService;

    @Autowired
    protected SimpleService<AppsCollectionResult> appsCollectionResultService;


    @RequestMapping("/packageList")
    public void packageList() throws Exception {

        JSONObject body = getPostData();
        String fCompanyId = body.getString("fCompanyId");
        String fProjectId = body.getString("fProjectId");
        String fNoticeId = body.getString("appsNoticeMainId");


        //query 与公司id相等
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fSupplier.fId", fCompanyId);
        queryJson.put("eq", equal);
        query.put("query", queryJson);

        List<AppsCompanyBidPackage> packageList = appsCompanyBidPackageService.findAll(JSONQuerySpecification.getSpecification(query));
        packageList.stream()
                .filter(item -> Objects.equals(item.getfNoticeMain().getfId(), fNoticeId)
                        && Objects.equals(item.getfProjectMain().getfId(), fProjectId))
                .collect(Collectors.toList());
        JSONArray packageArray = new JSONArray();
        packageList.stream().forEach(item -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fId", item.getfPackage().getfId());
            jsonObject.put("fName", item.getfPackage().getfName());
            packageArray.add(jsonObject);
        });

        result.from(packageArray);
        result.getResponse().setCharacterEncoding("UTF-8");
    }

    /**
     * 包件添加
     *
     * @return
     */
    @RequestMapping("/packageAdd")
    public void packageAdd() throws Exception {
        JSONObject body = getPostData();
        System.out.println(body);
        Specification<AppsCollectionResultPackage> spec = new Specification<AppsCollectionResultPackage>() {
            @Override
            public Predicate toPredicate(Root<AppsCollectionResultPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("appsCollectionResultDetail").get("fId"), body.getString("fId"));
            }
        };
        AppsCollectionResultPackage appsCollectionResultPackage = appsCollectionResultPackageService.findOne(spec);
        if (appsCollectionResultPackage == null) {
            appsCollectionResultPackage = new AppsCollectionResultPackage();
        }

        AppsProjectPackage fSelectedPackages = new AppsProjectPackage();


        fSelectedPackages.setfId(body.getString("fSelectedPackages"));


        AppsCollectionResultDetail appsCollectionResultDetail = new AppsCollectionResultDetail();
        appsCollectionResultDetail.setfId(body.getString("fId"));



        appsCollectionResultPackage.setAppsProjectPackage(fSelectedPackages);
        appsCollectionResultPackage.setAppsCollectionResultDetail(appsCollectionResultDetail);


        appsCollectionResultPackageService.save(appsCollectionResultPackage);

        result.ok();

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
