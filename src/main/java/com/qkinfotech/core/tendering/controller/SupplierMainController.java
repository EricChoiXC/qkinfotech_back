package com.qkinfotech.core.tendering.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.org.model.OrgCompany;
import com.qkinfotech.core.org.model.OrgElement;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.tendering.interfaceConfig.InterfaceLog;
import com.qkinfotech.core.tendering.model.apps.designer.AppsDesignPerformance;
import com.qkinfotech.core.tendering.model.apps.designer.AppsDesignerAchievement;
import com.qkinfotech.core.tendering.model.apps.designer.AppsDesignerAwards;
import com.qkinfotech.core.tendering.model.apps.designer.AppsDesignerMain;
import com.qkinfotech.core.tendering.model.apps.notice.AppsCompanyBidPackage;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeCompanyBid;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeMain;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierInfo;
import com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierInviteCompany;
import com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain;
import com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierPackage;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import com.qkinfotech.core.tendering.model.attachment.AttachmentPackage;
import com.qkinfotech.core.tendering.service.FileMainService;
import com.qkinfotech.core.tendering.service.MqSender;
import com.qkinfotech.core.tendering.service.SupplierService;
import com.qkinfotech.util.HttpServletRequestUtil;
import com.qkinfotech.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.util.FileUtil;
import org.checkerframework.checker.units.qual.A;
import org.openqa.selenium.Pdf;
import org.openqa.selenium.PrintsPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.print.PrintOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 资格申请
 */

@RestController
@RequestMapping("/supplier/main/")
public class SupplierMainController {

    @Autowired
    protected HttpServletRequest request;
    @Autowired
    protected HttpServletResponse response;
    @Autowired
    protected SimpleResult result;
    @Autowired
    protected Bean2Json bean2json;
    @Autowired
    protected Json2Bean json2Bean;
    @Autowired
    protected SimpleService<AppsSupplierMain> appsSupplierMainService;
    @Autowired
    protected SimpleService<AppsProjectPackage> appsProjectPackageService;
    @Autowired
    protected SimpleService<AppsSupplierPackage> appsSupplierPackageService;
    @Autowired
    protected SimpleService<AppsSupplierInviteCompany> appsSupplierInviteCompanyService;
    @Autowired
    protected SimpleService<AppsCompanyBidPackage> appsCompanyBidPackageService;
    @Autowired
    protected SimpleService<OrgPerson> orgPersonService;
    @Autowired
    protected SimpleService<AppsSupplierInfo> appsSupplierInfoService;
    @Autowired
    protected SimpleService<AppsDesignerMain> appsDesignerMainService;
    @Autowired
    protected SimpleService<AppsDesignerAwards> appsDesignerAwardsService;
    @Autowired
    protected SimpleService<AppsDesignPerformance> appsDesignPerformanceService;
    @Autowired
    protected SimpleService<AppsDesignerAchievement> appsDesignerAchievementService;
    @Autowired
    protected SimpleService<AppsNoticeMain> appsNoticeMainService;
    @Autowired
    protected SimpleService<AppsProjectMain> appsProjectMainService;
    @Autowired
    protected SimpleService<AttachmentPackage> attachmentPackageService;
    @Autowired
    protected FileMainService fileMainService;
    @Autowired
    protected SupplierService supplierService;
    @Autowired
    protected MqSender mqSender;
    @Autowired
    private SimpleService<InterfaceLog> interfaceLogService;

    @RequestMapping("/save")
    @ResponseBody
    public void save() throws Exception {
        JSONObject body = getPostData();
        AppsSupplierMain main = new AppsSupplierMain();
        main.setfId(body.getString("fId"));
        //公告
        main.setfNotice(body.getObject("fNotice", AppsNoticeMain.class));
        //项目
        main.setfProjectId(body.getObject("fProjectMain", AppsProjectMain.class));
        //供应商账号
        main.setfSupplier(body.getObject("fSupplier", OrgPerson.class));
        main.setfCreateTime(new Date());
        appsSupplierMainService.save(main);//先创建一个空的记录 方便先做对应关系存储
        // 多包件/单包件 资格申请 关联 包件
        JSONArray array = body.getJSONArray("fPackageIds");
        for (Object o : array) {
            AppsProjectPackage projectPackage = appsProjectPackageService.getById(o.toString());
            AppsSupplierPackage supplierPackage = new AppsSupplierPackage();
            supplierPackage.setfPackageId(projectPackage);
            supplierPackage.setfSupplier(main);
            appsSupplierPackageService.save(supplierPackage);
        }
        //新建资格申请 添加公司默认关系
        AppsSupplierInviteCompany inviteCompany = new AppsSupplierInviteCompany();
        OrgPerson person = orgPersonService.getById(body.getJSONObject("fSupplier").getString("fId"));
        inviteCompany.setfCompanyId(person);
        inviteCompany.setfCompanyName(person.getfName());//公司名称
        //是否主体
        inviteCompany.setfIsMain("true");
        //资格申请main
        inviteCompany.setfSupplierId(main);
        inviteCompany.setfIsCreate(true);
        appsSupplierInviteCompanyService.save(inviteCompany);

        //创建默认 第一个公司信息
        AppsSupplierInfo info = new AppsSupplierInfo();
        info.setfCompanyName(person.getfName());
        info.setfInviteCompany(inviteCompany);
        info.setfSupplierId(main);
        appsSupplierInfoService.save(info);


        JSONObject object = new JSONObject();
        object.put("fMainId", main.getfId());
        result.from(object);
        result.getResponse().setCharacterEncoding("UTF-8");
    }

