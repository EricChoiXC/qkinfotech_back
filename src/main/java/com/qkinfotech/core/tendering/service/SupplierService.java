package com.qkinfotech.core.tendering.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.tendering.model.apps.designer.AppsDesignPerformance;
import com.qkinfotech.core.tendering.model.apps.designer.AppsDesignerAchievement;
import com.qkinfotech.core.tendering.model.apps.designer.AppsDesignerAwards;
import com.qkinfotech.core.tendering.model.apps.designer.AppsDesignerMain;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticePackage;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierInfo;
import com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierInviteCompany;
import com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain;
import com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierPackage;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import com.qkinfotech.core.tendering.model.masterModels.MasterDataMeetingType;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SupplierService {

    @Autowired
    protected SimpleService<AppsSupplierInviteCompany> appsSupplierInviteCompanyService;
    @Autowired
    protected SimpleService<AppsSupplierInfo> appsSupplierInfoService;
    @Autowired
    protected SimpleService<AppsSupplierMain> appsSupplierMainService;
    @Autowired
    protected SimpleService<AppsDesignerMain> appsDesignerMainService;
    @Autowired
    protected SimpleService<AppsDesignerAwards> appsDesignerAwardsService;
    @Autowired
    protected SimpleService<AppsDesignPerformance> appsDesignPerformanceService;
    @Autowired
    protected SimpleService<AppsDesignerAchievement> appsDesignerAchievementService;
    @Autowired
    protected SimpleService<AttachmentMain> attachmentMainService;
    @Autowired
    protected SimpleService<AppsSupplierPackage> appsSupplierPackageService;
    @Autowired
    protected FileMainService fileMainService;

    @Autowired
    protected Bean2Json bean2json;

    @Autowired
    protected Json2Bean json2bean;


    /**
     * 历史记录覆盖
     */
    public void historyCover(AppsSupplierMain main, AppsSupplierMain historyMain, String companyId) throws Exception {
        //清空全部关系 公司信息/联合体/设计师/项目
        cleanInfos(main, companyId);
        cleanCompanys(main, companyId);
        cleanItems(main);
        cleanDesigners(main);
        //找到自己InviteCompany记录
        AppsSupplierInviteCompany company = findMyCompany(main, companyId);
        //找到自己的info记录
        AppsSupplierInfo myInfo = findMyInfo(main, company);

        //新增关系 查询历史记录
        List<AppsSupplierInviteCompany> historylist = findInviteCompanyList(historyMain.getfId(), companyId);
        for (AppsSupplierInviteCompany supplierInviteCompany : historylist) {
            //找出 历史公司信息
            AppsSupplierInfo info = findInfo(historyMain, supplierInviteCompany);
            //查找不是自己公司，也就是联合体
            if (!companyId.equals(supplierInviteCompany.getfCompanyId().getfId())) {
                //联合体数据 关联
                AppsSupplierInviteCompany inviteCompany = new AppsSupplierInviteCompany();
                inviteCompany.setfSupplierId(main);
                inviteCompany.setfCompanyId(supplierInviteCompany.getfCompanyId());
                inviteCompany.setfCompanyName(supplierInviteCompany.getfCompanyName());
                inviteCompany.setfIsMain(supplierInviteCompany.getfIsMain());
                appsSupplierInviteCompanyService.save(inviteCompany);
                //对应的历史公司信息,进行覆盖
                if (info != null) {
                    AppsSupplierInfo newInfo = new AppsSupplierInfo();
                    JSONObject json = bean2json.toJson(info);
                    json.put("fId", newInfo.getfId());
                    json.put("fSupplierId", main.getfId());
                    json.put("fInviteCompany", inviteCompany.getfId());
                    AppsSupplierInfo bean = json2bean.toBean(json, AppsSupplierInfo.class);
//                    info.setfId(newInfo.getfId());
//                    info.setfInviteCompany(inviteCompany);
//                    info.setfSupplierId(main);
                    appsSupplierInfoService.save(bean);
                }
            } else {
                //更新 是否主体标记
                company.setfIsMain(supplierInviteCompany.getfIsMain());
                appsSupplierInviteCompanyService.save(company);
                //覆盖自己公司记录
                if (myInfo != null) {
                    myInfo.setfCountry(info.getfCountry());
                    myInfo.setfLegalRepresentative(info.getfLegalRepresentative());
                    myInfo.setfCompanyRegisteredAddress(info.getfCompanyRegisteredAddress());
                    myInfo.setfIncorporationTime(info.getfIncorporationTime());
                    myInfo.setfCompanyPhone(info.getfCompanyPhone());
                    myInfo.setfOfficialWebsiteAddress(info.getfOfficialWebsiteAddress());
                    myInfo.setfDesignersTotal(info.getfDesignersTotal());
                    myInfo.setfRegisteredArchitectsOrLandscapeArchitects(info.getfRegisteredArchitectsOrLandscapeArchitects());
                    myInfo.setfBusinessregistrationbusinesslicensenumber(info.getfBusinessregistrationbusinesslicensenumber());
                    myInfo.setfDesignQualificationTypeOrLevel(info.getfDesignQualificationTypeOrLevel());
                    myInfo.setfContactPerson(info.getfContactPerson());
                    myInfo.setfDuties(info.getfDuties());
                    myInfo.setfPhone(info.getfPhone());
                    myInfo.setfEmail(info.getfEmail());
                    myInfo.setfMailingAddressAndPostcode(info.getfMailingAddressAndPostcode());
                    myInfo.setfCompanyProfile(info.getfCompanyProfile());
                    myInfo.setfCreateTime(new Date());
                    appsSupplierInfoService.save(myInfo);
                }
            }
        }

        //找到对应的历史设计师信息,照片 进行覆盖
        List<AppsDesignerMain> designers = findDesigner(historyMain);
        //历史 设计师 纪录循环 进行覆盖
        JSONObject obj = new JSONObject();
        for (AppsDesignerMain designer : designers) {
            //新设计师记录
            AppsDesignerMain designerMain = new AppsDesignerMain();
            designerMain.setfName(designer.getfName());
            designerMain.setfCompanyName(designer.getfCompanyName());
            designerMain.setfSupplierId(main);
            designerMain.setfCreateTime(new Date());
            designerMain.setfExperienceYears(designer.getfExperienceYears());
            designerMain.setfProfessionalQualification(designer.getfProfessionalQualification());
            appsDesignerMainService.save(designerMain);
            //旧设计师记录 对应 新设计师关系 用于项目关联设计师
            obj.put(designer.getfId(), designerMain);
            //该设计师的 历史 主创同类设计业绩
            List<AppsDesignPerformance> performances = findPerformance(designer);
            for (AppsDesignPerformance performance : performances) {
                AppsDesignPerformance newPerformance = new AppsDesignPerformance();
                newPerformance.setfDesignerId(designerMain);
                newPerformance.setfCreateTime(new Date());
                newPerformance.setfSameCategoryProjectName(performance.getfSameCategoryProjectName());
                appsDesignPerformanceService.save(newPerformance);
            }
            //该设计师的 历史 奖项
            List<AppsDesignerAwards> awards = findAwards(designer);
            for (AppsDesignerAwards award : awards) {
                AppsDesignerAwards newAward = new AppsDesignerAwards();
                newAward.setfProjectAwards(award.getfProjectAwards());
                newAward.setfDesignerId(designerMain);
                appsDesignerAwardsService.save(newAward);
            }
            //历史 图片 关系
            saveNewAtt("com.qkinfotech.core.tendering.model.apps.designer.AppsDesignerMain", designer.getfId(), "designer", designerMain.getfId());
        }
        //历史 项目 纪录循环 进行覆盖
        List<AppsDesignerAchievement> items = findItems(historyMain);
        for (AppsDesignerAchievement item : items) {
            AppsDesignerMain designerMain = (AppsDesignerMain) obj.get(item.getfDesigner().getfId());
            //公司项目 覆盖
            AppsDesignerAchievement newItem = new AppsDesignerAchievement();
            newItem.setfName(item.getfName());
            newItem.setfProjectPlace(item.getfProjectPlace());
            newItem.setfOwnerName(item.getfOwnerName());
            newItem.setfFunctionality(item.getfFunctionality());
            newItem.setfNorm(item.getfNorm());
            newItem.setfServiceStartTime(item.getfServiceStartTime());
            newItem.setfServiceEndTime(item.getfServiceEndTime());
            newItem.setfProjectStatus(item.getfProjectStatus());
            newItem.setfUndertakingWork(item.getfUndertakingWork());
            newItem.setfCreateTime(new Date());
            newItem.setfDesigner(designerMain);
            newItem.setfSupplierId(main);
            appsDesignerAchievementService.save(newItem);
            //历史 图片 覆盖
            saveNewAtt("com.qkinfotech.core.tendering.model.apps.designer.AppsDesignerAchievement", item.getfId(), "achievement", newItem.getfId());
            saveNewAtt("com.qkinfotech.core.tendering.model.apps.designer.AppsDesignerAchievement", item.getfId(), "achievement2", newItem.getfId());

        }
    }

    /**
     * 覆盖新图片
     *
     * @param fModelName
     * @param fModelId
     * @param fkey
     * @param newId
     */
    public void saveNewAtt(String fModelName, String fModelId, String fkey, String newId) throws Exception {
        List<AttachmentMain> attachmentMains = fileMainService.getAttMain(fModelName, fModelId, fkey);
        if (!attachmentMains.isEmpty()) {
            AttachmentMain attachmentMain = attachmentMains.get(0);
            AttachmentMain newAtt = new AttachmentMain();
            fileMainService.saveAttMain(newAtt, attachmentMain.getfFile().getfId(), attachmentMain.getfFileName(), fModelName, newId, fkey, "");
        }
    }


    /**
     * 查询自己公司
     */
    public AppsSupplierInviteCompany findMyCompany(AppsSupplierMain main, String companyId) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fSupplierId.fId", main.getfId());
        eq1.put("eq", fSupplierId);
        and.add(eq1);

        JSONObject eq2 = new JSONObject();
        JSONObject fCompanyId = new JSONObject();
        fCompanyId.put("fCompanyId.fId", companyId);
        eq2.put("eq", fCompanyId);
        and.add(eq2);

        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsSupplierInviteCompany> infos = appsSupplierInviteCompanyService.findAll(JSONQuerySpecification.getSpecification(query));
        if (infos.isEmpty()) {
            return null;
        }
        return infos.get(0);
    }

    /**
     * 查询自己公司基本信息
     */
    public AppsSupplierInfo findMyInfo(AppsSupplierMain main, AppsSupplierInviteCompany inviteCompany) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fSupplierId.fId", main.getfId());
        eq1.put("eq", fSupplierId);
        and.add(eq1);

        JSONObject eq2 = new JSONObject();
        JSONObject fCompanyId = new JSONObject();
        fCompanyId.put("fInviteCompany.fId", inviteCompany.getfId());
        eq2.put("eq", fCompanyId);
        and.add(eq2);

        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsSupplierInfo> infos = appsSupplierInfoService.findAll(JSONQuerySpecification.getSpecification(query));
        if (infos.isEmpty()) {
            return null;
        }
        return infos.get(0);
    }


    /**
     * 查询 历史公司基本信息
     *
     * @param main
     * @param inviteCompany
     * @return
     */
    public AppsSupplierInfo findInfo(AppsSupplierMain main, AppsSupplierInviteCompany inviteCompany) {
        //查找是否已经存在关系
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fSupplierId.fId", main.getfId());
        eq1.put("eq", fSupplierId);
        and.add(eq1);

        JSONObject eq2 = new JSONObject();
        JSONObject fCompanyId = new JSONObject();
        fCompanyId.put("fInviteCompany.fId", inviteCompany.getfId());
        eq2.put("eq", fCompanyId);
        and.add(eq2);

        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsSupplierInfo> infos = appsSupplierInfoService.findAll(JSONQuerySpecification.getSpecification(query));
        if (infos.isEmpty()) {
            return null;
        }
        return infos.get(0);
    }

    /**
     * 查询 历史设计师信息
     *
     * @param main
     * @return
     */
    public List<AppsDesignerMain> findDesigner(AppsSupplierMain main) {
        //查找是否已经存在关系
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fSupplierId.fId", main.getfId());
        eq1.put("eq", fSupplierId);
        and.add(eq1);

        queryJson.put("and", and);
        query.put("query", queryJson);
        return appsDesignerMainService.findAll(JSONQuerySpecification.getSpecification(query));
    }

    /**
     * 查询 历史设计师 主创同类设计业绩
     *
     * @param main
     * @return
     */
    public List<AppsDesignPerformance> findPerformance(AppsDesignerMain main) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fDesignerId.fId", main.getfId());
        eq1.put("eq", fSupplierId);
        and.add(eq1);

        queryJson.put("and", and);
        query.put("query", queryJson);
        return appsDesignPerformanceService.findAll(JSONQuerySpecification.getSpecification(query));
    }

    /**
     * 查询 历史设计师 获奖
     *
     * @param main
     * @return
     */
    public List<AppsDesignerAwards> findAwards(AppsDesignerMain main) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fDesignerId.fId", main.getfId());
        eq1.put("eq", fSupplierId);
        and.add(eq1);

        queryJson.put("and", and);
        query.put("query", queryJson);
        return appsDesignerAwardsService.findAll(JSONQuerySpecification.getSpecification(query));
    }


    /**
     * 查询 历史 公司项目
     *
     * @param main
     * @return
     */
    public List<AppsDesignerAchievement> findItems(AppsSupplierMain main) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fSupplierId.fId", main.getfId());
        eq1.put("eq", fSupplierId);
        and.add(eq1);

        queryJson.put("and", and);
        query.put("query", queryJson);
        return appsDesignerAchievementService.findAll(JSONQuerySpecification.getSpecification(query));
    }


    /**
     * 清除公司基本信息 不包括自己
     */
    public void cleanInfos(AppsSupplierMain main, String companyId) {
        //查找是否已经存在关系
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fSupplierId.fId", main.getfId());
        eq1.put("eq", fSupplierId);
        and.add(eq1);

        JSONObject eq2 = new JSONObject();
        JSONObject fCompanyId = new JSONObject();
        fCompanyId.put("fInviteCompany.fCompanyId.fId", companyId);
        eq2.put("neq", fCompanyId);
        and.add(eq2);

        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsSupplierInfo> infos = appsSupplierInfoService.findAll(JSONQuerySpecification.getSpecification(query));
        for (AppsSupplierInfo info : infos) {
            appsSupplierInfoService.delete(info);
        }
    }

    /**
     * 清空联合体关系 不包括自己
     */
    public void cleanCompanys(AppsSupplierMain main, String companyId) {
        List<AppsSupplierInviteCompany> list = findInviteCompanyList(main.getfId(), companyId);
        for (AppsSupplierInviteCompany supplierInviteCompany : list) {
            if (!companyId.equals(supplierInviteCompany.getfCompanyId().getfId())) {
                appsSupplierInviteCompanyService.delete(supplierInviteCompany);
            }
        }
    }

    /**
     * 清空设计师
     */
    public void cleanDesigners(AppsSupplierMain main) {
        //找到全部设计师
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();
        JSONObject eq = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fSupplierId.fId", main.getfId());
        eq.put("eq", fSupplierId);
        and.add(eq);
        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsDesignerMain> dmains = appsDesignerMainService.findAll(JSONQuerySpecification.getSpecification(query));
        //清空
        for (AppsDesignerMain dmain : dmains) {
            delDesignerById(dmain.getfId());
        }
    }

    /**
     * 清空项目
     */
    public void cleanItems(AppsSupplierMain main) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();
        JSONObject eq = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fSupplierId.fId", main.getfId());
        eq.put("eq", fSupplierId);
        and.add(eq);
        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsDesignerAchievement> achievementList = appsDesignerAchievementService.findAll(JSONQuerySpecification.getSpecification(query));
        for (AppsDesignerAchievement list : achievementList) {
            appsDesignerAchievementService.delete(list);
        }
    }


    /**
     * 根据id删除设计师
     */
    public void delDesignerById(String id) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();
        JSONObject eq = new JSONObject();
        JSONObject fDesignerId = new JSONObject();
        fDesignerId.put("fDesignerId.fId", id);
        eq.put("eq", fDesignerId);
        and.add(eq);
        queryJson.put("and", and);
        query.put("query", queryJson);
        appsDesignerAwardsService.delete(JSONQuerySpecification.getSpecification(query));
        appsDesignPerformanceService.delete(JSONQuerySpecification.getSpecification(query));
        appsDesignerMainService.delete(id);
    }

    /**
     * 公司联合体 关系查询
     *
     * @param supplierId
     * @param companyId
     * @return
     */
    public List<AppsSupplierInviteCompany> findInviteCompanyList(String supplierId, String companyId) {
        //查找是否已经存在关系
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fSupplierId = new JSONObject();
        fSupplierId.put("fSupplierId.fId", supplierId);
        eq1.put("eq", fSupplierId);
        and.add(eq1);

        JSONObject eq2 = new JSONObject();
        JSONObject fCompanyId = new JSONObject();
        fCompanyId.put("fCompanyId.fId", companyId);
        eq2.put("eq", fCompanyId);
        and.add(eq2);

        queryJson.put("and", and);
        query.put("query", queryJson);
        return appsSupplierInviteCompanyService.findAll(JSONQuerySpecification.getSpecification(query));
    }


    /**
     * 获取供应商申请文件
     *
     * @param noticefId 公告id
     * @param personId  供应商账户id
     * @return
     */
    public AppsSupplierPackage getSupplierMain(String noticefId, String personId, int status, String packageId) {
        Specification<AppsSupplierPackage> spec = new Specification<AppsSupplierPackage>() {
            @Override
            public Predicate toPredicate(Root<AppsSupplierPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                // 联接 AppsSupplierPackage 实体
                Join<AppsSupplierPackage,AppsSupplierMain> packageJoin = root.join("fSupplier", JoinType.INNER);

                // 创建查询条件
                Predicate predicate = criteriaBuilder.equal(packageJoin.get("fNotice").get("fId"), noticefId);
                Predicate predicate1 = criteriaBuilder.equal(packageJoin.get("fSupplier").get("fId"), personId);
                Predicate predicate2 = criteriaBuilder.equal(packageJoin.get("fCurrentStatus"), status);
                if(packageId!=null){
                    Predicate predicate3 = criteriaBuilder.equal(root.get("fPackageId").get("fId"), packageId);
                    return criteriaBuilder.and(predicate, predicate1, predicate2, predicate3);
                }else{
                    return criteriaBuilder.and(predicate, predicate1, predicate2);
                }
                // 组合查询条件
            }

        };
        List<AppsSupplierPackage> list = appsSupplierPackageService.findAll(spec);
        if(!list.isEmpty()){
            return list.get(0);
        }
        return null;
    }

    /**
     * 获取主体公司
     *
     * @param main
     * @return
     */
    public List<AppsSupplierInviteCompany> getMainCompany(AppsSupplierMain main) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject fNotice = new JSONObject();
        fNotice.put("fSupplierId.fId", main.getfId());
        eq1.put("eq", fNotice);
        and.add(eq1);

//        JSONObject eq2 = new JSONObject();
//        JSONObject fSupplier = new JSONObject();
//        fSupplier.put("fIsMain", "true");
//        eq2.put("eq", fSupplier);
//        and.add(eq2);

        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsSupplierInviteCompany> inviteCompanyList = appsSupplierInviteCompanyService.findAll(JSONQuerySpecification.getSpecification(query));
//        if(!inviteCompanyList.isEmpty()){
//            return inviteCompanyList.get(0).getfCompanyName();
//        }
//        return null;
        return inviteCompanyList;
    }

}

