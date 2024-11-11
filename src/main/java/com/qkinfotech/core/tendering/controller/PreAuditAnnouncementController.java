package com.qkinfotech.core.tendering.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.file.FileTransferController;
import com.qkinfotech.core.file.SysFile;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.tendering.interfaceConfig.InterfaceLog;
import com.qkinfotech.core.tendering.model.apps.finalization.AppsFinalizationResultPackage;
import com.qkinfotech.core.tendering.model.apps.finalization.AppsFinalizationResults;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeMain;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticePackage;
import com.qkinfotech.core.tendering.model.apps.project.*;
import com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import com.qkinfotech.core.tendering.model.attachment.AttachmentPackage;
import com.qkinfotech.util.SpringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.hc.core5.http.ContentType;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/pre/audit/announcement")
@Slf4j
public class PreAuditAnnouncementController<T extends BaseEntity> {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected SimpleResult result;

    @Autowired
    protected SimpleService<AppsProjectMain> appsProjectMainService;

    @Autowired
    protected SimpleService<AppsNoticeMain> appsNoticeMainService;

    @Autowired
    protected SimpleService<AppsNoticePackage> appsNoticePackageService;

    @Autowired
    protected SimpleService<AttachmentPackage> attachmentPackageService;

    @Autowired
    protected SimpleService<AppsFinalizationResultPackage> appsFinalizationResultPackageService;

    @Autowired
    protected SimpleService<AppsFinalizationResults> appsFinalizationResultsService;

    @Autowired
    protected SimpleService<AppsProjectPackage> appsProjectPackageService;

    @Autowired
    protected SimpleService<InterfaceLog> interfaceLogService;

    @Autowired
    protected SimpleService<AppsSupplierMain> appsSupplierMainService;

    /**
     * 资格预审公告保存
     *
     * @throws Exception
     */
    @RequestMapping("/save")
    @ResponseBody

