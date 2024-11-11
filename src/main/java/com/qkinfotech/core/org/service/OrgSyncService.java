package com.qkinfotech.core.org.service;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.app.config.EkpConfig;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.org.model.*;
import com.qkinfotech.core.tendering.interfaceConfig.InterfaceLog;
import com.qkinfotech.core.tendering.model.org.SitcExpert;
import com.qkinfotech.core.tendering.model.org.SitcSupplier;
import com.qkinfotech.core.user.model.SysUser;
import com.qkinfotech.util.DESEncrypt;
import com.qkinfotech.util.StringUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
@Slf4j
public class OrgSyncService {

    private Integer maxCount = 200;

    private Integer count = 0;

    @Autowired
    private EkpConfig ekpConfig;

    @Autowired
    private SimpleService<OrgElement> orgElementService;

    @Autowired
    private SimpleService<OrgCompany> orgCompanyService;

    @Autowired
    private SimpleService<OrgDept> orgDeptService;

    @Autowired
    private SimpleService<OrgPerson> orgPersonService;

    @Autowired
    private SimpleService<SysUser> sysUserService;

    @Autowired
    private SimpleService<OrgGroup> orgGroupService;

    @Autowired
    private SimpleService<OrgPost> orgPostService;

    @Autowired
    private SimpleService<OrgGroupMember> orgGroupMemberService;

    @Autowired
    private SimpleService<OrgPostMember> orgPostMemberService;


    @Autowired
    private SimpleService<SitcExpert> sitcExpertService;

    @Autowired
    private SimpleService<SitcSupplier> sitcSupplierService;

    @Autowired
    private SimpleService<InterfaceLog> interfaceLogService;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void syncCompany(Map<String, JSONObject> companyMap) throws Exception {
        count = 0;

        // 更新
        orgCompanyService.scroll(null, (OrgCompany orgCompany) -> {
            if (companyMap.containsKey(orgCompany.getfId()) && !companyMap.get(orgCompany.getfId()).containsKey("updated")) {
                updateCompany(orgCompany, companyMap.get(orgCompany.getfId()));
                companyMap.get(orgCompany.getfId()).put("updated", true);
            } else {
                orgCompany.setfValid(false);
                orgCompany.setfHibernateIds("");
                orgCompanyService.save(orgCompany);
            }
            count++;
            if (count >= maxCount) {
                orgCompanyService.flush();
                count = 0;
            }
        });
        orgCompanyService.flush();
        count = 0;

        // 新增
        for (String key : companyMap.keySet()) {
            JSONObject json = companyMap.get(key);
            if (json.containsKey("updated") && json.getBoolean("updated")) {

            } else {
                OrgCompany orgCompany = new OrgCompany(json.getString(key));
                updateCompany(orgCompany, json);
                count++;
                if (count >= maxCount) {
                    orgCompanyService.flush();
                    count = 0;
                }
            }
        }
        orgCompanyService.flush();
        count = 0;

        // 更新层级
        for (String key : companyMap.keySet()) {
            JSONObject json = companyMap.get(key);
            if (json.containsKey("fd_parentid") && StringUtil.isNotNull(json.getString("fd_parentid"))) {
                OrgCompany company = orgCompanyService.getById(key);
                OrgCompany parent = orgCompanyService.getById(json.getString("fd_parentid"));
                if (company != null && parent != null) {
                    company.setfParent(parent);
                    company.setfHibernateIds(parent.getfHibernateIds() + "_" + company.getfId());
                    orgCompanyService.save(company);
                    count++;
                    if (count >= maxCount) {
                        orgCompanyService.flush();
                        count = 0;
                    }
                }
            }
        }
        orgCompanyService.flush();
        count = 0;

    }

