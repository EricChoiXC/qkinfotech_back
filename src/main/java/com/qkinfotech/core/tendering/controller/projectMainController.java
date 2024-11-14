package com.qkinfotech.core.tendering.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.file.FileManager;
import com.qkinfotech.core.file.SysFile;
import com.qkinfotech.core.file.SysFileInputStream;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.IEntityExtension;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.mvc.util.QueryBuilder;
import com.qkinfotech.core.org.model.OrgDept;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.tendering.model.apps.finalization.AppsFinalizationResultPackage;
import com.qkinfotech.core.tendering.model.apps.project.*;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import com.qkinfotech.core.tendering.model.masterModels.*;
import com.qkinfotech.core.tendering.service.FileMainService;
import com.qkinfotech.core.tendering.service.ProjectMainUtilService;
import com.qkinfotech.core.user.SysRoleService;
import com.qkinfotech.util.SqlUtil;
import com.qkinfotech.util.StringUtil;
import com.qkinfotech.util.UrlMappingUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/projectNo/applicationa")
@Slf4j
public class projectMainController<T extends BaseEntity> {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected SimpleResult result;

    @Autowired
    protected Json2Bean json2bean;

    @Autowired
    protected SimpleService<AppsProjectMain> appsProjectMainService;

    @Autowired
    protected SimpleService<AppsProjectTag> appsProjectTagService;

    @Autowired
    protected SimpleService<AppsProjectType> appsProjectTypeService;

    @Autowired
    protected SimpleService<AppsProjectNatures> appsProjectNaturesService;

    @Autowired
    protected SimpleService<AppsProjectMembers> appsProjectMembersService;

    @Autowired
    protected SimpleService<MasterDataCountry> masterDataCountryService;

    @Autowired
    protected SimpleService<AppsProjectDocumentation> appsProjectDocumentationService;

    @Autowired
    protected SimpleService<AppsFinalizationResultPackage> appsFinalizationResultPackageService;

    @Autowired
    protected SimpleService<MasterDataFund> masterDataFundService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    protected Bean2Json bean2json;

    @Autowired(required = false)
    protected List<IEntityExtension> extensions;

    protected Class<AppsProjectMain> modelClass = AppsProjectMain.class;

    protected String chnName = "中国";

    protected String chnNum = "156";

    @Autowired
    protected ProjectMainUtilService projectMainUtilService;

    @Autowired
    protected SimpleService<MasterDataType> masterDataTypeService;

    @Autowired
    protected SimpleService<MasterDataScale> masterDataScaleService;

    @Autowired
    protected SimpleService<AppsProjectScale> appsProjectScaleService;

    @Autowired
    private UrlMappingUtil urlMappingUtil;
    @Autowired
    protected FileMainService fileMainService;
    @Autowired
    protected FileManager fileManager;

    private String companyImportanceName = "公司重点";

    /**
     * 项目编号申请
     *
     * @throws Exception
     */
    @RequestMapping("/save")
    @ResponseBody