    @RequestMapping("/getPackageNames")
    @ResponseBody
    public void getPackageNames() throws Exception {
        JSONObject obj = new JSONObject();
        StringBuilder packageName = new StringBuilder();
        StringBuilder packageIds = new StringBuilder();
        JSONObject body = getPostData();
        //找登录人公司名称
        String loginId = body.getString("loginId");
        OrgPerson person = orgPersonService.getById(loginId);
        obj.put("name", person.getfName());
        //找项目名称
        String fMainId = body.getString("fMainId");
        AppsSupplierMain main = appsSupplierMainService.getById(fMainId);
        obj.put("title", main.getfProjectId().getfName());
        obj.put("status", main.getfCurrentStatus());//状态 0-未创建 1-已创建 2-已递交 9-过期只可查看
        //查询包件名称
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();
        JSONObject eq = new JSONObject();
        JSONObject mainId = new JSONObject();
        mainId.put("fSupplier.fId", fMainId);
        eq.put("eq", mainId);
        and.add(eq);
        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsSupplierPackage> packages = appsSupplierPackageService.findAll(JSONQuerySpecification.getSpecification(query));
        for (int i = 0; i < packages.size(); i++) {
            packageName.append(packages.get(i).getfPackageId().getfName()).append(";");
            packageIds.append(packages.get(i).getfPackageId().getfId()).append(";");
        }
        obj.put("packageIds", packageIds);
        obj.put("packageName", packageName);
        obj.put("fIsUnion", main.getfNotice().getfIsUnion());//是否支持联合体
        obj.put("active", main.getfIsUnion());//是否支持联合体
        result.from(obj);
        result.getResponse().setCharacterEncoding("UTF-8");
    }

    @RequestMapping("/inviteCompanySave")
    @ResponseBody
    public void inviteCompanySave() throws Exception {
        JSONArray body = getArraylPostData();
        for (int i = 0; i < body.size(); i++) {
            AppsSupplierInviteCompany inviteCompany = new AppsSupplierInviteCompany();
            if (StringUtil.isNotNull(body.getJSONObject(i).getString("fId"))) {
                inviteCompany.setfId(body.getJSONObject(i).getString("fId"));
            }
            inviteCompany.setfCompanyName(body.getJSONObject(i).getString("fCompanyName"));//公司名称
            if (StringUtil.isNotNull(body.getJSONObject(i).getString("fCompanyId"))) {
                //如果能匹配组织机构 就加上关联
                OrgPerson person = orgPersonService.getById(body.getJSONObject(i).getString("fCompanyId"));
                inviteCompany.setfCompanyId(person);
            }
            //是否主体
            inviteCompany.setfIsMain(body.getJSONObject(i).getString("fIsMain"));
            //资格申请main
            AppsSupplierMain projectMain = body.getJSONObject(i).getObject("fSupplierId", AppsSupplierMain.class);
            inviteCompany.setfSupplierId(projectMain);
            appsSupplierInviteCompanyService.save(inviteCompany);
        }
    }

    /**
     * 设计师数据初始化
     *
     * @throws Exception
     */
    @RequestMapping("/designerInit")
    @ResponseBody
    public void designerInit() throws Exception {
        JSONObject body = getPostData();
        JSONArray desinger = new JSONArray();
        //查询
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();
        JSONObject eq = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fSupplierId.fId", body.getString("fMainId"));
        eq.put("eq", fSupplierId);
        and.add(eq);
        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsDesignerMain> designerMains = appsDesignerMainService.findAll(JSONQuerySpecification.getSpecification(query));
        for (AppsDesignerMain designerMain : designerMains) {
            JSONObject object = new JSONObject();
            object.put("fId", designerMain.getfId());
            object.put("name", designerMain.getfName());
            object.put("level", designerMain.getfProfessionalQualification());
            object.put("years", designerMain.getfExperienceYears());
            //获取 曾获国内或国际设计奖项 记录
            object.put("winning", designerAwardsList(designerMain));
            //主创同类设计业绩 记录
            object.put("creators", designerPerformanceList(designerMain));
            // 设计师所属公司名称  false代表 不可联合体
            if ("false".equals(designerMain.getfSupplierId().getfNotice().getfIsUnion())) {
                object.put("companyName", designerMain.getfSupplierId().getfSupplier().getfName());
            } else {
                object.put("companyName", designerMain.getfCompanyName());
            }

            desinger.add(object);
        }
        result.from(desinger);
        result.getResponse().setCharacterEncoding("UTF-8");
    }

    private JSONArray designerAwardsList(AppsDesignerMain designerMain) {
        JSONArray array = new JSONArray();
        //查询
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();
        JSONObject eq = new JSONObject();
        JSONObject fDesignerId = new JSONObject();
        fDesignerId.put("fDesignerId.fId", designerMain.getfId());
        eq.put("eq", fDesignerId);
        and.add(eq);
        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsDesignerAwards> awards = appsDesignerAwardsService.findAll(JSONQuerySpecification.getSpecification(query));
        for (AppsDesignerAwards award : awards) {
            JSONObject obj = new JSONObject();
            obj.put("fId", award.getfId());
            obj.put("fName", award.getfProjectAwards());
            obj.put("fSimpleName", award.getfProjectAbbreviation());
            obj.put("fYear", award.getfAwardYear());
            array.add(obj);
        }
        return array;
    }

    private JSONArray designerPerformanceList(AppsDesignerMain designerMain) {
        JSONArray array = new JSONArray();
        //查询
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();
        JSONObject eq = new JSONObject();
        JSONObject fDesignerId = new JSONObject();
        fDesignerId.put("fDesignerId.fId", designerMain.getfId());
        eq.put("eq", fDesignerId);
        and.add(eq);
        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsDesignPerformance> performances = appsDesignPerformanceService.findAll(JSONQuerySpecification.getSpecification(query));
        for (AppsDesignPerformance performance : performances) {
            JSONObject obj = new JSONObject();
            obj.put("fId", performance.getfId());
            obj.put("name", performance.getfSameCategoryProjectName());
            array.add(obj);
        }
        return array;
    }