    public void updateCompany(OrgCompany orgCompany, JSONObject json) {
        orgCompany.setfName(json.getString("fd_name"));
        orgCompany.setfNamePinYin(json.getString("fd_name_pinyin"));
        orgCompany.setfNameSimplePinYin(json.getString("fd_name_simple_pinyin"));
        orgCompany.setfOrder(json.getString("fd_order"));
        orgCompany.setfNo(json.getString("fd_no"));
        orgCompany.setfCode(json.getString("fd_no"));
        orgCompany.setfValid(json.getBooleanValue("fd_is_available", false));
        if (orgCompany.getfValid()) {
            orgCompany.setfHibernateIds(orgCompany.getfId());
        } else {
            orgCompany.setfHibernateIds("");
        }
        orgCompanyService.save(orgCompany);
    }


    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void syncDept(Map<String, JSONObject> deptMap) throws Exception {
        count = 0;

        // 更新
        orgDeptService.scroll(null, (OrgDept orgDept) -> {
            if (deptMap.containsKey(orgDept.getfId()) && !deptMap.get(orgDept.getfId()).containsKey("updated")) {
                updateDept(orgDept, deptMap.get(orgDept.getfId()));
                deptMap.get(orgDept.getfId()).put("updated", true);
            } else {
                orgDept.setfValid(false);
                orgDept.setfHibernateIds("");
                orgDeptService.save(orgDept);
            }
            count++;
            if (count >= maxCount) {
                orgDeptService.flush();
                count = 0;
            }
        });
        orgDeptService.flush();
        count = 0;

        // 新增
        for (String key : deptMap.keySet()) {
            JSONObject json = deptMap.get(key);
            if (json.containsKey("updated") && json.getBoolean("updated")) {

            } else {
                OrgDept orgDept = new OrgDept(json.getString(key));
                updateDept(orgDept, json);
                count++;
                if (count >= maxCount) {
                    orgDeptService.flush();
                    count = 0;
                }
            }
        }
        orgDeptService.flush();
        count = 0;

        // 更新层级
        for (String key : deptMap.keySet()) {
            JSONObject json = deptMap.get(key);
            if (json.containsKey("fd_parentid") && StringUtil.isNotNull(json.getString("fd_parentid"))) {
                OrgDept orgDept = orgDeptService.getById(key);
                OrgDept parDept = orgDeptService.getById(json.getString("fd_parentid"));
                OrgCompany parCompany = orgCompanyService.getById(json.getString("fd_parentid"));
                if (orgDept != null) {
                    if (parDept != null) {
                        orgDept.setfParent(parDept);
                        orgDept.setfHibernateIds(orgDept.getfHibernateIds() + "_" + orgDept.getfId());
                    } else if (parCompany != null) {
                        orgDept.setfCompany(parCompany);
                        orgDept.setfHibernateIds(parCompany.getfHibernateIds() + "_" + orgDept.getfId());
                    }
                    orgDeptService.save(orgDept);
                    count++;
                    if (count >= maxCount) {
                        orgDeptService.flush();
                        count = 0;
                    }
                }
            }
        }
        orgDeptService.flush();
        count = 0;
    }

    public void updateDept(OrgDept orgDept, JSONObject json) {
        orgDept.setfName(json.getString("fd_name"));
        orgDept.setfNamePinYin(json.getString("fd_name_pinyin"));
        orgDept.setfNameSimplePinYin(json.getString("fd_name_simple_pinyin"));
        orgDept.setfOrder(json.getString("fd_order"));
        orgDept.setfCode(json.getString("fd_no"));
        orgDept.setfNo(json.getString("fd_no"));
        orgDept.setfValid(json.getBooleanValue("fd_is_available", false));
        if (orgDept.getfValid()) {
            orgDept.setfHibernateIds(orgDept.getfId());
        } else {
            orgDept.setfHibernateIds("");
        }
        orgDeptService.save(orgDept);
    }


    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void syncPost(Map<String, JSONObject> postMap) throws Exception {
        count = 0;

        Set<String> postIds = new HashSet<>();
        orgPostService.scroll(null, (OrgPost orgPost) -> {
            postIds.add(orgPost.getfId());
        });

        // 新增和更新
        for (String postId : postMap.keySet()) {
            JSONObject json = postMap.get(postId);
            OrgPost orgPost = null;
            if (postIds.contains(postId)) {
                orgPost = orgPostService.getById(postId);
                postIds.remove(postId);
            } else {
                orgPost = new OrgPost(postId);
            }
            updatePost(orgPost, json);
            count++;
            if (count >= maxCount) {
                orgPostService.flush();
                count = 0;
            }
        }
        orgPostService.flush();
        count = 0;

        // 无效数据
        for (String postId : postIds) {
            OrgPost orgPost = orgPostService.getById(postId);
            orgPost.setfHibernateIds("");
            orgPost.setfValid(false);
            orgPostService.save(orgPost);
            count++;
            if (count >= maxCount) {
                orgPostService.flush();
                count = 0;
            }
        }
        orgPostService.flush();
        count = 0;
    }