    public void save() throws Exception {
        JSONObject body = getPostData();
        AppsNoticeMain main = new AppsNoticeMain();
        //
        AppsProjectMain projectMain = body.getObject("fProjectMain", AppsProjectMain.class);
        main.setfProjectId(projectMain);
        main.setfIsUnion(body.getString("fIsUnion"));
        main.setfIsPrequalification(body.getString("fIsPrequalification"));
        main.setfIsInviteShortlists(body.getString("fIsInviteShortlists"));
        main.setfIsAccurateMatching(body.getString("fIsAccurateMatching"));
        appsNoticeMainService.save(main);
        //包件信息
        JSONArray array = body.getJSONArray("packageIds");
        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            AppsProjectPackage projectPackage = jsonObject.to(AppsProjectPackage.class);
            //
            AppsNoticePackage noticePackage = new AppsNoticePackage();
            noticePackage.setfPackage(projectPackage);
            noticePackage.setfMain(main);
            appsNoticePackageService.save(noticePackage);
        }
        JSONObject obj = new JSONObject();
        obj.put("fId", main.getfId());
        result.from(obj);
        result.getResponse().setCharacterEncoding("UTF-8");
    }


    /**
     * 资格预审公告接口获取参数
     *
     * @throws Exception
     */
    @RequestMapping("/getNotice")
    @ResponseBody
    public void getNotice() throws Exception {
        String businessId = request.getParameter("businessId");//项目编号
//        String ekpId = request.getParameter("ekpId");//公告id
        String pmId = "";//项目经理id

        logger.debug(">>>>资格预审公告接口获取参数<<<<" + businessId);
        JSONObject obj = new JSONObject();
        obj.put("f_protocol_no", businessId);
        //包件数据
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject equal = new JSONObject();
        JSONObject protocolNo = new JSONObject();
        protocolNo.put("fMainId.fProtocolNo", businessId);
        equal.put("eq", protocolNo);
        and.add(equal);

        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsProjectPackage> packages = appsProjectPackageService.findAll(JSONQuerySpecification.getSpecification(query));
        JSONArray arr = new JSONArray();
        for (AppsProjectPackage projectPackage : packages) {
            JSONObject object = new JSONObject();
            object.put("fId", projectPackage.getfId());
            object.put("fName", projectPackage.getfName());
            //  查询包件是否已经通过iso审批
            int flag = getPackageFlag(projectPackage);
            object.put("flag", flag);
            obj.put("f_project_name", projectPackage.getfMainId().getfName());
            arr.add(object);
            pmId = projectPackage.getfMainId().getfDeptManager().getfId();
        }
        obj.put("f_pm_id", pmId);
        obj.put("f_package", arr);
        /*
            接口地址 https://10.8.6.145/gzt/pm/pre/audit/announcement/getNotice?businessId=1403006018
            接口返回
            {
		"f_protocol_no": "1403006018",
		"f_project_name": "最终项目0001",
		"f_pm_id": "170ae6ff8f98cb59d44c24f4272bb649",
		"f_package": [
			{
				"fId": "01J8YPMCFX1GMF0HT9471YMBJD",
				"fName": "报价A",
				"flag": 1
			},
			{
				"fId": "01J8YPMCJDTNS2518M8B268J5A",
				"fName": "包件B",
				"flag": 1
			}
		]
	}
         */

        //日志留存
        InterfaceLog log = new InterfaceLog();
        log.setfCreateTime(new Date());
        log.setfInterfaceUrl("/pm/pre/audit/announcement/getNotice");
        log.setfInterfaceName("ekp公告获取包件信息");
        log.setfProtocolNo(businessId);
        log.setfInterfaceStatus("1");
        log.setfInterfaceInfo(obj.toString());
        interfaceLogService.save(log);

        result.from(obj);
        result.getResponse().setCharacterEncoding("UTF-8");
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

    /**
     * 查询包件是否已经通过iso审批 并返回状态
     *
     * @param projectPackage
     * @return
     */
    private int getPackageFlag(AppsProjectPackage projectPackage) {

        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject equal = new JSONObject();
        JSONObject fPackageId = new JSONObject();
        fPackageId.put("fPackageId.fId", projectPackage.getfId());
        equal.put("eq", fPackageId);
        and.add(equal);

        JSONObject equal1 = new JSONObject();
        JSONObject isoFlag = new JSONObject();
        isoFlag.put("fIsoFlag", 1);
        equal1.put("eq", isoFlag);
        and.add(equal1);

        queryJson.put("and", and);
        query.put("query", queryJson);
        System.out.println("包件是否iso审批:" + projectPackage.getfId());
        List<AttachmentPackage> attachmentPackages = attachmentPackageService.findAll(JSONQuerySpecification.getSpecification(query));
        if (!attachmentPackages.isEmpty()) {//查询包件 是否已经审批过
            return 1; // 已审批
        }
        return 0; //未审批
    }


    /**
     * 结果公告接口获取参数
     *
     * @throws Exception
     */
    @RequestMapping("/getResultNotice")
    @ResponseBody
    public void getResultNotice() throws Exception {
        String businessId = request.getParameter("businessId");//项目编号
//        String packages = request.getParameter("packages");//所选包件
        logger.debug(">>>>结果公告接口获取参数<<<<" + businessId);
        JSONObject obj = new JSONObject();
        obj.put("f_protocol_no", businessId);
        //包件数据
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject equal = new JSONObject();
        JSONObject protocolNo = new JSONObject();
        protocolNo.put("fMainId.fProtocolNo", businessId);
        equal.put("eq", protocolNo);
        and.add(equal);

        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsProjectPackage> packages = appsProjectPackageService.findAll(JSONQuerySpecification.getSpecification(query));
        JSONArray arr = new JSONArray();
        //返回该项目的全部包件
        for (AppsProjectPackage appsProjectPackage : packages) {
            JSONObject object = new JSONObject();
            object.put("fId", appsProjectPackage.getfId());
            object.put("fName", appsProjectPackage.getfName());
            arr.add(object);
        }
        obj.put("f_packages", arr);

        /*
            接口地址 https://10.8.6.145/shgjzb/pm/pre/audit/announcement/getResultNotice?businessId=20240101001
            接口返回
            {
                "f_protocol_no": "20240101001", //项目编号
                "f_packages": [{
                        "fPackageId": "01J0A49WTBKVGP2KNPJ2JMDJA7",
                        "fName": "金桥副中心中央公园综合体国际方案征集（景观组）",
                    },
                    {
                        "fPackageId": "01J0A49WTBKVGP2KNPJ2JMDJA7",
                        "fName": "金桥副中心中央公园综合体国际方案征集（景观组）",
                    }
                ]
            }
         */

        //日志留存
        InterfaceLog log = new InterfaceLog();
        log.setfCreateTime(new Date());
        log.setfInterfaceUrl("/pm/pre/audit/announcement/getResultNotice");
        log.setfInterfaceName("ekp结果公告获取包件信息");
        log.setfProtocolNo(businessId);
        log.setfInterfaceStatus("1");
        log.setfInterfaceInfo(obj.toString());
        interfaceLogService.save(log);

        result.from(obj);
        result.getResponse().setCharacterEncoding("UTF-8");
    }

    /**
     * 结果公告 链接/标题 保存
     *
     * @throws Exception
     */
    @RequestMapping("/saveResultNotice")
    @ResponseBody
    public void saveResultNotice() throws Exception {
        String businessId = request.getParameter("businessId");//项目编号
        String title = request.getParameter("title");//所选包件
        String url = request.getParameter("url");//所选包件
        logger.debug(">>>>结果公告 链接/标题 保存<<<<" + businessId);
        try {
            //根据项目编号 查询项目
            JSONObject query = new JSONObject();
            JSONObject queryJson = new JSONObject();
            JSONObject equal = new JSONObject();
            equal.put("fProtocolNo", businessId);
            queryJson.put("eq", equal);
            query.put("query", queryJson);
            List<AppsProjectMain> projectMains = appsProjectMainService.findAll(JSONQuerySpecification.getSpecification(query));

            AppsFinalizationResults results = new AppsFinalizationResults();
            results.setfCreateTime(new Date());
            results.setfFinalistAnnouncementUrl(url);
            results.setfFinalistAnnouncementName(title);
            if (!projectMains.isEmpty()) {
                results.setfProject(projectMains.get(0));
            } else {
                throw new Exception("无法通过项目编号查询到记录！请检查编号是否正确！"); // 手动抛出一个异常
            }
            appsFinalizationResultsService.save(results);

            JSONObject obj = new JSONObject();
            obj.put("msg", "成功");
            obj.put("status", "S");
            result.from(obj);
            result.getResponse().setCharacterEncoding("UTF-8");
        } catch (Exception e) {
            JSONObject obj = new JSONObject();
            obj.put("msg", "失败" + e.getMessage());
            obj.put("status", "E");
            result.from(obj);
            result.getResponse().setCharacterEncoding("UTF-8");
        }

        /*
            接口地址 https://10.8.6.145/shgjzb/pm/pre/audit/announcement/saveResultNotice?businessId=20240101001&title=xxxx&noticeUrl=xxxx
            接口返回
            {"msg":"成功","status":"S"}
         */

    }

}