    /**
     * 项目数据初始化
     *
     * @throws Exception
     */
    @RequestMapping("/itemInit")
    @ResponseBody
    public void itemInit() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        JSONObject body = getPostData();
        JSONArray item = new JSONArray();
        //查询
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();
        JSONObject eq = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fSupplierId.fId", body.getString("fMainId"));
        eq.put("eq", fSupplierId);
        and.add(eq);
        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsDesignerAchievement> achievementList = appsDesignerAchievementService.findAll(JSONQuerySpecification.getSpecification(query));
        for (AppsDesignerAchievement achievement : achievementList) {
            JSONObject object = new JSONObject();
            object.put("fId", achievement.getfId());
            object.put("name", achievement.getfName());
            object.put("place", achievement.getfProjectPlace());
            object.put("owner", achievement.getfOwnerName());
            object.put("typeScales", achievement.getfFunctionality());
            object.put("time1", format.format(achievement.getfServiceStartTime()));
            object.put("time2", format.format(achievement.getfServiceEndTime()));
            object.put("status", achievement.getfProjectStatus());
            object.put("remarks", achievement.getfUndertakingWork());
            object.put("designerId", achievement.getfDesigner().getfId());
            object.put("designerName", achievement.getfDesigner().getfName());
            item.add(object);
        }
        result.from(item);
        result.getResponse().setCharacterEncoding("UTF-8");
    }


    /**
     * 获取当前登录供应商 所购买的包件
     *
     * @throws Exception
     */
    @RequestMapping("/packagelist")
    @ResponseBody
    public void getPackageList() throws Exception {
        JSONArray array = new JSONArray();
        JSONObject body = getPostData();//入参
        String loginid = body.getString("loginId");//当前供应商登录 id
        String ekpid = body.getString("ekpId");//ekp公告id
        String showActions = body.getString("showActions");//true 模糊匹配 /false 精确匹配

        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();
        JSONObject eq = new JSONObject();
        JSONObject fEkpId = new JSONObject();
        fEkpId.put("fNoticeMain.fEkpId", ekpid);
        eq.put("eq", fEkpId);
        and.add(eq);

        JSONObject eq1 = new JSONObject();
        JSONObject fSupplier = new JSONObject();
        fSupplier.put("fSupplier.fId", loginid);
        eq1.put("eq", fSupplier);
        and.add(eq1);

        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsCompanyBidPackage> companyBids = appsCompanyBidPackageService.findAll(JSONQuerySpecification.getSpecification(query));
        if (!companyBids.isEmpty()) {
            for (AppsCompanyBidPackage bidPackage : companyBids) {
                JSONObject obj = bean2json.toJson(bidPackage);
                // 查询每个包件状态 以及是否已经创建
                if ("true".equals(showActions)) {//模糊匹配
                    //公告id + 登录人 查询 suppliermain
                    AppsSupplierMain supplierMain = getSupplierMain(obj.getJSONObject("fNoticeMain").getString("fId"), loginid);
                    if (supplierMain != null) {
                        obj.put("status", supplierMain.getfCurrentStatus());
                        obj.put("supplierId", supplierMain.getfId());
                        // 找到该公告下 该包件的 suppliermain  inviteCompany 区分填写联合体
//                        getInviteCompanyBySupplier(packages.get(0).getfSupplier().getfId(), obj);
                    } else {
                        obj.put("supplierId", new AppsSupplierMain().getfId());
                    }
                } else {
                    List<AppsSupplierPackage> packages = getSupplierPackage(obj.getJSONObject("fNoticeMain").getString("fId"), obj.getJSONObject("fPackage").getString("fId"), loginid);
                    if (!packages.isEmpty()) {
                        obj.put("status", packages.get(0).getfSupplier().getfCurrentStatus());
                        obj.put("supplierId", packages.get(0).getfSupplier().getfId());
                        // 找到该公告下 该包件的 suppliermain  inviteCompany 区分填写联合体
                        getInviteCompanyBySupplier(packages.get(0).getfSupplier().getfId(), obj);
                    } else {
                        obj.put("supplierId", new AppsSupplierMain().getfId());
                    }
                }
                array.add(obj);
            }
        }
        result.from(array);
        result.getResponse().setCharacterEncoding("UTF-8");
    }


    @RequestMapping("/getInviteCompany")
    @ResponseBody
    public void getInviteCompany() throws Exception {
        JSONObject body = getPostData();//入参
        String supplierId = body.getString("supplierId");//供应商主文档 id
        String noticeId = body.getString("noticeId");//公告id
        String packageId = body.getString("packageId");//包件 id
        String personId = body.getString("personId");//登录公司 id
        String showActions = body.getString("showActions");//true 模糊匹配 /false 精确匹配
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        // 查询包件状态 以及是否已经创建
        if ("true".equals(showActions)) {
            getInviteCompanyBySupplier(supplierId, obj);
            //包件关系返回  查询 supplier_package
            JSONArray arr = new JSONArray();
            List<AppsSupplierPackage> supplierPackageList = getSupplierPackageByMain(supplierId);
            for (AppsSupplierPackage supplierPackage : supplierPackageList) {
                arr.add(supplierPackage.getfPackageId());
            }
            obj.put("fPackageId", arr);
        } else {
            List<AppsSupplierPackage> packages = getSupplierPackage(noticeId, packageId, personId);
            if (!packages.isEmpty()) {
                obj.put("status", packages.get(0).getfSupplier().getfCurrentStatus());
                obj.put("packageName", packages.get(0).getfPackageId().getfName());
                // 找到该公告下 该包件的 suppliermain  inviteCompany 区分填写联合体
                getInviteCompanyBySupplier(supplierId, obj);
            }
        }
        array.add(obj);
        result.from(array);
        result.getResponse().setCharacterEncoding("UTF-8");
    }


    private List<AppsSupplierPackage> getSupplierPackage(String noticeId, String packageId, String personId) {
        //查询 AppsSupplierMain 的包件关联
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fPackageId = new JSONObject();
        fPackageId.put("fPackageId.fId", packageId);
        eq1.put("eq", fPackageId);
        and.add(eq1);

        JSONObject eq = new JSONObject();
        JSONObject fEkpId = new JSONObject();
        fEkpId.put("fSupplier.fNotice.fId", noticeId);
        eq.put("eq", fEkpId);
        and.add(eq);

        JSONObject eq2 = new JSONObject();
        JSONObject fSupplier = new JSONObject();
        fSupplier.put("fSupplier.fSupplier.fId", personId);
        eq2.put("eq", fSupplier);
        and.add(eq2);

        queryJson.put("and", and);
        query.put("query", queryJson);
        return appsSupplierPackageService.findAll(JSONQuerySpecification.getSpecification(query));
    }

    /**
     * 供应商数据保存
     */
    @RequestMapping("/supplierSave")
    @ResponseBody
    public void supplierSave() throws Exception {
        JSONObject body = getPostData();
        AppsSupplierMain fMain = appsSupplierMainService.getById(body.getString("fMain"));
        fMain.setfCurrentStatus(body.getIntValue("fCurrentStatus"));//已保存
        appsSupplierMainService.save(fMain);
        // 找到基本信息记录 更新/新增
        infoSave(body.getJSONArray("infoValue"), fMain);
        // 找到设计师记录 更新/新增
        designerSave(body.getJSONArray("designerValue"), fMain);
        // 找到项目业绩记录 更新/新增
        itemSave(body.getJSONArray("itemValues"), fMain);

    }

    /**
     * 项目信息保存
     *
     * @param array
     * @param fMain
     */
    private void itemSave(JSONArray array, AppsSupplierMain fMain) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            //项目基本信息
            AppsDesignerAchievement achievement = appsDesignerAchievementService.getById(jsonObject.getString("fId"));
            if (achievement == null) {
                achievement = new AppsDesignerAchievement();
                achievement.setfId(jsonObject.getString("fId"));
            }
            achievement.setfName(jsonObject.getString("name"));
            achievement.setfProjectPlace(jsonObject.getString("place"));
            achievement.setfOwnerName(jsonObject.getString("owner"));
            //规模