    public void save() throws Exception {
        JSONObject body = getPostData();
        String fId = body.getString("fId");

        AppsProjectMain main = new AppsProjectMain();
        main.setfId(fId);

        // 部门
        OrgDept dept = body.getObject("fDept", OrgDept.class);
        main.setfDept(dept);
        //项目经理
        OrgPerson fDeptManager = body.getObject("fDeptManager", OrgPerson.class);
        main.setfDeptManager(fDeptManager);
        //业主
        main.setfOwner(body.getString("fOwner"));

        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fMainId.fId", fId);
        queryJson.put("eq", equal);
        query.put("query", queryJson);

        //项目名称
        main.setfName(body.getString("fName"));
        //项目简称
        main.setfSimpleName(body.getString("fSimpleName"));
        //项目英文名称
        main.setfEngName(body.getString("fEngName"));

        //资金来源
        /*MasterDataFund masterDataFund = body.getObject("fCapitalSource", MasterDataFund.class);
        masterDataFund = masterDataFundService.getById(masterDataFund.getfId());
        main.setfCapitalSource(masterDataFund);
        if (StringUtil.isNotNull(body.getString("fCapitalSource")) && "其他".equals(masterDataFund.getfName())) {
            main.setfCapitalSourceOther(body.getString("fCapitalSourceOther"));
        }*/

        //开拓信息
        main.setfDevelopInfo(body.getString("fDevelopInfo"));

        //协议编号
        main.setfProtocolNumber(body.getString("fProtocolNumber"));

        //流程id
        main.setfReviewId(body.getString("fReviewId"));

        //项目编号
        main.setfProtocolNo(body.getString("fProtocolNo"));

        //当前处理人
        main.setfCurrentProcessorId(body.getString("fCurrentProcessorId"));

        //审核状态
        main.setfAuditStatus(body.getString("fAuditStatus"));

        //项目备注
        main.setfNotes(body.getString("fNotes"));

        //项目执行地
        MasterDataCountry country = body.getObject("fCountry", MasterDataCountry.class);
//        masterDataCountryService.getById(body.getString("fCountry"))

        main.setfExecutionCountry(country);
        if (chnNum.equals(country.getfCountryNum()) || chnName.equals(country.getfName())) {
            MasterDataCity city = body.getObject("fCity", MasterDataCity.class);
            if (StringUtil.isNotNull(city.getfId())) {
                main.setfExecutionCity(city);
            }
        }

        //预期收入
        main.setfPlanIncome(body.getFloat("fPlanIncome"));
        //是否涉密
        main.setfIsClassified(body.getBoolean("fIsClassified"));
        //计划用汇
        // main.setfPlanForeignExchange(body.getFloat("fPlanForeignExchange"));
        //项目预算
        main.setfProjectBudget(body.getFloat("fProjectBudget"));

        //类别
        main.setfProjectCategory(body.getObject("projectCategory", MasterDataCategory.class));

        //项目重要性
        main.setfProjectImportance(body.getObject("selectedImportance", MasterDataImportance.class));
        //项目模式
        main.setfProjectMode(body.getObject("fProjectMode", MasterDataMode.class));

        //其他项目类型、其他项目标签
        main.setfTypeOther(body.getString("fTypeOther"));
        main.setfTagOther(body.getString("fTagOther"));

        //送审时间
        if (Objects.equals(body.getString("fAuditStatus"), "0")) {
            main.setfSubmittalTime(new Date());
        }
//        else if (Objects.equals(body.getString("fAuditStatus"), "1")) {
//            main.setfQualifyTime(new Date());
//        }

        //项目地点
        main.setfProjectPlace(body.getString("fProjectPlace"));
        main.setfCreateTime(new Date());
        main.setfPublishTime(new Date());

        JSONArray natureArray = body.getJSONArray("selectedNatures");
        for (int i = 0; i < natureArray.size(); i++) {
            JSONObject jsonObject = natureArray.getJSONObject(i);
            MasterDataNature masterDataNature = jsonObject.to(MasterDataNature.class);
            if (masterDataNature.getfName().equals("其他") && StringUtil.isNotNull(body.getString("fProjectNatureOther"))) {
                main.setfProjectNatureOther(body.getString("fProjectNatureOther"));
            }
        }

        appsProjectMainService.save(main);

        //删除 之前的项目组成员
        List<AppsProjectMembers> savedMembers = appsProjectMembersService.findAll(JSONQuerySpecification.getSpecification(query));
        if (!savedMembers.isEmpty()) {
            for (AppsProjectMembers savedMember : savedMembers) {
                appsProjectMembersService.delete(savedMember.getfId());
            }
        }
        // 添加
        JSONArray memberArray = body.getJSONArray("fProjectMembers");
        for (int i = 0; i < memberArray.size(); i++) {
            JSONObject jsonObject = memberArray.getJSONObject(i);
            OrgPerson orgPerson = jsonObject.to(OrgPerson.class);
            AppsProjectMembers appsProjectMembers = new AppsProjectMembers();
            appsProjectMembers.setfPersonId(orgPerson);
            appsProjectMembers.setfMainId(main);
            appsProjectMembersService.save(appsProjectMembers);
        }


        //删除 之前的项目性质数据
        List<AppsProjectNatures> savedNatures = appsProjectNaturesService.findAll(JSONQuerySpecification.getSpecification(query));
        if (!savedNatures.isEmpty()) {
            for (AppsProjectNatures savedNature : savedNatures) {
                appsProjectNaturesService.delete(savedNature.getfId());
            }
        }
        //项目性质
        for (int i = 0; i < natureArray.size(); i++) {
            JSONObject jsonObject = natureArray.getJSONObject(i);
            MasterDataNature masterDataNature = jsonObject.to(MasterDataNature.class);
            AppsProjectNatures appsProjectNatures = new AppsProjectNatures();
            appsProjectNatures.setfNatureId(masterDataNature);
            appsProjectNatures.setfMainId(main);
            appsProjectNaturesService.save(appsProjectNatures);
        }


        //删除原有的的标签数据
        List<AppsProjectTag> savedTags = appsProjectTagService.findAll(JSONQuerySpecification.getSpecification(query));
        for (AppsProjectTag savedTag : savedTags) {
            appsProjectTagService.delete(savedTag.getfId());
        }
        //项目标签
        JSONArray jsonArray = JSONArray.parseArray(body.get("selectedTags").toString());
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            MasterDataTag masterDataTag = jsonObject.to(MasterDataTag.class);
            AppsProjectTag appsProjectTag = new AppsProjectTag();
            appsProjectTag.setfTagId(masterDataTag);
            appsProjectTag.setfMainId(main);
            appsProjectTagService.save(appsProjectTag);
        }