    public void updatePost(OrgPost orgPost, JSONObject json) {
        orgPost.setfName(json.getString("fd_name"));
        orgPost.setfNamePinYin(json.getString("fd_name_pinyin"));
        orgPost.setfNameSimplePinYin(json.getString("fd_name_simple_pinyin"));
        orgPost.setfOrder(json.getString("fd_order"));
        orgPost.setfCode(json.getString("fd_no"));
        orgPost.setfNo(json.getString("fd_no"));
        orgPost.setfValid(json.getBoolean("fd_is_available"));
        if (json.containsKey("fd_parentid") && json.get("fd_parentid") != null) {
            OrgElement element = orgElementService.getById(json.getString("fd_parentid"));
            if (element != null) {
                orgPost.setfOwner(element);
                orgPost.setfHibernateIds(element.getfHibernateIds() + "_" + orgPost.getfId());
            }
        } else {
            orgPost.setfValid(false);
            orgPost.setfOwner(null);
            orgPost.setfHibernateIds("");
        }
        orgPostService.save(orgPost);
    }


    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void syncUser(Map<String, JSONObject> personMap, Map<String, JSONObject> elementMap) throws Exception {
        count = 0;

        Set<String> userIds = new HashSet<>();
        sysUserService.scroll(null, (SysUser sysUser) -> {
            userIds.add(sysUser.getfId());
        });
        sysUserService.flush();

        // 更新和新增
        for (String userId : personMap.keySet()) {
            JSONObject personJson = personMap.get(userId);
            JSONObject eleJson = elementMap.get(userId);
            if (eleJson == null) {
                continue;
            }
            SysUser sysUser = null;
            if (userIds.contains(userId)) {
                sysUser = sysUserService.getById(userId);
                userIds.remove(userId);
            } else {
                sysUser = new SysUser();
                sysUser.setfId(userId);
            }

            updateUser(sysUser, personJson, eleJson);
            count++;
            if (count >= maxCount) {
                sysUserService.flush();
                count = 0;
            }
        }
        sysUserService.flush();
        count = 0;

        // 无效数据
        for (String userId : userIds) {
            SysUser sysUser = sysUserService.getById(userId);
            sysUser.setfDisabled(true);
            sysUserService.save(sysUser);
            count++;
            if (count >= maxCount) {
                sysUserService.flush();
                count = 0;
            }
        }
        sysUserService.flush();
        count = 0;
    }

    public void updateUser(SysUser sysUser, JSONObject personJson, JSONObject eleJson) throws Exception{
        if ("admin".equals(personJson.getString("fd_login_name"))) {
            return;
        }
        sysUser.setfLoginName(personJson.getString("fd_login_name").trim());
        sysUser.setfPassword("0");
        sysUser.setfLocked(false);
        sysUser.setfExpired(false);
        sysUser.setfDisabled(!eleJson.getBooleanValue("fd_is_available", true));
        sysUserService.save(sysUser);
    }


    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void syncPerson(Map<String, JSONObject> personMap, Map<String, JSONObject> elementMap) throws Exception {
        count = 0;

        Set<String> personIds = new HashSet<>();
        orgPersonService.scroll(null, (OrgPerson orgPerson) -> {
            personIds.add(orgPerson.getfId());
        });
        orgPersonService.flush();

        // 更新和新增
        for (String personId : personMap.keySet()) {
            JSONObject personJson = personMap.get(personId);
            JSONObject eleJson = elementMap.get(personId);
            if (eleJson == null) {
                continue;
            }
            OrgPerson person = null;
            if (personIds.contains(personId)) {
                person = orgPersonService.getById(personId);
                personIds.remove(personId);
            } else {
                person = new OrgPerson(personId);
            }
            updatePerson(person, personJson, eleJson);
            count++;
            if (count >= maxCount) {
                orgPersonService.flush();
                count = 0;
            }
        }
        orgPersonService.flush();
        count = 0;

        // 无效数据
        for (String personId : personIds) {
            OrgPerson person = orgPersonService.getById(personId);
            person.setfValid(false);
            person.setfHibernateIds("");
            orgPersonService.save(person);
            count++;
            if (count >= maxCount) {
                orgPersonService.flush();
                count = 0;
            }
        }
        orgPersonService.flush();
        count = 0;
    }

