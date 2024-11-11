package com.qkinfotech.core.org.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import com.qkinfotech.core.tendering.model.org.SitcExpert;
import com.qkinfotech.core.tendering.model.org.SitcSupplier;
import com.qkinfotech.core.user.model.SysUser;
import com.qkinfotech.util.DESEncrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.app.config.EkpConfig;
import com.qkinfotech.core.app.model.SysConfig;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.task.ITask;
import com.qkinfotech.core.task.Task;
import com.qkinfotech.core.task.TaskLogger;
import com.qkinfotech.core.org.model.OrgBase;
import com.qkinfotech.core.org.model.OrgCompany;
import com.qkinfotech.core.org.model.OrgDept;
import com.qkinfotech.core.org.model.OrgElement;
import com.qkinfotech.core.org.model.OrgGroup;
import com.qkinfotech.core.org.model.OrgGroupCate;
import com.qkinfotech.core.org.model.OrgGroupMember;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.org.model.OrgPost;
import com.qkinfotech.core.org.model.OrgPostMember;
import com.qkinfotech.util.StringUtil;

@Task(trigger = "cron:0 0 1 * * ?", group = "Org", name = "组织架构同步定时任务")
public class OrgSyncTask implements ITask {

	@Autowired
	private SimpleService<OrgCompany> orgCompanyService;

	@Autowired
	private SimpleService<OrgDept> orgDeptService;

	@Autowired
	private SimpleService<OrgPerson> orgPersonService;

	@Autowired
	private SimpleService<OrgElement> orgElementService;

	@Autowired
	private SimpleService<OrgPost> orgPostService;

	@Autowired
	private SimpleService<OrgPostMember> orgPostMemberService;

	@Autowired
	private SimpleService<OrgGroup> orgGroupService;

	@Autowired
	private SimpleService<OrgGroupCate> orgGroupCateService;

	@Autowired
	private SimpleService<OrgGroupMember> orgGroupMemberService;

	@Autowired
	private SimpleService<SysUser> sysUserService;

	@Autowired
	private SimpleService<SitcExpert> sitcExpertService;

	@Autowired
	private SimpleService<SitcSupplier> sitcSupplierService;

	@Autowired
	private EkpConfig ekpConfig;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private Connection conn = null;

	private TransactionStatus status = null;

	private Integer count = 0;

	private TaskLogger logger = null;

	//当前环境的公司数据
	private Map<String, OrgCompany> companyMap = new HashMap<String, OrgCompany>();
	private Map<String, JSONObject> syncCompanyMap = new HashMap<String, JSONObject>();

	//当前环境的部门数据
	private Map<String, OrgDept> deptMap = new HashMap<String, OrgDept>();
	private Map<String, JSONObject> syncDeptMap = new HashMap<String, JSONObject>();

	private Map<String, OrgGroupCate> groupCateMap = new HashMap<String, OrgGroupCate>();