        //项目类型删除
        List<AppsProjectType> savedTypes = appsProjectTypeService.findAll(JSONQuerySpecification.getSpecification(query));
        for (AppsProjectType savedType : savedTypes) {
            appsProjectTypeService.delete(savedType.getfId());
        }
        //项目类型
        JSONArray typeScaleArray = JSONArray.parseArray(body.get("fTypes").toString());
        for (Object obj : typeScaleArray) {
            JSONObject typeJson = (JSONObject) obj;
            AppsProjectType appsProjectType = new AppsProjectType();
            appsProjectType.setfMainId(main);
            appsProjectType.setfTypeId(masterDataTypeService.getById(typeJson.getJSONObject("fTypeId").getString("fId")));
            appsProjectType.setfGroup(typeJson.getString("fGroup"));
            appsProjectTypeService.save(appsProjectType);
        }

        //项目规模
        JSONObject scaleQuery = new JSONObject();
        JSONObject scaleJson = new JSONObject();
        JSONObject equal2 = new JSONObject();
        equal2.put("fMain.fId", fId);
        scaleJson.put("eq", equal2);
        scaleQuery.put("query", scaleJson);
        List<AppsProjectScale> savedScales = appsProjectScaleService.findAll(JSONQuerySpecification.getSpecification(scaleQuery));
        for (AppsProjectScale scale : savedScales) {
            appsProjectScaleService.delete(scale.getfId());
        }
        JSONArray scaleArray = JSONArray.parseArray(body.get("fScales").toString());
        for (Object obj : scaleArray) {
            JSONObject scaleJson2 = (JSONObject) obj;
            AppsProjectScale appsProjectScale = new AppsProjectScale();
            appsProjectScale.setfMain(main);
            appsProjectScale.setfScale(masterDataScaleService.getById(scaleJson2.getJSONObject("fScale").getString("fId")));
            appsProjectScale.setfGroup(scaleJson2.getString("fGroup"));
            appsProjectScale.setfValue(scaleJson2.getString("fValue"));
            appsProjectScaleService.save(appsProjectScale);
        }