    public void updatePerson(OrgPerson orgPerson, JSONObject personJson, JSONObject eleJson) {
        if ("admin".equals(personJson.getString("fd_login_name"))) {
            return;
        }
        orgPerson.setfName(eleJson.getString("fd_name"));
        orgPerson.setfNamePinYin(eleJson.getString("fd_name_pinyin"));
        orgPerson.setfNameSimplePinYin(eleJson.getString("fd_name_simple_pinyin"));
        orgPerson.setfOrder(eleJson.getString("fd_order"));
        orgPerson.setfCode(eleJson.getString("fd_no"));
        orgPerson.setfNo(eleJson.getString("fd_no"));
        orgPerson.setfEkpUserType(personJson.getString("fd_ekp_user_type"));
        orgPerson.setfSupplierType(personJson.getString("fd_supplier_type"));
        orgPerson.setfSupplierCode(personJson.getString("fd_supplier_code"));
        orgPerson.setfSupplierLeader(personJson.getString("fd_supplier_leader"));
        orgPerson.setfSupplierContacts(personJson.getString("fd_supplier_contacts"));
        orgPerson.setfExpertCode(personJson.getString("fd_expert_code"));
        orgPerson.setfExpertBankNum(personJson.getString("fd_expert_bank_num"));
        orgPerson.setfValid(eleJson.getBooleanValue("fd_is_available", false));
        orgPerson.setfUpdateId(personJson.getString("fd_update_id"));
        if (StringUtil.isNotNull(eleJson.getString("fd_parentid"))) {
            String parentId = eleJson.getString("fd_parentid");
            OrgDept dept = orgDeptService.getById(parentId);
            if (dept != null) {
                orgPerson.setfParent(dept);
                orgPerson.setfHibernateIds(dept.getfHibernateIds() + "_" + orgPerson.getfId());
            }
        }
        orgPersonService.save(orgPerson);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void syncGroup(Map<String, JSONObject> groupMap) throws Exception {
        count = 0;
        Set<String> groupIds = new HashSet<>();
        orgGroupService.scroll(null, (OrgGroup orgGroup) -> {
            groupIds.add(orgGroup.getfId());
        });
        orgGroupService.flush();

        for (String groupId : groupMap.keySet()) {
            JSONObject groupJson = groupMap.get(groupId);
            OrgGroup orgGroup = null;
            if (groupIds.contains(groupId)) {
                orgGroup = orgGroupService.getById(groupId);
                groupIds.remove(groupId);
            } else {
                orgGroup = new OrgGroup(groupId);
            }
            updateGroup(orgGroup, groupJson);
            count++;
            if (count >= maxCount) {
                orgGroupService.flush();
                count = 0;
            }
        }
        orgGroupService.flush();
        count = 0;

        for (String groupId : groupIds) {
            OrgGroup orgGroup = orgGroupService.getById(groupId);
            orgGroup.setfValid(false);
            orgGroupService.save(orgGroup);
            count++;
            if (count >= maxCount) {
                orgGroupService.flush();
                count = 0;
            }
        }
        orgGroupService.flush();
        count = 0;
    }

    public void updateGroup(OrgGroup orgGroup, JSONObject groupJson) {
        orgGroup.setfName(groupJson.getString("fd_name"));
        orgGroup.setfNamePinYin(groupJson.getString("fd_name_pinyin"));
        orgGroup.setfOrder(groupJson.getString("fd_order"));
        orgGroup.setfCode(groupJson.getString("fd_no"));
        orgGroup.setfValid(groupJson.getBooleanValue("fd_is_available", false));
        orgGroupService.save(orgGroup);
    }


    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void syncPostMembers(Set<JSONObject> postMembersJson) throws Exception {
        Map<String, OrgPostMember> postMembersMap = new HashMap<>();
        orgPostMemberService.scroll(null, (OrgPostMember orgPostMember) -> {
            postMembersMap.put(orgPostMember.getfPost().getfId() + "-" + orgPostMember.getfElement().getfId(), orgPostMember);
        });

        for (JSONObject postMemberJson : postMembersJson) {
            if (postMembersMap.containsKey(postMemberJson.getString("fd_postid") + "-" + postMemberJson.getString("fd_personid"))) {
                postMembersMap.remove(postMemberJson.getString("fd_postid") + "-" + postMemberJson.getString("fd_personid"));
            } else {
                OrgPostMember orgPostMember = new OrgPostMember();
                orgPostMember.getfId();
                orgPostMember.setfPost(orgPostService.getById(postMemberJson.getString("fd_postid")));
                orgPostMember.setfElement(orgElementService.getById(postMemberJson.getString("fd_personid")));
                orgPostMemberService.save(orgPostMember);
                count++;
                if (count >= maxCount) {
                    orgPostMemberService.flush();
                    count = 0;
                }
            }
        }
        orgPostMemberService.flush();
        count = 0;

        for (OrgPostMember member : postMembersMap.values()) {
            orgPostMemberService.delete(member);
            count++;
            if (count >= maxCount) {
                orgPostMemberService.flush();
                count = 0;
            }
        }
        orgPostMemberService.flush();
        count = 0;
    }


    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void syncGroupMembers(Set<JSONObject> groupMembersJson) throws Exception {
        Map<String, OrgGroupMember> groupMembersMap = new HashMap<>();
        orgGroupMemberService.scroll(null, (OrgGroupMember orgGroupMember) -> {
            groupMembersMap.put(orgGroupMember.getfGroup().getfId() + "-" + orgGroupMember.getfElement().getfId(), orgGroupMember);
        });

        for (JSONObject groupMemberJson : groupMembersJson) {
            if (groupMembersMap.containsKey(groupMemberJson.getString("fd_groupid") + "-" + groupMemberJson.getString("fd_elementid"))) {
                groupMembersMap.remove(groupMemberJson.getString("fd_groupid") + "-" + groupMemberJson.getString("fd_elementid"));
            } else {
                OrgGroupMember orgGroupMember = new OrgGroupMember();
                orgGroupMember.getfId();
                orgGroupMember.setfGroup(orgGroupService.getById(groupMemberJson.getString("fd_groupid")));
                orgGroupMember.setfElement(orgElementService.getById(groupMemberJson.getString("fd_elementid")));
                orgGroupMemberService.save(orgGroupMember);
                count++;
                if (count >= maxCount) {
                    orgGroupMemberService.flush();
                    count = 0;
                }
            }
        }
        orgGroupMemberService.flush();
        count = 0;

        for (OrgGroupMember member : groupMembersMap.values()) {
            orgGroupMemberService.delete(member);
            count++;
            if (count >= maxCount) {
                orgGroupMemberService.flush();
                count = 0;
            }
        }
        orgGroupMemberService.flush();
        count = 0;

    }



    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void syncSupplier(Map<String, JSONObject> supplierMap) throws Exception {
        count = 0;
        for (String supplierId : supplierMap.keySet()) {
            JSONObject json = supplierMap.get(supplierId);
            SitcSupplier supplier = sitcSupplierService.getById(supplierId);
            if (supplier == null) {
                supplier = new SitcSupplier();
                supplier.setfId(supplierId);
                supplier.setfOrgId(supplierId);
                supplier.setfName(json.getString("fd_name"));
                supplier.setfEmail(json.getString("fd_email"));
                supplier.setfSupplierType(json.getString("fd_supplier_type"));
                supplier.setfCreditCode(json.getString("fd_credit_code"));
                supplier.setfFullName(json.getString("fd_full_name"));
                supplier.setfArea(json.getString("fd_area"));
                supplier.setfAttest(json.getString("fd_attest"));
                supplier.setfCode(json.getString("fd_code"));
                supplier.setfPrincipal(json.getString("fd_principal"));
                supplier.setfContactPerson(json.getString("fd_contact_person"));
                supplier.setfWeixin(json.getString("fd_weixin"));
                supplier.setfTaxpayerType(json.getString("fd_taxpayer_type"));
                supplier.setfBillingAddress(json.getString("fd_billing_address"));
                supplier.setfBillingPhone(json.getString("fd_billing_phone"));
                supplier.setfAddress(json.getString("fd_address"));
                supplier.setfPostcode(json.getString("fd_postcode"));
                supplier.setfTelephone(json.getString("fd_telephone"));
                supplier.setfFax(json.getString("fd_fax"));
                supplier.setfBusiness(json.getString("fd_business"));
                supplier.setfWebsite(json.getString("fd_website"));
                supplier.setfTaxpayer(json.getString("fd_taxpayer"));
                supplier.setfBank(json.getString("fd_bank"));
                supplier.setfAccount(json.getString("fd_account"));
                supplier.setfDepositBank(json.getString("fd_deposit_bank"));
                supplier.setfDepositAccount(json.getString("fd_deposit_account"));
                supplier.setfAccountCode(json.getString("fd_account_code"));
                supplier.setfUpperLimit(json.getInteger("fd_upper_limit"));
                supplier.setfLinkmanTel(json.getString("fd_linkman_tel"));
                supplier.setfParentId(json.getString("fd_parent_id"));
                supplier.setfNumber(json.getString("fd_number"));
                supplier.setfOpenId(json.getString("fd_open_id"));
                supplier.setfNickName(json.getString("fd_nick_name"));
                supplier.setfDentify(json.getString("fd_dentify"));
                supplier.setfSupplierId(json.getString("fd_supplier_id"));
                supplier.setfPapersNumber(json.getString("fd_papers_number"));
                supplier.setfSupplierIndustry(json.getString("fd_supplier_industry"));
                sitcSupplierService.save(supplier);
                count++;

                //事务提交
                if (count >= maxCount) {
                    sitcSupplierService.flush();
                    count = 0;
                }
            }
        }
        sitcSupplierService.flush();
        count = 0;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void syncExpert(Map<String, JSONObject> expertMap) throws Exception {
        count = 0;
        for (String expertId : expertMap.keySet()) {
            JSONObject json = expertMap.get(expertId);
            SitcExpert expert = sitcExpertService.getById(expertId);
            if (expert == null) {
                expert = new SitcExpert();
                expert.setfId(expertId);
                expert.setfOrgId(expertId);
                expert.setfName(json.getString("fd_name"));
                expert.setfSex(json.getString("fd_sex"));
                expert.setfPapersType(json.getString("fd_papers_type"));
                expert.setfPapersNumber(json.getString("fd_papers.number"));
                expert.setfGraduateSchool(json.getString("fd_graduate_school"));
                expert.setfMajor(json.getString("fd_major"));
                expert.setfEducation(json.getString("fd_education"));
                expert.setfDegree(json.getString("fd_degree"));
                expert.setfWorkUnit(json.getString("fd_work_unit"));
                expert.setfOccupation(json.getString("fd_occupation"));
                expert.setfEmail(json.getString("fd_email"));
                expert.setfWexin(json.getString("fd_wexin"));
                expert.setfPostal(json.getString("fd_postal"));
                expert.setfHomePhone(json.getString("fd_home_phone"));
                expert.setfPostalCode(json.getString("fd_postal_code"));
                expert.setfInstancy(json.getString("fd_instancy"));
                expert.setfPresence(json.getString("fd_presence"));
                expert.setfIssuingBank(json.getString("fd_issuing_bank"));
                expert.setfBankNumber(json.getString("fd_bank_number"));
                expert.setfIsExpert(json.getString("fd_is_expert"));
                expert.setfBuileType(json.getString("fd_buile_type"));
                expert.setfWorking(json.getString("fd_working"));
                expert.setfExperience(json.getString("fd_experience"));
                expert.setfAttication(json.getString("fd_attication"));
                expert.setfBirthdate(json.getDate("fd_birthdate"));
                expert.setfAge(json.getInteger("fd_age"));
                expert.setfSenior(json.getString("fd_senior"));
                expert.setfIsEme(json.getBoolean("fd_is_eme"));
                expert.setfNumber(json.getString("fd_number"));
                expert.setfOriginalId(json.getString("fd_original_id"));
                expert.setfRemarks(json.getDouble("fd_remarks"));
                expert.setfPersonId(json.getString("fd_person_id"));
                expert.setfDentify(json.getString("fd_dentify"));
                expert.setfOpenId(json.getString("fd_open_id"));
                expert.setfNickName(json.getString("fd_nick_name"));
                expert.setfSeat(json.getString("fd_seat"));
                sitcExpertService.save(expert);
                count++;

                //事务提交
                if (count >= maxCount) {
                    sitcExpertService.flush();
                    count = 0;
                }
            }
        }
        sitcExpertService.flush();
        count = 0;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void saveLog(InterfaceLog log) {
        interfaceLogService.save(log);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateUser(JSONObject personJson, JSONObject elementJson) throws Exception {
        SysUser user = sysUserService.getById(personJson.getString("fd_id"));
        if (user == null) {
            user = new SysUser();
            user.setfId(personJson.getString("fd_id"));
        }
        updateUser(user, personJson, elementJson);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updatePerson(JSONObject personJson, JSONObject elementJson) throws Exception {
        OrgPerson person = orgPersonService.getById(personJson.getString("fd_id"));
        if (person == null) {
            person = new OrgPerson();
            person.setfId(personJson.getString("fd_id"));
        }
        updatePerson(person, personJson, elementJson);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateSupplier(JSONObject supplierJson) throws Exception {
        Map<String, JSONObject> supplierMap = new HashMap<>();
        supplierMap.put(supplierJson.getString("fd_id"), supplierJson);
        syncSupplier(supplierMap);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateExpert(JSONObject expertJson) throws Exception {
        Map<String, JSONObject> expertMap = new HashMap<>();
        expertMap.put(expertJson.getString("fd_id"), expertJson);
        syncExpert(expertMap);
    }

}