	@Override
	public void execute(TaskLogger logger, JSONObject parameter) throws Exception {
		Date startTime = new Date();
		try {
			this.logger = logger;
			logger.write("========== start sync organization ==========");
			//1.链接数据库
			logger.write(" connect database ");
			String bean = "ekpConfig";
			JSONObject queryJson = new JSONObject();
			JSONObject equal = new JSONObject();
			equal.put("fModelName", bean);
			queryJson.put("equals", equal);

			conn = DriverManager.getConnection(
					ekpConfig.getEkpDatabaseUrl(),
					ekpConfig.getEkpDatabaseUsername(),
					ekpConfig.getEkpDatabasePassword());

			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
			status = transactionManager.getTransaction(def);

			//先获取当前的机构部门数据，方便后期层级信息同步的处理
			orgDeptService.scroll(null,
					(OrgDept) -> {
						this.deptMap.put(OrgDept.getfId(), OrgDept);
					});
			orgCompanyService.scroll(null,
					(OrgCompany) -> {
						this.companyMap.put(OrgCompany.getfId(), OrgCompany);
					});

			//2.同步机构（新建有效机构信息）
			logger.write(" start sync company ");
			System.out.println(" start sync company ");
			syncCompany();

			//3.同步机构信息和机构层级信息
			if (!this.syncCompanyMap.isEmpty()) {
				logger.write(" start sync company info ");
				System.out.println(" start sync company info ");
				syncCompanyInfo();
			}

			//4.同步部门信息（新建有效部门信息）
			logger.write(" start sync dept ");
			System.out.println(" start sync dept");
			syncDept();

			//5.同步部门信息
			if (!this.syncDeptMap.isEmpty()) {
				logger.write(" start sync dept info ");
				System.out.println(" start sync dept info ");
				syncDeptInfo();
			}

			try {
				if (Objects.nonNull(conn)){
					try {
						conn.close();
						conn = DriverManager.getConnection(
								ekpConfig.getEkpDatabaseUrl(),
								ekpConfig.getEkpDatabaseUsername(),
								ekpConfig.getEkpDatabasePassword());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			//6.同步人员和人员层级信息
			logger.write(" start sync person ");
			System.out.println(" start sync person");
			syncUser();
			try {
				if (Objects.nonNull(conn)){
					try {
						conn.close();
						conn = DriverManager.getConnection(
								ekpConfig.getEkpDatabaseUrl(),
								ekpConfig.getEkpDatabaseUsername(),
								ekpConfig.getEkpDatabasePassword());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			syncPerson();
			try {
				if (Objects.nonNull(conn)){
					try {
						conn.close();
						conn = DriverManager.getConnection(
								ekpConfig.getEkpDatabaseUrl(),
								ekpConfig.getEkpDatabaseUsername(),
								ekpConfig.getEkpDatabasePassword());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			syncSupplier();
			try {
				if (Objects.nonNull(conn)){
					try {
						conn.close();
						conn = DriverManager.getConnection(
								ekpConfig.getEkpDatabaseUrl(),
								ekpConfig.getEkpDatabaseUsername(),
								ekpConfig.getEkpDatabasePassword());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			syncExport();
			try {
				if (Objects.nonNull(conn)){
					try {
						conn.close();
						conn = DriverManager.getConnection(
								ekpConfig.getEkpDatabaseUrl(),
								ekpConfig.getEkpDatabaseUsername(),
								ekpConfig.getEkpDatabasePassword());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			//7.同步岗位信息
			logger.write(" start sync post ");
			System.out.println(" start sync post");
			syncPost();

			//8.同步岗位人员信息
			logger.write(" start sync post person ");
			System.out.println(" start sync post person");
			syncPostPerson();

			//9.同步群组信息和群组分类信息
			logger.write(" start sync group ");
			System.out.println(" start sync group");
			syncGroupCate();
			syncGroup();

			//10.同步群组人员信息
			logger.write(" start sync group person ");
			System.out.println(" start sync group person");
			syncGroupPerson();

			if (Objects.nonNull(status)) {
				transactionManager.commit(status);
			}
		} catch (Exception e) {
			e.printStackTrace();
			transactionManager.rollback(status);
			logger.write(e);
			throw e;
		} finally {
			if (Objects.nonNull(conn)){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			logger.write("========== end sync organization ==========");
			// 清理全局数据
			this.companyMap = new HashMap<String, OrgCompany>();
			this.syncCompanyMap = new HashMap<String, JSONObject>();
			this.deptMap = new HashMap<String, OrgDept>();
			this.syncDeptMap = new HashMap<String, JSONObject>();
			this.groupCateMap = new HashMap<String, OrgGroupCate>();
		}
		Date finishTime = new Date();
		System.out.println(" sync org finish, cost time : " + (finishTime.getTime() - startTime.getTime()) + "ms");
		logger.write(" sync org finish, cost time : " + (finishTime.getTime() - startTime.getTime()) + "ms");
	}

	/**
	 * 同步机构信息，新增新机构
	 */
	public void syncCompany() throws Exception {
		/* 待同步数据 */
		Map<String, JSONObject> syncMap = scroll(" select * from sys_org_element where fd_org_type = ? ", new String[]{"1"}, "fd_id");

		for (JSONObject json : syncMap.values()) {
			String fId = json.getString("fd_id");
			/* 数据或者其任一父级数据为无效，则不新建/置为无效 */
			boolean isAvailable = isAvailable(json, syncMap, new HashMap());

			if (companyMap.containsKey(fId)) {
				/* 更新已有数据的有效性，但不提交，在syncCompanyInfo中update */
				OrgCompany company = companyMap.get(fId);
				company.setfValid(isAvailable);
			} else {
				/* 新建有效的组织架构数据，并放入companyMap中 */
				if (!isAvailable) {
					continue;
				}
				OrgCompany company = new OrgCompany(fId);
				company.setfName(json.getString("fd_name"));
				company.setfNamePinYin(json.getString("fd_name_pinyin"));
				company.setfNameSimplePinYin(json.getString("fd_name_simple_pinyin"));
				company.setfOrder(json.getString("fd_order"));
				company.setfNo(json.getString("fd_no"));
				company.setfCode(json.getString("fd_no"));
				company.setfValid(true);
				//logger.write(" add new company : " + company.getfName());
				orgCompanyService.save(company);
				companyMap.put(fId, company);
				count++;
			}

			//事务提交
			if (count >= 200) {
				status.flush();
				count = 0;
			}
		}
		status.flush();
		count = 0;
		this.syncCompanyMap = syncMap;
	}

	/**
	 * 同步机构信息和机构层级信息
	 */
	public void syncCompanyInfo() throws Exception {
		for (OrgCompany company : companyMap.values()) {
			String id = company.getfId();
			/* 更新机构信息，不在syncMap里面的数据不参与同步更新 */
			if (syncCompanyMap.containsKey(id) ) {
				JSONObject json = syncCompanyMap.get(id);
				company.setfName(json.getString("fd_name"));
				company.setfNamePinYin(json.getString("fd_name_pinyin"));
				company.setfNameSimplePinYin(json.getString("fd_name_simple_pinyin"));
				company.setfOrder(json.getString("fd_order"));
				company.setfNo(json.getString("fd_no"));
				company.setfCode(json.getString("fd_no"));
				company.setfParent(companyMap.get(json.getString("fd_parentid")));
				orgCompanyService.save(company);
				count++;
				//logger.write("sync company : " + company.getfName() + ", available : " + company.getfValid());
			}

			//事务提交
			if (count >= 200) {
				status.flush();
				count = 0;
			}
		}
		status.flush();
		count = 0;

		//更新机构hibernateIds
		for (OrgCompany company : companyMap.values()) {
			String id = company.getfId();
			/* 更新机构信息，不在syncMap里面的数据不参与同步更新 */
			if (syncCompanyMap.containsKey(id) ) {
				String ids = "";
				OrgCompany parent = company;
				while (parent != null) {
					ids = parent.getfId() + "_" + ids;
					parent = parent.getfParent();
				}
				company.setfHibernateIds(ids);
				orgElementService.save(company);
				count ++;

				//事务提交
				if (count >= 200) {
					status.flush();
					count = 0;
				}
			}
		}
		status.flush();
		count = 0;
	}

	/**
	 * 同步部门信息，新建新部门
	 */
	public void syncDept() throws Exception {
		Map<String, JSONObject> syncMap = scroll(" select * from sys_org_element where fd_org_type = ? ", new String[]{"2"}, "fd_id");
		for (JSONObject json : syncMap.values()) {
			String fId = json.getString("fd_id");

			/* 数据或者其任一父级数据为无效，则不新建/置为无效 */
			boolean isAvailable = isAvailable(json, this.syncCompanyMap, syncMap);

			if (deptMap.containsKey(fId)) {
				/* 更新已有数据的有效性，但不提交，在syncDeptInfo中update */
				OrgDept dept = deptMap.get(fId);
				dept.setfValid(isAvailable);
			} else {
				/* 新建有效的组织架构数据，并放入deptMap中 */
				if (!isAvailable) {
					continue;
				}
				OrgDept dept = new OrgDept(fId);
				dept.setfName(json.getString("fd_name"));
				dept.setfNamePinYin(json.getString("fd_name_pinyin"));
				dept.setfNameSimplePinYin(json.getString("fd_name_simple_pinyin"));
				dept.setfOrder(json.getString("fd_order"));
				dept.setfCode(json.getString("fd_no"));
				dept.setfNo(json.getString("fd_no"));
				dept.setfValid(true);
				logger.write(" add new dept : " + dept.getfName());
				orgDeptService.save(dept);
				deptMap.put(fId, dept);
				count++;
			}

			//事务提交
			if (count >= 200) {
				status.flush();
				count = 0;
			}
		}
		status.flush();
		count = 0;
		this.syncDeptMap = syncMap;
	}

	/**
	 * 同步部门信息和层级信息
	 */
	public void syncDeptInfo() throws Exception {
		for (OrgDept dept : deptMap.values()) {
			String id = dept.getfId();
			if (syncDeptMap.containsKey(id) ) {
				JSONObject json = syncDeptMap.get(id);
				dept.setfName(json.getString("fd_name"));
				dept.setfNamePinYin(json.getString("fd_name_pinyin"));
				dept.setfNameSimplePinYin(json.getString("fd_name_simple_pinyin"));
				dept.setfOrder(json.getString("fd_order"));
				dept.setfCode(json.getString("fd_no"));
				dept.setfNo(json.getString("fd_no"));
				if (deptMap.containsKey(json.getString("fd_parentid"))) {
					dept.setfParent(deptMap.get(json.getString("fd_parentid")));
				} else if (companyMap.containsKey(json.getString("fd_parentid"))) {
					dept.setfCompany(companyMap.get(json.getString("fd_parentid")));
				}
				orgDeptService.save(dept);
				count++;
				//logger.write("sync dept : " + dept.getfName() + ", available : " + dept.getfValid());
			}

			//事务提交
			if (count >= 200) {
				status.flush();
				count = 0;
			}
		}
		status.flush();
		count = 0;

		//更新hibernateIds
		for (OrgDept dept : deptMap.values()) {
			String id = dept.getfId();
			if (syncDeptMap.containsKey(id) ) {
				String ids = "";
				OrgDept parent = dept;
				OrgCompany company = dept.getfCompany();
				while (parent != null) {
					ids = parent.getfId() + "_" + ids;
					company = parent.getfCompany();
					parent = parent.getfParent();
				}
				while (company != null) {
					ids = company.getfId() + "_" + ids;
					company = company.getfParent();
				}
				dept.setfHibernateIds(ids);
				orgElementService.save(dept);
				count ++;

				//事务提交
				if (count >= 200) {
					status.flush();
					count = 0;
				}
			}
		}
		status.flush();
		count = 0;
	}

	public void syncUser() throws Exception {
		DESEncrypt encrypt = new DESEncrypt("kmssPropertiesKey");
		PreparedStatement statement = null;
		ResultSet rs = null;
		/* 现有人员数据 */
		Map<String, SysUser> userMap = new HashMap<>();
		sysUserService.scroll(null,
				(SysUser) -> {
					userMap.put(SysUser.getfId(), SysUser);
				});
		try {
			OrgPerson temp = new OrgPerson();

			statement = conn.prepareStatement(" select * from sys_org_element e inner join sys_org_person p on e.fd_id = p.fd_id where e.fd_org_type = ? ");
			statement.setString(1, "8");
			rs = statement.executeQuery();

			ResultSetMetaData data = rs.getMetaData();
			int columnNum = data.getColumnCount();

			while(rs.next()) {
				JSONObject json = new JSONObject();
				for (int i=1; i<=columnNum; i++) {
					String name = data.getColumnName(i);
					Object value = rs.getObject(i);
					json.put(name, value);
				}
				/* sys_user同步跳过admin， 避免出现两个admin */
				if (json.getString("fd_login_name").equals("admin")) {
					continue;
				}

				String fId = json.getString("fd_id");
				boolean isAvailable = isAvailable(json, this.syncCompanyMap, this.syncDeptMap); // 人员有效性
				SysUser user = userMap.get(fId);
				if (user == null) {
					user = new SysUser();
					user.setfId(fId);
				}
				user.setfLoginName(json.getString("fd_login_name").trim());
				try {
					if (StringUtil.isNull(json.getString("fd_init_password"))) {
						user.setfPassword("0");
					} else {
						user.setfPassword(encrypt.decryptString(json.getString("fd_init_password")));
					}
				} catch (Exception e) {
					//logger.write("save password field : " + user.getfLoginName());
				}
				user.setfDisabled(!isAvailable);

				sysUserService.save(user);
				count++;

				//事务提交
				if (count >= 200) {
					status.flush();
					count = 0;
				}
			}

			status.flush();
			count = 0;
		} finally {
			if (Objects.nonNull(rs)) {
				rs.close();
			}
			if (Objects.nonNull(statement)){
				statement.close();
			}
		}
	}

	/**
	 * 同步人员和人员层级信息
	 */
	public void syncPerson () throws Exception {
		PreparedStatement statement = null;
		ResultSet rs = null;
		/* 现有人员数据 */
		/*Map<String, OrgPerson> personMap = new HashMap<String, OrgPerson>();
		orgPersonService.scroll(null,
				(OrgPerson) -> {
					personMap.put(OrgPerson.getfId(), OrgPerson);
				});
		Map<String, SysUser> userMap = new HashMap<>();
		sysUserService.scroll(null,
				(SysUser) -> {
					userMap.put(SysUser.getfId(), SysUser);
				});*/

		try {
			statement = conn.prepareStatement(
					" select * from sys_org_element e inner join sys_org_person p on e.fd_id = p.fd_id " +
							/*"left join km_supplier_person s on s.fd_id = p.fd_update_id or (p.fd_update_id is null and s.fd_id = e.fd_id) " +
							"left join km_expert_person x on x.fd_id = p.fd_update_id or (p.fd_update_id is null and x.fd_id = e.fd_id) " +*/
							"where e.fd_org_type = ? "
			);
			statement.setString(1, "8");
			rs = statement.executeQuery();

			ResultSetMetaData data = rs.getMetaData();
			int columnNum = data.getColumnCount();

			while(rs.next()) {
				JSONObject json = new JSONObject();
				for (int i=1; i<=columnNum; i++) {
					String name = data.getTableName(i) + "." + data.getColumnName(i);
					Object value = rs.getObject(i);
					json.put(name, value);
					if (name.equals("sys_org_element.fd_parentid")) {
						json.put("fd_parentid", value);
					}
					if (name.equals("sys_org_element.fd_is_available")) {
						json.put("fd_is_available", value);
					}
					if (name.equals("sys_org_element.fd_org_type")) {
						json.put("fd_org_type", value);
					}
				}

				String fId = json.getString("sys_org_element.fd_id");
				boolean isAvailable = isAvailable(json, this.syncCompanyMap, this.syncDeptMap); // 人员有效性
				OrgPerson person = orgPersonService.getById(fId);
				if (person == null) {
					if (!isAvailable) {
						continue;
					}
					person = new OrgPerson();
					person.setfId(fId);
					person.setfUser(sysUserService.getById(fId));
				}

				person.setfName(json.getString("sys_org_element.fd_name"));
				person.setfNamePinYin(json.getString("sys_org_element.fd_name_pinyin"));
				person.setfNameSimplePinYin(json.getString("sys_org_element.fd_name_simple_pinyin"));
				person.setfOrder(json.getString("sys_org_element.fd_order"));
				person.setfCode(json.getString("sys_org_element.fd_no"));
				person.setfNo(json.getString("sys_org_element.fd_no"));
				person.setfEkpUserType(json.getString("sys_org_person.fd_ekp_user_type"));
				person.setfSupplierType(json.getString("sys_org_person.fd_supplier_type"));
				person.setfSupplierCode(json.getString("sys_org_person.fd_supplier_code"));
				person.setfSupplierLeader(json.getString("sys_org_person.fd_supplier_leader"));
				person.setfSupplierContacts(json.getString("sys_org_person.fd_supplier_contacts"));
				person.setfExpertCode(json.getString("sys_org_person.fd_expert_code"));
				person.setfExpertBankNum(json.getString("sys_org_person.fd_expert_bank_num"));
				person.setfValid(isAvailable);
				person.setfUpdateId(json.getString("sys_org_person.fd_update_id"));
				if (StringUtil.isNotNull(json.getString("fd_parentid"))) {
					OrgDept parentDept = deptMap.get(json.getString("fd_parentid"));
					person.setfParent(parentDept);
					person.setfHibernateIds(parentDept.getfHibernateIds() + "_" + person.getfId());
				}

				orgPersonService.save(person);
				count++;

				//事务提交
				if (count >= 200) {
					status.flush();
					count = 0;
				}
			}

			status.flush();
			count = 0;
		} finally {
			if (Objects.nonNull(rs)) {
				rs.close();
			}
			if (Objects.nonNull(statement)){
				statement.close();
			}
		}
	}

	/**
	 * 同步供应商
	 */
	public void syncSupplier() throws Exception {
		PreparedStatement statement = null;
		ResultSet rs = null;
		Map<String, SitcSupplier> supplierMap = new HashMap<>();
		sitcSupplierService.scroll(null, (sitcSupplier) -> {
			supplierMap.put(sitcSupplier.getfId(), sitcSupplier);
		});
		try {
			statement = conn.prepareStatement(" select * from km_supplier_person order by doc_create_time desc ");
			rs = statement.executeQuery();
			ResultSetMetaData data = rs.getMetaData();
			int columnNum = data.getColumnCount();

			while(rs.next()) {
				JSONObject json = new JSONObject();
				for (int i = 1; i <= columnNum; i++) {
					String name = data.getColumnName(i);
					Object value = rs.getObject(i);
					json.put(name, value);
				}

				String fId = json.getString("fd_id");
				if (!supplierMap.containsKey(fId)) {
					SitcSupplier supplier = new SitcSupplier();
					supplier.setfId(fId);
					supplier.setfOrgId(fId);
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
					if (count >= 200) {
						status.flush();
						count = 0;
					}
				}
			}

			status.flush();
			count = 0;
		} catch ( Exception e) {

		} finally {
			if (Objects.nonNull(rs)) {
				rs.close();
			}
			if (Objects.nonNull(statement)) {
				statement.close();
			}
		}
	}

	/**
	 * 同步专家
	 */
	public void syncExport() throws Exception {

		PreparedStatement statement = null;
		ResultSet rs = null;
		Map<String, SitcExpert> exportMap = new HashMap<>();
		sitcExpertService.scroll(null, (sitcExpert) -> {
			exportMap.put(sitcExpert.getfId(), sitcExpert);
		});
		try {
			statement = conn.prepareStatement(" select * from km_expert_person order by doc_create_time desc ");
			rs = statement.executeQuery();
			ResultSetMetaData data = rs.getMetaData();
			int columnNum = data.getColumnCount();

			while(rs.next()) {
				JSONObject json = new JSONObject();
				for (int i = 1; i <= columnNum; i++) {
					String name = data.getColumnName(i);
					Object value = rs.getObject(i);
					json.put(name, value);
				}

				String fId = json.getString("fd_id");
				if (!exportMap.containsKey(fId)) {
					SitcExpert expert = new SitcExpert();
					expert.setfId(fId);
					expert.setfOrgId(fId);
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
					if (count >= 200) {
						status.flush();
						count = 0;
					}
				}
			}

			status.flush();
			count = 0;
		} catch ( Exception e) {

		} finally {
			if (Objects.nonNull(rs)) {
				rs.close();
			}
			if (Objects.nonNull(statement)) {
				statement.close();
			}
		}
	}

	/**
	 * 同步岗位
	 */
	public void syncPost() throws Exception {
		PreparedStatement statement = null;
		ResultSet rs = null;
		Map<String, OrgPost> postMap = new HashMap<String, OrgPost>();
		orgPostService.scroll(null,
				(OrgPost) -> {
					postMap.put(OrgPost.getfId(), OrgPost);
				});
		try {
			statement = conn.prepareStatement(" select * from sys_org_element where fd_org_type = ? ");
			statement.setString(1, "4");
			rs = statement.executeQuery();

			ResultSetMetaData data = rs.getMetaData();
			int columnNum = data.getColumnCount();

			while(rs.next()) {
				JSONObject json = new JSONObject();
				for (int i=1; i<=columnNum; i++) {
					String name = data.getColumnName(i);
					Object value = rs.getObject(i);
					json.put(name, value);
				}
				String fId = json.getString("fd_id");
				boolean isAvailable = isAvailable(json, this.syncCompanyMap, this.syncDeptMap);
				OrgPost post = postMap.get(fId);
				if (post == null) {
					if (!isAvailable) {
						continue;
					}
					post = new OrgPost(fId);
					logger.write(" add new post : " + post.getfId());
				} else {
					//logger.write(" update post : " + post.getfName() + ", available : " + isAvailable);
				}
				post.setfValid(isAvailable);
				post.setfName(json.getString("fd_name"));
				post.setfNamePinYin(json.getString("fd_name_pinyin"));
				post.setfOrder(json.getString("fd_order"));
				post.setfCode(json.getString("fd_no"));

				if (json.containsKey("fd_parentid") && json.get("fd_parentid") != null) {
					post.setfOwner(orgElementService.getById(json.getString("fd_parentid")));
					post.setfHibernateIds(post.getfOwner().getfHibernateIds() + "_" + post.getfId());
				}
				orgPostService.save(post);
				count++;

				//事务提交
				if (count >= 200) {
					status.flush();
					count = 0;
				}
			}
		} finally {
			if (Objects.nonNull(rs)) {
				rs.close();
			}
			if (Objects.nonNull(statement)){
				statement.close();
			}
		}
	}

	/**
	 * 同步群组
	 */
	public void syncGroup() throws Exception {
		PreparedStatement statement = null;
		ResultSet rs = null;
		Map<String, OrgGroup> groupMap = new HashMap<String, OrgGroup>();
		orgGroupService.scroll(null,
				(OrgGroup) -> {
					groupMap.put(OrgGroup.getfId(), OrgGroup);
				});
		try {
			statement = conn.prepareStatement(" select * from sys_org_element where fd_org_type = ? ");
			statement.setString(1, "16");
			rs = statement.executeQuery();

			ResultSetMetaData data = rs.getMetaData();
			int columnNum = data.getColumnCount();

			while(rs.next()) {
				JSONObject json = new JSONObject();
				for (int i=1; i<=columnNum; i++) {
					String name = data.getColumnName(i);
					Object value = rs.getObject(i);
					json.put(name, value);
				}
				String fId = json.getString("fd_id");
				//群组有效性只由自身决定
				boolean isAvailable = json.getBoolean("fd_is_available");
				OrgGroup group = groupMap.get(fId);
				if (group == null) {
					if (!isAvailable) {
						continue;
					}
					group = new OrgGroup(fId);
					logger.write(" add new group : " + group.getfId());
				} else {
					//logger.write(" update group : " + group.getfName() + ", available : " + isAvailable);
				}
				group.setfName(json.getString("fd_name"));
				group.setfNamePinYin(json.getString("fd_name_pinyin"));
				group.setfOrder(json.getString("fd_order"));
				group.setfCode(json.getString("fd_no"));
				group.setfValid(isAvailable);
				if (json.containsKey("fd_cateid") && json.get("fd_cateid") != null) {
					group.setfGroupCate(this.groupCateMap.get(json.getString("fd_cateid")));
				}

				orgGroupService.save(group);
				count++;

				//事务提交
				if (count >= 200) {
					status.flush();
					count = 0;
				}
			}
		} finally {
			if (Objects.nonNull(rs)) {
				rs.close();
			}
			if (Objects.nonNull(statement)){
				statement.close();
			}
		}
	}

	/**
	 * 同步岗位人员
	 */
	public void syncPostPerson() throws Exception {
		//ekp系统的岗位人员数据
		JSONArray array = select(" select * from sys_org_post_person order by fd_postid ", null);
		//系统现有的岗位人员数据
		Map<String, Map<String, OrgPostMember>> memberMap = new HashMap<String, Map<String, OrgPostMember>>();
		orgPostMemberService.scroll(null,
				(orgPostMember) -> {
					if (memberMap.containsKey(orgPostMember.getfPost().getfId())) {
						Map<String, OrgPostMember> map = memberMap.get(orgPostMember.getfPost().getfId());
						map.put(orgPostMember.getfElement().getfId(), orgPostMember);
						memberMap.replace(orgPostMember.getfPost().getfId(), map);
					} else {
						Map<String, OrgPostMember> map = new HashMap<String, OrgPostMember>();
						map.put(orgPostMember.getfElement().getfId(), orgPostMember);
						memberMap.put(orgPostMember.getfPost().getfId(), map);
					}
				});
		//系统现有的岗位数据
		Map<String, OrgPost> postMap = new HashMap<String, OrgPost>();
		orgPostService.scroll(null,
				(OrgPost) -> {
					postMap.put(OrgPost.getfId(), OrgPost);
				});
		//系统现有的组织架构数据
		Map<String, OrgElement> elementMap = new HashMap<String, OrgElement>();
		orgElementService.scroll(null,
				(OrgElement) -> {
					elementMap.put(OrgElement.getfId(), OrgElement);
				});

		for (int i=0; i<array.size(); i++) {
			JSONObject json = array.getJSONObject(i);
			String postid = json.getString("fd_postid");
			String personid = json.getString("fd_personid");

			//若岗位不存在，或岗位已失效，清除所有已有岗位人员数据
			if (!postMap.containsKey(postid) || !postMap.get(postid).getfValid()) {
				if (memberMap.containsKey(postid)) {
					for (OrgPostMember member : memberMap.get(postid).values()) {
						logger.write("delete member: post :" + member.getfPost().getfName() + "; person:" + member.getfElement().getfName());
						orgPostMemberService.delete(member);
						if (count++ >= 200) {
							status.flush();
							count = 0;
						}
					}
					memberMap.remove(postid);
					continue;
				}
			}

			Map<String, OrgPostMember> map = new HashMap<String, OrgPostMember>();
			// 获取现有的岗位人员记录
			if (memberMap.containsKey(postid)) {
				map = memberMap.get(postid);
			}
			// 若岗位人员数据已存在
			if (map.containsKey(personid)) {
				//且人员有效，将其从map中移除
				if (elementMap.containsKey(personid) && elementMap.get(personid).getfValid()) {
					logger.write("remove person: post_id:" + postid + ", person_id:" + personid);
					map.remove(personid);
				}
				//若人员不存在或无效，则后面将清除该条数据
			} else {
				//若岗位人员记录不存在，且人员有效，则新建岗位人员数据
				if (elementMap.containsKey(personid) && elementMap.get(personid).getfValid()) {
					OrgPostMember member = new OrgPostMember();
					member.setfElement(elementMap.get(personid));
					member.setfPost(postMap.get(postid));
					logger.write("add person: post:" + postMap.get(postid).getfName() + ", person:" + elementMap.get(personid).getfName());
					orgPostMemberService.save(member);

					//事务提交
					count++;
					if (count >= 200) {
						status.flush();
						count = 0;
					}
				}
			}
			memberMap.replace(postid, map);
		}

		//通过上面的遍历之后，此时memberMap里面剩余的记录，是本次同步岗位人员后，不存在与ekp的岗位人员记录，刚剩余记录删除
		for(Map<String, OrgPostMember> map : memberMap.values()) {
			for (OrgPostMember member : map.values()) {
				logger.write("delete member: post :" + member.getfPost().getfName() + "; person:" + member.getfElement().getfName());
				orgPostMemberService.delete(member);
				//事务提交
				count++;
				if (count >= 200) {
					status.flush();
					count = 0;
				}
			}
		}
	}

	/**
	 * 同步群组人员，处理逻辑同岗位人员
	 */
	public void syncGroupPerson() throws Exception {
		JSONArray array = select(" select * from sys_org_group_element order by fd_groupid ", null);
		Map<String, Map<String, OrgGroupMember>> memberMap = new HashMap<String, Map<String, OrgGroupMember>>();
		orgGroupMemberService.scroll(null,
				(orgGroupMember) -> {
					if (memberMap.containsKey(orgGroupMember.getfGroup().getfId())) {
						Map<String, OrgGroupMember> map = memberMap.get(orgGroupMember.getfGroup().getfId());
						map.put(orgGroupMember.getfElement().getfId(), orgGroupMember);
						memberMap.replace(orgGroupMember.getfGroup().getfId(), map);
					} else {
						Map<String, OrgGroupMember> map = new HashMap<String, OrgGroupMember>();
						map.put(orgGroupMember.getfElement().getfId(), orgGroupMember);
						memberMap.put(orgGroupMember.getfGroup().getfId(), map);
					}
				});

		Map<String, OrgGroup> groupMap = new HashMap<String, OrgGroup>();
		orgGroupService.scroll(null,
				(OrgGroup) -> {
					groupMap.put(OrgGroup.getfId(), OrgGroup);
				});
		Map<String, OrgElement> elementMap = new HashMap<String, OrgElement>();
		orgElementService.scroll(null,
				(OrgElement) -> {
					elementMap.put(OrgElement.getfId(), OrgElement);
				});

		for (int i=0; i<array.size(); i++) {
			JSONObject json = array.getJSONObject(i);
			String groupid = json.getString("fd_groupid");
			String personid = json.getString("fd_elementid");

			if (!groupMap.containsKey(groupid) || !groupMap.get(groupid).getfValid()) {
				if (memberMap.containsKey(groupid)) {
					for (OrgGroupMember member : memberMap.get(groupid).values()) {
						orgGroupMemberService.delete(member);
						if (count++ >= 200) {
							status.flush();
							count = 0;
						}
					}
					memberMap.remove(groupid);
					continue;
				}
			}

			Map<String, OrgGroupMember> map = new HashMap<String, OrgGroupMember>();
			if (memberMap.containsKey(groupid)) {
				map = memberMap.get(groupid);
			}
			if (map.containsKey(personid)) {
				if (elementMap.containsKey(personid) && elementMap.get(personid).getfValid()) {
					map.remove(personid);
				}
			} else {
				if (elementMap.containsKey(personid) && elementMap.get(personid).getfValid()) {
					OrgGroupMember member = new OrgGroupMember();
					member.setfElement(elementMap.get(personid));
					member.setfGroup(groupMap.get(groupid));
					orgGroupMemberService.save(member);

					count++;
					if (count >= 200) {
						status.flush();
						count = 0;
					}
				}
			}

			memberMap.replace(groupid, map);
		}

		for(Map<String, OrgGroupMember> map : memberMap.values()) {
			for (OrgGroupMember member : map.values()) {
				orgGroupMemberService.delete(member);
				//事务提交
				count++;
				if (count >= 200) {
					status.flush();
					count = 0;
				}
			}
		}
	}

	/**
	 * 同步群组分类
	 */
	private void syncGroupCate() throws Exception {
		Map<String, JSONObject> syncMap = new HashMap<String, JSONObject>();
		syncMap = scroll(" select * from sys_org_group_cate ", null, "fd_id");
		Map<String, OrgGroupCate> groupCateMap = new HashMap<String, OrgGroupCate>();
		orgGroupCateService.scroll(null,
				(orgGroupCate) -> {
					groupCateMap.put(orgGroupCate.getfId(), orgGroupCate);
				});

		//生成新群组信息
		for (JSONObject json : syncMap.values()) {
			String fId = json.getString("fd_id");
			if (!groupCateMap.containsKey(fId)) {
				OrgGroupCate cate = new OrgGroupCate(fId);
				cate.setfName(json.getString("fd_name"));
				orgGroupCateService.save(cate);
				groupCateMap.put(fId, cate);
				count++;
				logger.write(" add new groupCate : " + cate.getfName());
			}
			if (count > 200) {
				status.flush();
				count = 0;
			}
		}
		status.flush();
		count = 0;

		//更新群组信息，层级信息
		for (OrgGroupCate cate : groupCateMap.values()) {
			String fId = cate.getfId();
			if (syncMap.containsKey(fId)) {
				JSONObject json = syncMap.get(fId);
				cate.setfKeyword(json.getString("fd_keyword"));
				cate.setFdOrder(json.getInteger("fd_order"));
				cate.setfParent(groupCateMap.get(json.getString("fd_parentid")));
				cate.setfName(json.getString("fd_name"));
				orgGroupCateService.save(cate);
				count++;
				//logger.write(" update groupCate : " + cate.getfName());

				if (count > 200) {
					status.flush();
					count = 0;
				}
			}
		}
		status.flush();
		count = 0;
		this.groupCateMap =groupCateMap;
	}

	/**
	 * 查询语句，返回Map，需要key字段名
	 */
	public Map<String, JSONObject> scroll(String sql, String[] inputs, String key) throws Exception {
		Map<String, JSONObject> result = new HashMap<String, JSONObject>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = conn.prepareStatement(sql);
			if (inputs != null) {
				for (int i=0; i<inputs.length; i++) {
					statement.setString(i+1, inputs[i]);
				}
			}
			rs = statement.executeQuery();

			ResultSetMetaData data = rs.getMetaData();
			int columnNum = data.getColumnCount();

			while(rs.next()) {
				JSONObject json = new JSONObject();
				for (int i=1; i<=columnNum; i++) {
					String name = data.getColumnName(i);
					Object value = rs.getObject(i);
					json.put(name, value);
				}
				result.put(json.getString(key), json);
			}
		} finally {
			if (Objects.nonNull(rs)) {
				rs.close();
			}
			if (Objects.nonNull(statement)){
				statement.close();
			}
		}
		return result;
	}


	/**
	 * 查询语句，返回JSONArray
	 */
	public JSONArray select (String sql, String[] inputs) throws Exception {
		JSONArray jsonArray = new JSONArray();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = conn.prepareStatement(sql);
			if (inputs != null) {
				for (int i=0; i<inputs.length; i++) {
					statement.setString(i+1, inputs[i]);
				}
			}
			rs = statement.executeQuery();

			ResultSetMetaData data = rs.getMetaData();
			int columnNum = data.getColumnCount();

			while(rs.next()) {
				JSONObject json = new JSONObject();
				for (int i=1; i<=columnNum; i++) {
					String name = data.getColumnName(i);
					Object value = rs.getObject(i);
					json.put(name, value);
				}
				jsonArray.add(json);
			}
		} finally {
			if (Objects.nonNull(rs)) {
				rs.close();
			}
			if (Objects.nonNull(statement)){
				statement.close();
			}
		}
		return jsonArray;
	}

	/**
	 * 是否有效判断
	 */
	public Boolean isAvailable(JSONObject json, Map<String, JSONObject> syncCompanyMap, Map<String, JSONObject> syncDeptMap) throws Exception {
		//人员数据；无parent，或parent不存在时，则无效
		String fdParentId = json.getString("fd_parentid");
		if (8 == json.getInteger("fd_org_type")) {
			if (!json.containsKey("fd_parentid")) {
				return false;
			} else {
				if (!companyMap.containsKey(fdParentId) && !deptMap.containsKey(fdParentId)) {
					return false;
				}
			}
		}

		//层级数据无效
		boolean isAvailable = true;
		JSONObject parent = json;//从自己开始
		while (parent != null) {
			//中间任一层级无效，则该记录无效
			if (!json.getBoolean("fd_is_available")) {
				isAvailable = false;
				break;
			}

			String nextParentId = parent.getString("fd_parentid");
			//若已无下一层级id，则说明已获取到定，返回结果
			if (StringUtil.isNull(nextParentId)) {
				break;
			}

			if (syncDeptMap.containsKey(nextParentId)) {
				parent = syncDeptMap.get(nextParentId);
			} else if (syncCompanyMap.containsKey(nextParentId)) {
				parent = syncCompanyMap.get(nextParentId);
			} else {
				//如果其父级不存在companyMap和deptMap中，则说明其父级不存在，数据无效
				isAvailable = false;
				parent = null;
				break;
			}
		}
		return isAvailable;
	}
}