//            JSONArray scales = jsonObject.getJSONArray("typeScales");
            achievement.setfFunctionality(jsonObject.getString("typeScales"));
            if (StringUtil.isNotNull(jsonObject.getString("time1"))) {
//                achievement.setfServiceStartTime(new Date(jsonObject.getLong("time1")));
                achievement.setfServiceStartTime(format.parse(jsonObject.getString("time1")));
            }
            if (StringUtil.isNotNull(jsonObject.getString("time2"))) {
//                achievement.setfServiceStartTime(new Date(jsonObject.getLong("time2")));
                achievement.setfServiceEndTime(format.parse(jsonObject.getString("time2")));
            }
            //关联主创设计师
            AppsDesignerMain designerId = appsDesignerMainService.getById(jsonObject.getString("designerId"));
            if (designerId != null) {
                achievement.setfDesigner(designerId);
            }
            //
            achievement.setfUndertakingWork(jsonObject.getString("remarks"));
            achievement.setfProjectStatus(jsonObject.getString("status"));
            achievement.setfCreateTime(new Date());
            achievement.setfSupplierId(fMain);
            appsDesignerAchievementService.save(achievement);
        }
    }


    /**
     * 基本信息记录save
     *
     * @return
     */
    private void infoSave(JSONArray array, AppsSupplierMain fMain) {
        for (int i = 0; i < array.size(); i++) {
            AppsSupplierInfo info = new AppsSupplierInfo();
            JSONObject jsonObject = array.getJSONObject(i);
            if (StringUtil.isNotNull(jsonObject.getString("fId"))) {//判断是否存在记录
                info = appsSupplierInfoService.getById(jsonObject.getString("fId"));
            } else {
                info.setfSupplierId(fMain);
                info.setfCreateTime(new Date());
            }
            AppsSupplierInviteCompany fInviteCompany = appsSupplierInviteCompanyService.getById(jsonObject.getString("fInviteCompany"));
            info.setfInviteCompany(fInviteCompany);
            info.setfCompanyName(jsonObject.getString("fCompanyName"));
            info.setfCountry(jsonObject.getString("fCountry"));
            info.setfLegalRepresentative(jsonObject.getString("fLegalRepresentative"));
            info.setfCompanyRegisteredAddress(jsonObject.getString("fCompanyRegisteredAddress"));
            info.setfIncorporationTime(jsonObject.getString("fIncorporationTime"));
            info.setfCompanyPhone(jsonObject.getString("fCompanyPhone"));
            info.setfOfficialWebsiteAddress(jsonObject.getString("fOfficialWebsiteAddress"));
            info.setfDesignersTotal(jsonObject.getIntValue("fDesignersTotal"));
            info.setfRegisteredArchitectsOrLandscapeArchitects(jsonObject.getIntValue("fRegisteredArchitectsOrLandscapeArchitects"));
            info.setfBusinessregistrationbusinesslicensenumber(jsonObject.getString("fBusinessregistrationbusinesslicensenumber"));
            info.setfDesignQualificationTypeOrLevel(jsonObject.getString("fDesignQualificationTypeOrLevel"));
            info.setfContactPerson(jsonObject.getString("fContactPerson"));
            info.setfDuties(jsonObject.getString("fDuties"));
            info.setfPhone(jsonObject.getString("fPhone"));
            info.setfEmail(jsonObject.getString("fEmail"));
            info.setfMailingAddressAndPostcode(jsonObject.getString("fMailingAddressAndPostcode"));
            info.setfCompanyProfile(jsonObject.getString("fCompanyProfile"));
            appsSupplierInfoService.save(info);
        }
    }

    /**
     * 设计师数据保存
     *
     * @param array
     * @param fMain
     */
    private void designerSave(JSONArray array, AppsSupplierMain fMain) {
        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            //设计师基本信息
            AppsDesignerMain designerMain = appsDesignerMainService.getById(jsonObject.getString("fId"));
            if (designerMain == null) {
                designerMain = new AppsDesignerMain();
                designerMain.setfId(jsonObject.getString("fId"));
            }
            designerMain.setfCompanyName(jsonObject.getString("companyName"));
            designerMain.setfCreateTime(new Date());
            designerMain.setfSupplierId(fMain);
            designerMain.setfName(jsonObject.getString("name"));
            designerMain.setfProfessionalQualification(jsonObject.getString("level"));
            designerMain.setfExperienceYears(jsonObject.getString("years"));
            appsDesignerMainService.save(designerMain);
            //曾获国内或国际设计奖项
            JSONArray winning = jsonObject.getJSONArray("winning");
            designerAwardsSave(winning, designerMain);
            //主创同类设计业绩
            JSONArray creators = jsonObject.getJSONArray("creators");
            designerPerformanceSave(creators, designerMain);

        }
    }

    private void designerAwardsSave(JSONArray array, AppsDesignerMain designerMain) {
        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            //曾获国内或国际设计奖项
            AppsDesignerAwards awards = new AppsDesignerAwards();
            if (StringUtil.isNotNull(jsonObject.getString("fId"))) {
                awards.setfId(jsonObject.getString("fId"));
            }
            awards.setfCreateTime(new Date());
            awards.setfDesignerId(designerMain);
            awards.setfAwardYear(jsonObject.getString("fYear"));
            awards.setfProjectAwards(jsonObject.getString("fName"));
            awards.setfProjectAbbreviation(jsonObject.getString("fSimpleName"));
            appsDesignerAwardsService.save(awards);
        }
    }

    private void designerPerformanceSave(JSONArray array, AppsDesignerMain designerMain) {
        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            //主创同类设计业绩
            AppsDesignPerformance performance = new AppsDesignPerformance();
            if (StringUtil.isNotNull(jsonObject.getString("fId"))) {
                performance.setfId(jsonObject.getString("fId"));
            }
            performance.setfDesignerId(designerMain);
            performance.setfCreateTime(new Date());
            performance.setfSameCategoryProjectName(jsonObject.getString("name"));
            appsDesignPerformanceService.save(performance);
        }
    }

    /**
     * 删除公司信息
     */
    @RequestMapping("/deleteSupplierInfo")
    @ResponseBody
    public void deleteSupplierInfo() throws Exception {
        JSONObject body = getPostData();
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();
        JSONObject eq = new JSONObject();
        JSONObject inviteCompany = new JSONObject();
        inviteCompany.put("fInviteCompany.fId", body.getString("fInviteCompany"));
        eq.put("eq", inviteCompany);
        and.add(eq);
        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsSupplierInfo> infos = appsSupplierInfoService.findAll(JSONQuerySpecification.getSpecification(query));
        if (!infos.isEmpty()) {
            appsSupplierInfoService.delete(infos.get(0).getfId());
        }
    }

    /**
     * 删除设计师数据
     *
     * @throws Exception
     */
    @RequestMapping("/deleteDesigner")
    @ResponseBody
    public void deleteDesigner() throws Exception {
        JSONObject body = getPostData();
        String id = body.getString("fId");
        //
        supplierService.delDesignerById(id);
    }


    /**
     * 联合体初始化
     *
     * @throws Exception
     */
    @RequestMapping("/companyInit")
    @ResponseBody
    public void companyInit() throws Exception {
        JSONObject body = getPostData();
        String supplierId = body.getString("supplierId");
        String companyId = body.getString("companyId");
        List<AppsSupplierInviteCompany> inviteCompanyList = supplierService.findInviteCompanyList(supplierId, companyId);
        if (inviteCompanyList.isEmpty()) {//如果为空则没有创建关联关系，需要新建
            AppsSupplierInviteCompany inviteCompany = new AppsSupplierInviteCompany();
            inviteCompany.setfSupplierId(appsSupplierMainService.getById(supplierId));
            inviteCompany.setfCompanyId(orgPersonService.getById(companyId));
            inviteCompany.setfCompanyName(orgPersonService.getById(companyId).getfName());
            inviteCompany.setfIsMain("true");
            appsSupplierInviteCompanyService.save(inviteCompany);
        }

    }


    /**
     * 资格申请 是否在期限范围内 可填写
     *
     * @throws Exception
     */
    @RequestMapping("/getNoticeDateRange")
    @ResponseBody
    public void getNoticeDateRange() throws Exception {
        JSONObject body = getPostData();
        String fId = body.getString("fId");
        AppsSupplierMain main = appsSupplierMainService.getById(fId);
        Date date = main.getfNotice().getfApplicationDocumentDeadline();
        JSONObject obj = new JSONObject();
        obj.put("deadline", date);
        if (date.before(new Date())) {
            obj.put("flag", false);
        } else {
            obj.put("flag", true);
        }
        result.from(obj);
        result.getResponse().setCharacterEncoding("UTF-8");
    }


    /**
     * 历史文件数据导入
     *
     * @return
     */
    @RequestMapping("/getHistoryData")
    @ResponseBody
    public void getHistoryData() throws Exception {
        JSONObject body = getPostData();
        String fId = body.getString("fId");//要被覆盖记录的supplier id
        String historyId = body.getString("historyId");//历史记录supplier id
        String companyId = body.getString("companyId");// 公司id
        AppsSupplierMain main = appsSupplierMainService.getById(fId);
        AppsSupplierMain historyMain = appsSupplierMainService.getById(historyId);
        supplierService.historyCover(main, historyMain, companyId);
    }

    @RequestMapping("/getSupplierFile")
    @ResponseBody
    public void getSupplierFile() throws Exception {
        JSONObject body = getPostData();
        String fId = body.getString("fId");//supplier id
        JSONObject object = new JSONObject();
        List<AttachmentMain> attachmentMains = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain", fId, "supplierMain");
        if (!attachmentMains.isEmpty()) {
            object.put("fileId", attachmentMains.get(0).getfFile().getfId());
        }
        result.from(object);
        result.getResponse().setCharacterEncoding("UTF-8");
    }

    @RequestMapping("/getSupplierFileList")
    @ResponseBody
    public void getSupplierFileList() throws Exception {
        JSONObject body = getPostData();
        String fProtocolNo = body.getString("fProtocolNo");
        String fProjectName = body.getString("fProjectName");
        String fProjectManager = body.getString("fProjectManager");
        String fCompanyName = body.getString("fCompanyName");
        String fDesignersTotalType = body.getString("fDesignersTotalType");
        Integer fDesignersTotal = body.getInteger("fDesignersTotal");
        String fName = body.getString("fName");
        String fDesignQualificationTypeOrLevel = body.getString("fDesignQualificationTypeOrLevel");
        String fProfessionalQualification = body.getString("fProfessionalQualification");
        String fCountry = body.getString("fCountry");
        String fOwnerName = body.getString("fOwnerName");
        String fNorm = body.getString("fNorm");
        String fFunctionality = body.getString("fFunctionality");
        String fProjectPlace = body.getString("fProjectPlace");
        String fExperienceYearsType = body.getString("fExperienceYearsType");
        Integer fExperienceYears = body.getInteger("fExperienceYears");
        List supplierFiles = fileMainService.getSupplierFiles(fProtocolNo, fProjectName, fProjectManager, fCompanyName, fDesignersTotalType
                , fDesignersTotal, fName, fDesignQualificationTypeOrLevel, fProfessionalQualification, fCountry, fOwnerName, fNorm,
                fFunctionality, fProjectPlace, fExperienceYearsType, fExperienceYears);
        JSONObject res = new JSONObject();
        res.put("list", supplierFiles);
        result.from(res);
    }