        result.ok();

    }

    /**
     * 待审项目一览
     */
    @RequestMapping("/projectView")
    @ResponseBody

    public void projectView(@RequestParam String fId) throws Exception {
        AppsProjectMain projectMain = appsProjectMainService.getById(fId);
        JSONObject appsProjectMain = JSONObject.from(projectMain);

        //部门
        JSONObject deptJson = new JSONObject();
        deptJson.put("fId", projectMain.getfDept().getfId());
        deptJson.put("fName", projectMain.getfDept().getfName());
        appsProjectMain.put("fDept", deptJson);

        //项目经理
        JSONObject deptManagerJson = new JSONObject();
        deptManagerJson.put("fId", projectMain.getfDeptManager().getfId());
        deptManagerJson.put("fName", projectMain.getfDeptManager().getfName());
        appsProjectMain.put("fDeptManager", deptManagerJson);

        //项目组成员
        JSONArray deptMemberArray = new JSONArray();
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fMainId.fId", fId);
        queryJson.put("eq", equal);
        query.put("query", queryJson);
        for (AppsProjectMembers appsProjectMembers : appsProjectMembersService.findAll(JSONQuerySpecification.getSpecification(query))) {
            JSONObject deptMemberJson = new JSONObject();
            deptMemberJson.put("fId", appsProjectMembers.getfPersonId().getfId());
            deptMemberJson.put("fName", appsProjectMembers.getfPersonId().getfName());
            deptMemberArray.add(deptMemberJson);
        }
        appsProjectMain.put("fProjectMembers", deptMemberArray);

        //项目重要性id
        String importanceId = projectMain.getfProjectImportance().getfId();
        //项目模式id
        String modeId = projectMain.getfProjectMode().getfId();

        //项目资金来源id
//        String fundId = projectMain.getfCapitalSource().getfId();
        String fcityVal = "";
        //项目执行地
        if (projectMain.getfExecutionCity() != null) {
            fcityVal = projectMain.getfExecutionCity().getfId();
        }
        String fCountryVal = projectMain.getfExecutionCountry().getfId();


        //项目标签id列表
        JSONObject tagQuery = new JSONObject();
        JSONObject tagQueryJson = new JSONObject();
        JSONObject tagEqual = new JSONObject();
        tagEqual.put("fMainId.fId", fId);
        tagQueryJson.put("eq", tagEqual);
        tagQuery.put("query", tagQueryJson);
        List<AppsProjectTag> tagIdList = appsProjectTagService.findAll(JSONQuerySpecification.getSpecification(tagQuery));
        JSONArray appsTagArray = new JSONArray();
        JSONArray appsTagNameArray = new JSONArray();
        for (AppsProjectTag tag : tagIdList) {
            appsTagArray.add(tag.getfTagId().getfId());
            appsTagNameArray.add(tag.getfTagId().getfName());
        }
        appsProjectMain.put("fId", fId);
        appsProjectMain.put("selectedTag", appsTagArray);
        appsProjectMain.put("appsTagNameArray", appsTagNameArray);
        appsProjectMain.put("projectImportance", importanceId);
        appsProjectMain.put("projectMode", modeId);
//        appsProjectMain.put("capitalSource", fundId);
        appsProjectMain.put("fcityVal", fcityVal);
        appsProjectMain.put("fCountryVal", fCountryVal);
        appsProjectMain.put("fCountry", projectMain.getfExecutionCountry());
        appsProjectMain.put("fCity", projectMain.getfExecutionCity());
        //项目编号
        appsProjectMain.put("fProtocolNo", projectMain.getfProtocolNo());

        List<AppsProjectNatures> natures = appsProjectNaturesService.findAll(JSONQuerySpecification.getSpecification(query));
        JSONArray appsNatureArray = new JSONArray();//项目性质
        JSONArray appsNatureNameArray = new JSONArray();//项目性质
        JSONArray oaNatureArray = new JSONArray();//项目性质


        for (AppsProjectNatures nature : natures) {
            appsNatureArray.add(nature.getfNatureId().getfId());
            appsNatureNameArray.add(nature.getfNatureId().getfName());
            oaNatureArray.add(nature.getfNatureId());

        }
        if (StringUtil.isNotNull(projectMain.getfProjectNatureOther())) {
            appsNatureNameArray.add(projectMain.getfProjectNatureOther());
        }
        appsProjectMain.put("selectedNature", appsNatureArray);
        appsProjectMain.put("appsNatureNameArray", appsNatureNameArray);
        appsProjectMain.put("oaNatureNameArray", oaNatureArray);


        //项目類型id列表
        JSONObject typeQuery = new JSONObject();
        JSONObject typeQueryJson = new JSONObject();
        JSONObject typeEqual = new JSONObject();
        typeEqual.put("fMainId.fId", fId);
        typeQueryJson.put("eq", typeEqual);
        typeQuery.put("query", typeQueryJson);
        List<AppsProjectType> typeList = appsProjectTypeService.findAll(JSONQuerySpecification.getSpecification(typeQuery));
        ArrayList<String> list = new ArrayList<>();
        for (AppsProjectType appsProjectType : typeList) {
            list.add(appsProjectType.getfTypeId().getfId());
        }
        List<String> types = list.stream().distinct().toList();
        for (String type : types) {

        }
        appsProjectMain.put("fProjectType", list);
        appsProjectMain.put("fTypes", typeList);

        JSONArray appsTypeArray = new JSONArray();

        for (String type : types) {
            JSONArray appsScaleArray = new JSONArray();
            for (AppsProjectType appsProjectType : typeList) {

                if (appsProjectType.getfTypeId().getfId().equals(type)) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("fMainId.fId", fId);
                    jsonObject.put("fMasterDataTypeId", appsProjectType.getfTypeId());
                    jsonObject.put("fMasterDataScaleId", appsProjectType.getfScaleId());
                    jsonObject.put("value", appsProjectType.getfValue());
                    appsScaleArray.add(jsonObject);
                }
            }
            appsTypeArray.add(appsScaleArray);
        }

        appsProjectMain.put("typeScales", appsTypeArray);

        String jsonString = JSON.toJSONString(types);
        JSONArray selectedType = JSONArray.parseArray(String.valueOf(jsonString));


        appsProjectMain.put("selectedType", selectedType);

        Specification<AppsProjectScale> specification = new Specification<AppsProjectScale>() {
            @Override
            public Predicate toPredicate(Root<AppsProjectScale> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("fMain").get("fId"), fId);
            }
        };
        List<AppsProjectScale> fScales = appsProjectScaleService.findAll(specification);
        appsProjectMain.put("fScales", fScales);


        //协议编号
        appsProjectMain.put("fProtocolNumber", projectMain.getfProtocolNumber());
        //开拓信息
        appsProjectMain.put("fDevelopInfo", projectMain.getfDevelopInfo());

        JSONObject documentationQuery = new JSONObject();
        //构建参数
        SqlUtil.setParameter(documentationQuery, "fProjectId.fId", fId, "query", "eq");
        //归档信息
        AppsProjectDocumentation projectDocumentation = appsProjectDocumentationService.findOne(JSONQuerySpecification.getSpecification(documentationQuery));
        if (projectDocumentation != null) {
            appsProjectMain.put("projectDocumentation", projectDocumentation);
        }
        result.from(appsProjectMain);
    }


    @RequestMapping("/list")
    @ResponseBody

    public void list() throws Exception {
        JSONObject body = getPostData("list");
        body.put("distinct", "");

        QueryBuilder<AppsProjectMain> qb = QueryBuilder.parse(modelClass, body);

        Page<AppsProjectMain> data = appsProjectMainService.findAll(qb.specification(), qb.pageable());

        Page<JSONObject> out = data.map(e -> bean2json.toJson(e));
        result.from(out);
    }

    /**
     * 管理员修改项目
     *
     * @throws Exception
     */
    @RequestMapping("/admin/edit")
    @ResponseBody

    public void adminEdit() throws Exception {
        JSONObject body = getPostData();
        String fId = body.getString("fId");
        AppsProjectMain main = appsProjectMainService.getById(fId);

        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fMainId.fId", fId);
        queryJson.put("eq", equal);
        query.put("query", queryJson);
        //项目经理
        OrgPerson fDeptManager = body.getObject("fDeptManager", OrgPerson.class);
        main.setfDeptManager(fDeptManager);
        //项目名称
        main.setfName(body.getString("fName"));
        //项目简称
        main.setfSimpleName(body.getString("fSimpleName"));
        //项目英文名称
        main.setfEngName(body.getString("fEngName"));
        //项目编号
        main.setfProtocolNo(body.getString("fProtocolNo"));
        //预期收入
        main.setfPlanIncome(body.getFloat("fPlanIncome"));
        //计划用汇
        main.setfPlanForeignExchange(body.getFloat("fPlanForeignExchange"));
        //项目性质
        JSONArray natureArray = body.getJSONArray("selectedNatures");
        for (int i = 0; i < natureArray.size(); i++) {
            JSONObject jsonObject = natureArray.getJSONObject(i);
            MasterDataNature masterDataNature = jsonObject.to(MasterDataNature.class);
            if (masterDataNature.getfName().equals("其他") && StringUtil.isNotNull(body.getString("fProjectNatureOther"))) {
                main.setfProjectNatureOther(body.getString("fProjectNatureOther"));
            }
        }
        appsProjectMainService.save(main);

        //删除 之前的项目组成员
        List<AppsProjectMembers> savedMembers = appsProjectMembersService.findAll(JSONQuerySpecification.getSpecification(query));
        if (!savedMembers.isEmpty()) {
            for (AppsProjectMembers savedMember : savedMembers) {
                appsProjectMembersService.delete(savedMember.getfId());
            }
        }
        // 添加
        JSONArray memberArray = body.getJSONArray("fProjectMembers");
        for (int i = 0; i < memberArray.size(); i++) {
            JSONObject jsonObject = memberArray.getJSONObject(i);
            OrgPerson orgPerson = jsonObject.to(OrgPerson.class);
            AppsProjectMembers appsProjectMembers = new AppsProjectMembers();
            appsProjectMembers.setfPersonId(orgPerson);
            appsProjectMembers.setfMainId(main);
            appsProjectMembersService.save(appsProjectMembers);
        }
        //删除 之前的项目性质数据
        List<AppsProjectNatures> savedNatures = appsProjectNaturesService.findAll(JSONQuerySpecification.getSpecification(query));
        if (!savedNatures.isEmpty()) {
            for (AppsProjectNatures savedNature : savedNatures) {
                appsProjectNaturesService.delete(savedNature.getfId());
            }
        }
        //项目性质
        for (int i = 0; i < natureArray.size(); i++) {
            JSONObject jsonObject = natureArray.getJSONObject(i);
            MasterDataNature masterDataNature = jsonObject.to(MasterDataNature.class);
            AppsProjectNatures appsProjectNatures = new AppsProjectNatures();
            appsProjectNatures.setfNatureId(masterDataNature);
            appsProjectNatures.setfMainId(main);
            appsProjectNaturesService.save(appsProjectNatures);
        }
        //修改归档信息
        JSONObject projectDocumentationJson = body.getJSONObject("projectDocumentation");
        if (null != projectDocumentationJson) {
            AppsProjectDocumentation projectDocumentation = body.getObject("projectDocumentation", AppsProjectDocumentation.class);
            appsProjectDocumentationService.save(projectDocumentation);
        }
        result.ok();

    }

    /**
     * 根据用户id查询用户是否是管理员
     *
     * @throws Exception
     */
    @RequestMapping("/check/user")
    @ResponseBody

    public void checkIsAdminRole() throws Exception {
        JSONObject body = getPostData();
        String userId = body.getString("fId");
        boolean isAdmin = sysRoleService.checkIsAdminRole(userId);
        JSONObject query = new JSONObject();
        query.put("isAdmin", isAdmin);
        result.from(query);
    }


    @RequestMapping("/check/project/nomination")
    @ResponseBody

    public void checkProjectNominationResults() throws Exception {
        JSONObject body = getPostData();
        String projectId = body.getString("fId");
        //获取项目所有入围结果
        Specification<AppsFinalizationResultPackage> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("fProjectId").get("fId"), projectId);
        List<AppsFinalizationResultPackage> allResults = appsFinalizationResultPackageService.findAll(spec);
        JSONObject resultData = new JSONObject();
        if (!allResults.isEmpty()) {
            resultData.put("isOk", true);
        } else {
            resultData.put("isOk", false);
        }
        result.from(resultData);
    }

    @RequestMapping("/edit/project/start")
    @ResponseBody

    public void editProjectNoStart() throws Exception {
        JSONObject body = getPostData();
        String projectId = body.getString("fId");
        boolean isStart = body.getBoolean("isStart");
        AppsProjectMain projectMain = appsProjectMainService.getById(projectId);
        if (null != projectMain) {
            projectMain.setfIsProjectStart(isStart);
            appsProjectMainService.save(projectMain);
        } else {
            throw new Exception("未查询到项目，更新失败");
        }
        result.ok();
    }

    @RequestMapping("/check/project/start")
    @ResponseBody

    public void checkProjectNoStart() throws Exception {
        JSONObject body = getPostData();
        String projectId = body.getString("fId");
        AppsProjectMain projectMain = appsProjectMainService.getById(projectId);
        JSONObject resultData = new JSONObject();
        if (null != projectMain && projectMain.getfIsProjectStart() != null) {
            resultData.put("isOk", true);
        } else {
            resultData.put("isOk", false);
        }
        result.from(resultData);
    }

    @RequestMapping("/edit/project/ReportReview")
    @ResponseBody
    @CrossOrigin("http://localhost:3000")
    public void editProjectNoReportReview() throws Exception {
        JSONObject body = getPostData();
        String projectId = body.getString("fId");
        boolean isReportReview = body.getBoolean("isReportReview");
        AppsProjectMain projectMain = appsProjectMainService.getById(projectId);
        if (null != projectMain) {
            projectMain.setfIsProjectReportReview(isReportReview);
            appsProjectMainService.save(projectMain);
        } else {
            throw new Exception("未查询到项目，更新失败");
        }
        result.ok();
    }

    @RequestMapping("/check/project/ReportReview")
    @ResponseBody
    @CrossOrigin("http://localhost:3000")
    public void checkProjectNoReportReview() throws Exception {
        JSONObject body = getPostData();
        String projectId = body.getString("fId");
        AppsProjectMain projectMain = appsProjectMainService.getById(projectId);
        JSONObject resultData = new JSONObject();
        if (null != projectMain && projectMain.getfIsProjectReportReview() != null) {
            resultData.put("isOk", true);
        } else {
            resultData.put("isOk", false);
        }
        result.from(resultData);
    }

    /**
     * 基本结束日期
     *
     * @return
     */
    @RequestMapping("/checkTime")
    @ResponseBody
    public void checkEndTime() throws Exception {
        JSONObject body = getPostData();
        String fId = body.getString("fId");
        StringBuilder errMsg = new StringBuilder();
        Date lastTime = new Date();
        boolean status = true;
        JSONObject obj = new JSONObject();
        //获取全部包件id 用于对比
        List<AppsProjectPackage> packages = projectMainUtilService.getPackagesByMainId(fId);

        /**
         * 2024-09-23 基本结束日期校验逻辑修改，旧逻辑注释废弃
         *
         //项目未终止：
         //如果为重点项目，则工作计划每个包件都要上传。
         //资格预审:每个包件都需要发布公告，如果包件发布的公告只有资格预审公告，那么此包件必须要有一个资格预审会议，取资格预审会议结束时间，
         //   每一个包件的入围结果都不为空，获取入围结果修改日期。
         //   每个包件都需要有资格预审结果公告，取文件上传时间。
         //征集文件：每个包件都需要上传且文件通过ISO流传的附件，取文件流程的结束日期(fFinishTime)。iso取状态1
         //项目启动会：有项目启动会的包件必须上传会议议程附件，取附件的上传时间。 会议时间 和 附件时间
         //汇报评审会：汇报评审会的包件必须上传会议议程附件，取附件的上传时间。
         //征集结果：每个包件都需要有征集结果，每个包件都必须有书面报告附件。
         AppsProjectMain main = appsProjectMainService.getById(fId);
         //项目未终止
         if (!"4".equals(main.getfAuditStatus())) {
         //判断是否项目重点
         if ("公司重点".equals(main.getfProjectImportance().getfName())) {
         JSONObject object = projectMainUtilService.getPackagesTime(main, packages);
         if ("E".equals(object.get("status"))) {
         status = false;
         errMsg.append(object.get("msg"));
         } else {
         if (!lastTime.after(object.getDate("msg"))) {
         lastTime = object.getDate("msg");
         }
         }
         }
         //资格预审公告
         JSONObject noiceTime = projectMainUtilService.getNoiceTime(packages);
         if ("E".equals(noiceTime.get("status"))) {
         status = false;
         errMsg.append(noiceTime.get("msg"));
         } else {
         //存在非资格预审公告情况
         if (noiceTime.getDate("msg") != null && !lastTime.after(noiceTime.getDate("msg"))) {
         lastTime = noiceTime.getDate("msg");
         }
         }
         //入围结果
         JSONObject resultTime = projectMainUtilService.getFinalizationResultTime(packages);
         if ("E".equals(resultTime.get("status"))) {
         status = false;
         errMsg.append(resultTime.get("msg"));
         } else {
         if (!lastTime.after(resultTime.getDate("msg"))) {
         lastTime = resultTime.getDate("msg");
         }
         }
         //资格预审结果公告附件
         JSONObject noticeFileTime = projectMainUtilService.getNoticeFileTime(packages, main);
         if ("E".equals(noticeFileTime.get("status"))) {
         status = false;
         errMsg.append(noticeFileTime.get("msg"));
         } else {
         if (!lastTime.after(noticeFileTime.getDate("msg"))) {
         lastTime = noticeFileTime.getDate("msg");
         }
         }
         //项目启动会
         if (main.getfIsProjectStart()) {
         JSONObject meetingKickoffObj = projectMainUtilService.getMeetingKickoffTime(main, packages);
         if ("E".equals(meetingKickoffObj.get("status"))) {
         status = false;
         errMsg.append(meetingKickoffObj.get("msg"));
         } else {
         if (!lastTime.after(meetingKickoffObj.getDate("msg"))) {
         lastTime = meetingKickoffObj.getDate("msg");
         }
         }
         }
         // 汇报评审会
         if (main.getfIsProjectReportReview()) {
         JSONObject meetingKickoffObj = projectMainUtilService.getProjectReportTime(main, packages);
         if ("E".equals(meetingKickoffObj.get("status"))) {
         status = false;
         errMsg.append(meetingKickoffObj.get("msg"));
         } else {
         if (!lastTime.after(meetingKickoffObj.getDate("msg"))) {
         lastTime = meetingKickoffObj.getDate("msg");
         }
         }
         }

         //征集文件
         JSONObject resultAttachmentTime = projectMainUtilService.getCollectionResultAttachmentTime(packages, main);
         if ("E".equals(resultAttachmentTime.get("status"))) {
         status = false;
         errMsg.append(resultAttachmentTime.get("msg"));
         } else {
         if (!lastTime.after(resultAttachmentTime.getDate("msg"))) {
         lastTime = resultAttachmentTime.getDate("msg");
         }
         }
         //征集结果
         JSONObject collectionResult = projectMainUtilService.getCollectionResultTime(packages,main);
         if ("E".equals(collectionResult.get("status"))) {
         status = false;
         errMsg.append(collectionResult.get("msg"));
         } else {
         if (!lastTime.after(collectionResult.getDate("msg"))) {
         lastTime = collectionResult.getDate("msg");
         }
         }
         }
         */

        /**
         * 2024-09-23 新基本结束日期校验逻辑
         * 1：公司重点项目-》所有包件必须上传工作计划（workPlan）
         * 2：公告-》只有资格预审公告的包件-》必须上传资格预审公告附件和资格预审公告结果附件
         * 3：资格预审会议-》会议必须关联包件
         * 4：征集文件-》所有包件必须上传征集文件，并完成ISO流转
         * 5：书面报告-》所有包件必须上传书面报告，并完成ISO流转
         * 6：项目启动会-》会议必须关联包件
         * 7：汇报评审会-》会议必须关联包件
         * 8：征集结果-》所有包件必须填写征集结果日期
         *
         * 所有校验通过后，取最晚的征集结果日期
         */
        AppsProjectMain main = appsProjectMainService.getById(fId);
        if (!"4".equals(main.getfAuditStatus())) {
            JSONObject checkResult = new JSONObject();
            //10-14 基本结束和归档 附件校验/数据校验 分开校验
            //1.公司重点项目和工作计划
//            checkResult = projectMainUtilService.checkImportance(main, packages);
//            logger.info(JSONObject.toJSONString(checkResult));
//            if (!checkResult.getBoolean("result")) {
//                status = false;
//                errMsg.append(checkResult.getString("message"));
//            }

            //2.公告
//            checkResult = projectMainUtilService.checkNotice(main, packages);
//            logger.info(JSONObject.toJSONString(checkResult));
//            if (!checkResult.getBoolean("result")) {
//                status = false;
//                errMsg.append(checkResult.getString("message"));
//            }

            //3.资格预审会议
            checkResult = projectMainUtilService.checkPreMeeting(main);
            logger.info(JSONObject.toJSONString(checkResult));
            if (!checkResult.getBoolean("result")) {
                status = false;
                errMsg.append(checkResult.getString("message"));
            }

            //4.征集文件
            checkResult = projectMainUtilService.checkCollectionFile(main, packages);
            logger.info(JSONObject.toJSONString(checkResult));
            if (!checkResult.getBoolean("result")) {
                status = false;
                errMsg.append(checkResult.getString("message"));
            }

            //5.书面报告
            checkResult = projectMainUtilService.checkWrittenReport(main, packages);
            logger.info(JSONObject.toJSONString(checkResult));
            if (!checkResult.getBoolean("result")) {
                status = false;
                errMsg.append(checkResult.getString("message"));
            }

            //6.项目启动会
            checkResult = projectMainUtilService.checkKickOff(main);
            logger.info(JSONObject.toJSONString(checkResult));
            if (!checkResult.getBoolean("result")) {
                status = false;
                errMsg.append(checkResult.getString("message"));
            }

            //7.汇报评审会
            checkResult = projectMainUtilService.checkReport(main);
            logger.info(JSONObject.toJSONString(checkResult));
            if (!checkResult.getBoolean("result")) {
                status = false;
                errMsg.append(checkResult.getString("message"));
            }

            //8.征集结果
            checkResult = projectMainUtilService.checkCollectionResult(packages);
            logger.info(JSONObject.toJSONString(checkResult));
            if (!checkResult.getBoolean("result")) {
                status = false;
                errMsg.append(checkResult.getString("message"));
            } else {
                //10-10 归档的基本结束日期校验不再获取包件的基本结束日期，改成取最后一次终期汇报会议的日期
//                lastTime = checkResult.getDate("baseFinishDate");
                lastTime = projectMainUtilService.getLastTime(main);
            }
        }
        if (lastTime == null) {
            status = false;
            errMsg.append("未召开终期汇报会");
        } else {
            //日期格式转换
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            obj.put("endTime", sdf.format(lastTime));
        }
        obj.put("status", status);
        obj.put("msg", errMsg);
        result.from(obj);
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


    /**
     * 归档文件校验
     */
    @RequestMapping("/checkFiles")
    @ResponseBody
    public void checkFiles() throws Exception {
        JSONObject body = getPostData();
        String fId = body.getString("fId");
        StringBuilder errMsg = new StringBuilder();
        boolean status = true;
        JSONObject obj = new JSONObject();
        //获取全部包件id 用于对比
        List<AppsProjectPackage> packages = projectMainUtilService.getPackagesByMainId(fId);
        /**
         * 1：公司重点项目-》所有包件必须上传工作计划（workPlan）
         * 2：公告-》只有资格预审公告的包件-》必须上传资格预审公告附件和资格预审公告结果附件
         * 3：方案征集附件
         * 4：书面情况报告
         * 5：最终设计成果
         * 通过校验才能提交归档申请
         */
        AppsProjectMain main = appsProjectMainService.getById(fId);
        if (!"4".equals(main.getfAuditStatus())) {
            JSONObject checkResult = new JSONObject();
            //1.公司重点项目和工作计划
            checkResult = projectMainUtilService.checkImportance(main, packages);
            logger.info(JSONObject.toJSONString(checkResult));
            if (!checkResult.getBoolean("result")) {
                status = false;
                errMsg.append(checkResult.getString("message"));
            }

            //2.公告
            checkResult = projectMainUtilService.checkNotice(main, packages);
            logger.info(JSONObject.toJSONString(checkResult));
            if (!checkResult.getBoolean("result")) {
                status = false;
                errMsg.append(checkResult.getString("message"));
            }
            //3.方案征集附件
            List<AttachmentMain> collectionFile = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain", main.getfId(), "collectionFile");
            if (collectionFile.isEmpty()) {
                status = false;
                errMsg.append("没有上传方案征集附件\n");
            }
            //4：书面情况报告
            List<AttachmentMain> writtenReport = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.collection.AppsCollectionResult", main.getfId(), "writtenReport");
            if (writtenReport.isEmpty()) {
                status = false;
                errMsg.append("没有上传书面报告附件\n");
            }
            //5：最终设计成果
            List<AttachmentMain> resultFile = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.report.AppsReport", main.getfId(), "resultFile");
            if (resultFile.isEmpty()) {
                status = false;
                errMsg.append("没有上传最终设计成果附件\n");
            }
        }
        obj.put("status", status);
        obj.put("msg", errMsg);
        result.from(obj);
    }
}