//    public JSONObject checkEditor(){
//     JSONObject object = new JSONObject();
//     JSONObject body = getPostData();
//        String userId = body.getString("userId");
//
//
//        return object;
//    }

    @RequestMapping("/updateSupplier")
    @ResponseBody
    public void updateSupplier() throws Exception {
        JSONObject body = getPostData();
        String fId = body.getString("fId");//supplier id
        AppsSupplierMain main = appsSupplierMainService.getById(fId);
        main.setfSubTime(new Date());//提交时间
        main.setfCurrentStatus(2);//状态2 为递交
        main.setfIp(HttpServletRequestUtil.getRemoteIP(request));//递交ip地址
        appsSupplierMainService.save(main);
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

    private JSONArray getArraylPostData() {
        if (!"POST".equals(request.getMethod())) {
            return new JSONArray();
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
                return new JSONArray();
            }
            return JSONArray.parseArray(txt);

        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }


    /**
     * 供应商没有在线编辑 页面的 save
     *
     * @throws Exception
     */
    @RequestMapping("/newSave")
    @ResponseBody
    public void newSave() throws Exception {
        JSONArray arraylPostData = getArraylPostData();
        for (int i = 0; i < arraylPostData.size(); i++) {
            JSONObject object = arraylPostData.getJSONObject(i);
            //项目id
            String mainId = object.getString("mainId");
            //公告id
            String noticeId = object.getString("noticeId");
            //登录账号供应商id
            String supplierId = object.getString("userId");
            //判断是否有记录 具体是新建还是更新
            if (object.containsKey("supplierId")) {
                AppsSupplierMain main = appsSupplierMainService.getById(object.getString("supplierId"));
                if (main != null) {
                    //更新
                    main.setfCurrentStatus(object.getInteger("currentStatus"));
                    main.setfSubTime(new Date());
                    main.setfIp(HttpServletRequestUtil.getRemoteIP(request));//递交ip地址
                    appsSupplierMainService.save(main);
                    //
                    if ("true".equals(main.getfNotice().getfIsAccurateMatching())) {//模糊匹配
                        if (object.getJSONArray("packageId") != null) {
                            //全量更新包件关系 去除关系
                            delSupplierPackage(main);
                            //添加新关系
                            JSONArray packageIds = object.getJSONArray("packageId");
                            for (int le = 0; le < packageIds.size(); le++) {
                                AppsProjectPackage projectPackage = appsProjectPackageService.getById(packageIds.getString(le));
                                AppsSupplierPackage supplierPackage = new AppsSupplierPackage();
                                supplierPackage.setfPackageId(projectPackage);
                                supplierPackage.setfSupplier(main);
                                appsSupplierPackageService.save(supplierPackage);
                            }
                        }
                    }
                } else {
                    //新增
                    main = new AppsSupplierMain();
                    main.setfId(object.getString("supplierId"));
                    main.setfCreateTime(new Date());
                    main.setfNotice(appsNoticeMainService.getById(noticeId));
                    main.setfProjectId(appsProjectMainService.getById(mainId));
                    main.setfSupplier(orgPersonService.getById(supplierId));
                    main.setfCurrentStatus(object.getInteger("currentStatus"));
                    main.setfCreateTime(new Date());
                    main.setfSubTime(new Date());
                    main.setfIp(HttpServletRequestUtil.getRemoteIP(request));//递交ip地址
                    appsSupplierMainService.save(main);
                    // 多包件/单包件 资格申请 关联 包件
                    if ("true".equals(main.getfNotice().getfIsAccurateMatching())) {//模糊匹配
                        if (object.getJSONArray("packageId") != null) {
                            JSONArray packageIds = object.getJSONArray("packageId");
                            for (int le = 0; le < packageIds.size(); le++) {
                                AppsProjectPackage projectPackage = appsProjectPackageService.getById(packageIds.getString(le));
                                AppsSupplierPackage supplierPackage = new AppsSupplierPackage();
                                supplierPackage.setfPackageId(projectPackage);
                                supplierPackage.setfSupplier(main);
                                appsSupplierPackageService.save(supplierPackage);
                            }
                        }
                    } else {//精确
                        AppsProjectPackage projectPackage = appsProjectPackageService.getById(object.getString("packageId"));
                        AppsSupplierPackage supplierPackage = new AppsSupplierPackage();
                        supplierPackage.setfPackageId(projectPackage);
                        supplierPackage.setfSupplier(main);
                        appsSupplierPackageService.save(supplierPackage);
                    }
                }
                List<AttachmentMain> attachments = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain", main.getfId(), "supplierMain");
                //附件包件关系
                if (!attachments.isEmpty()) {
                    if ("true".equals(main.getfNotice().getfIsAccurateMatching())) {
                        //模糊匹配
                        JSONArray packageIds = object.getJSONArray("packageId");
                        if (!attachments.isEmpty()) {
                            for (int le = 0; le < packageIds.size(); le++) {
                                AppsProjectPackage projectPackage = appsProjectPackageService.getById(packageIds.getString(le));
                                AttachmentPackage pack = new AttachmentPackage();
                                pack.setfAttachmentId(attachments.get(0));
                                pack.setfPackageId(projectPackage);
                                attachmentPackageService.save(pack);
                            }
                        }
                    } else {
                        AppsProjectPackage projectPackage = appsProjectPackageService.getById(object.getString("packageId"));
                        AttachmentPackage pack = new AttachmentPackage();
                        pack.setfAttachmentId(attachments.get(0));
                        pack.setfPackageId(projectPackage);
                        attachmentPackageService.save(pack);
                    }
                }
                //申请公司关系
                AppsSupplierInviteCompany inviteCompany;
                if (object.containsKey("inviteCompany") && StringUtil.isNotNull(object.getString("inviteCompany"))) {
                    inviteCompany = appsSupplierInviteCompanyService.getById(object.getString("inviteCompany"));
                } else {
                    inviteCompany = new AppsSupplierInviteCompany();
                }
                saveSupplierInviteCompany(inviteCompany, object, main, true, supplierId);

                //联合体记录删除 全量更新
                delInviteCompany(main);
                //是否有联合体记录
                if (object.containsKey("customValue")) {
                    JSONArray array1 = object.getJSONArray("customValue");
                    for (int l = 0; l < array1.size(); l++) {
                        AppsSupplierInviteCompany unionInviteCompany = new AppsSupplierInviteCompany();
                        JSONObject obj = array1.getJSONObject(l);
                        if (obj.containsKey("companyName")) {
                            saveSupplierInviteCompany(unionInviteCompany, obj, main, false, null);
                        }
                    }
                }
            }
        }
    }


    /**
     * 查询公司名称 是否存在 数据库
     *
     * @param name
     * @return
     */
    public OrgPerson getCompanyByName(String name) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fName = new JSONObject();
        fName.put("fName", name);
        eq1.put("eq", fName);
        and.add(eq1);

        queryJson.put("and", and);
        query.put("query", queryJson);
        List<OrgPerson> list = orgPersonService.findAll(JSONQuerySpecification.getSpecification(query));
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public void saveSupplierInviteCompany(AppsSupplierInviteCompany inviteCompany, JSONObject obj, AppsSupplierMain
            main, boolean fIsCreate, String supplierId) {
        if (StringUtil.isNull(supplierId)) {
            inviteCompany.setfCompanyName(obj.getString("companyName"));
            //查询名称是否存在有就存id
            if (getCompanyByName(obj.getString("companyName")) != null) {
                inviteCompany.setfCompanyId(getCompanyByName(obj.getString("companyName")));
            }
        } else {
            OrgPerson person = orgPersonService.getById(supplierId);
            inviteCompany.setfCompanyId(person);
            inviteCompany.setfCompanyName(person.getfName());//公司名称
        }

        if (obj.containsKey("isMain")) {
            inviteCompany.setfIsMain(obj.getString("isMain"));
        }
        if (obj.containsKey("country")) {
            inviteCompany.setfCountry(obj.getString("country"));
        }
        if (obj.containsKey("designerName")) {
            inviteCompany.setfDesignerName(obj.getString("designerName"));
        }
        inviteCompany.setfIsCreate(fIsCreate);
        inviteCompany.setfSupplierId(main);
        appsSupplierInviteCompanyService.save(inviteCompany);
    }

    /**
     * 通过suppliermain获取联合体
     *
     * @param fId
     * @param object
     * @return
     */
    public JSONObject getInviteCompanyBySupplier(String fId, JSONObject object) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fSupplierId.fId", fId);
        eq1.put("eq", fSupplierId);
        and.add(eq1);

        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsSupplierInviteCompany> lists = appsSupplierInviteCompanyService.findAll(JSONQuerySpecification.getSpecification(query));
        JSONArray customValue = new JSONArray();
        for (AppsSupplierInviteCompany list : lists) {
            if (list.getfIsCreate()) {
                object.put("inviteCompany", list.getfId());
                object.put("currentStatus", list.getfSupplierId().getfCurrentStatus());
                object.put("companyName", list.getfCompanyName());
                object.put("country", list.getfCountry());
                object.put("designerName", list.getfDesignerName());
                object.put("isMain", list.getfIsMain());
            } else {
                JSONObject obj = new JSONObject();
                obj.put("inviteCompanyId", list.getfId());
                obj.put("companyName", list.getfCompanyName());
                obj.put("country", list.getfCountry());
                obj.put("designerName", list.getfDesignerName());
                obj.put("isMain", list.getfIsMain());
                customValue.add(obj);
            }
            object.put("customValue", customValue);
        }
        return object;
    }

    public void delInviteCompany(AppsSupplierMain main) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fIsCreate = new JSONObject();
        fIsCreate.put("fIsCreate", false);
        eq1.put("eq", fIsCreate);
        and.add(eq1);

        JSONObject eq2 = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fSupplierId.fId", main.getfId());
        eq2.put("eq", fSupplierId);
        and.add(eq2);

        queryJson.put("and", and);
        query.put("query", queryJson);
        appsSupplierInviteCompanyService.delete(JSONQuerySpecification.getSpecification(query));

    }


    /**
     * 供应商没有在线编辑 页面的 附件校验是否上传
     *
     * @throws Exception
     */
    @RequestMapping("/checkAtt")
    @ResponseBody
    public void checkAtt() throws Exception {
        JSONObject body = getPostData();
        String supplierId = body.getString("supplierId");
        List<AttachmentMain> pptAtt = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain", supplierId, "supplierMain");
//        List<AttachmentMain> pdfAtt = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain", supplierId, "supplierPDF");
        JSONObject res = new JSONObject();
        if (!pptAtt.isEmpty()) {
            res.put("flag", true);
        } else {
            res.put("flag", false);
        }
        result.from(res);
        result.getResponse().setCharacterEncoding("UTF-8");
    }

    /**
     * 更新ekp投标上传文件数
     */
    @RequestMapping("/updateEkpFileSize")
    @ResponseBody
    public void updateEkpFileSize() throws Exception {
        JSONObject body = getPostData();
        String supplierId = body.getString("supplierId");
        String noticeId = body.getString("noticeId");
        //找公告的ekpid
        AppsSupplierMain main = appsSupplierMainService.getById(noticeId);

        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fNotice = new JSONObject();
        fNotice.put("fNotice.fEkpId", main.getfNotice().getfEkpId());
        eq1.put("eq", fNotice);
        and.add(eq1);

        JSONObject eq2 = new JSONObject();
        JSONObject fSupplier = new JSONObject();
        fSupplier.put("fSupplier.fId", supplierId);
        eq2.put("eq", fSupplier);
        and.add(eq2);

        JSONObject eq3 = new JSONObject();
        JSONObject fCurrentStatus = new JSONObject();
        fCurrentStatus.put("fCurrentStatus", 2);
        eq3.put("eq", fCurrentStatus);
        and.add(eq3);

        queryJson.put("and", and);
        query.put("query", queryJson);

        List<AppsSupplierMain> mains = appsSupplierMainService.findAll(JSONQuerySpecification.getSpecification(query));
//        {
//            "newsId": "ekp公告id"
//            "signId": "ekp报名信息id",
//                "supplierId": "ekp供应商id",
//                "size": "5"
//        }
        JSONObject object = new JSONObject();
        object.put("newsId", noticeId);
        object.put("signId", "");
        object.put("supplierId", supplierId);
        object.put("size", mains.size());
        //查出该供应商 提交了多少申请文件 然后推送mq给ekp
        mqSender.sendMq("SYS_NEWS_FILE_SIZE_LISTENER", object.toJSONString());

        InterfaceLog log = new InterfaceLog();
        log.setfInterfaceUrl("SYS_NEWS_FILE_SIZE_LISTENER");
        log.setfInterfaceName("推送角标mq");
        log.setfInputParameter(object.toJSONString());
        log.setfCreateTime(new Date());
        log.setfInterfaceStatus("2");
        log.setfInterfaceInfo(result.toString());
        interfaceLogService.save(log);

    }


    /**
     * 根据公告查询领购包件
     */
    @RequestMapping("/getPackagesByNotice")
    @ResponseBody
    public void getPackagesByNotice() throws Exception {
        JSONObject body = getPostData();
        JSONArray res = new JSONArray();
        String ekpId = body.getString("ekpId");
        String loginId = body.getString("loginId");
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fNotice = new JSONObject();
        fNotice.put("fNoticeMain.fEkpId", ekpId);
        eq1.put("eq", fNotice);
        and.add(eq1);

        JSONObject eq2 = new JSONObject();
        JSONObject fSupplier = new JSONObject();
        fSupplier.put("fSupplier.fId", loginId);
        eq2.put("eq", fSupplier);
        and.add(eq2);

        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsCompanyBidPackage> list = appsCompanyBidPackageService.findAll(JSONQuerySpecification.getSpecification(query));
        for (AppsCompanyBidPackage bid : list) {
            res.add(bid.getfPackage());
        }

        result.from(res);
        result.getResponse().setCharacterEncoding("UTF-8");
    }

    public AppsSupplierMain getSupplierMain(String noticefId, String personId) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fNotice = new JSONObject();
        fNotice.put("fNotice.fId", noticefId);
        eq1.put("eq", fNotice);
        and.add(eq1);

        JSONObject eq2 = new JSONObject();
        JSONObject fSupplier = new JSONObject();
        fSupplier.put("fSupplier.fId", personId);
        eq2.put("eq", fSupplier);
        and.add(eq2);

        queryJson.put("and", and);
        query.put("query", queryJson);

        List<AppsSupplierMain> mains = appsSupplierMainService.findAll(JSONQuerySpecification.getSpecification(query));
        if (!mains.isEmpty()) {
            return mains.get(0);
        }
        return null;
    }

    private List<AppsSupplierPackage> getSupplierPackageByMain(String fSupplierId) {
        //查询 AppsSupplierMain 的包件关联
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fPackageId = new JSONObject();
        fPackageId.put("fSupplier.fId", fSupplierId);
        eq1.put("eq", fPackageId);
        and.add(eq1);

        queryJson.put("and", and);
        query.put("query", queryJson);
        return appsSupplierPackageService.findAll(JSONQuerySpecification.getSpecification(query));
    }

    public void delSupplierPackage(AppsSupplierMain main) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fSupplier.fId", main.getfId());
        eq.put("eq", fSupplierId);
        and.add(eq);

        queryJson.put("and", and);
        query.put("query", queryJson);
        appsSupplierPackageService.delete(JSONQuerySpecification.getSpecification(query));

    }


}