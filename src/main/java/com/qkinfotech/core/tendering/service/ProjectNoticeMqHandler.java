package com.qkinfotech.core.tendering.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.app.config.EkpConfig;
import com.qkinfotech.core.file.FileTransferController;
import com.qkinfotech.core.file.SysFile;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.tendering.interfaceConfig.InterfaceLog;
import com.qkinfotech.core.tendering.model.apps.notice.AppsCompanyBidPackage;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeCompanyBid;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeMain;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticePackage;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import com.qkinfotech.core.tendering.model.attachment.AttachmentPackage;
import com.qkinfotech.util.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 接受MQ 公告数据
 *
 * @param
 */
@Component
public class ProjectNoticeMqHandler {

    @Autowired
    protected SimpleService<AppsNoticeMain> appsNoticeMainService;

    @Autowired
    protected SimpleService<AppsNoticePackage> appsNoticePackageService;

    @Autowired
    protected SimpleService<AppsProjectMain> appsProjectMainService;

    @Autowired
    protected SimpleService<AppsProjectPackage> appsProjectPackageService;

    @Autowired
    protected SimpleService<AppsNoticeCompanyBid> appsNoticeCompanyBidService;

    @Autowired
    protected SimpleService<AppsCompanyBidPackage> appsCompanyBidPackageService;

    @Autowired
    protected SimpleService<OrgPerson> orgPersonService;
    @Autowired
    protected SimpleService<SysFile> sysFileService;
    @Autowired
    protected SimpleService<AttachmentMain> attachmentMainService;
    @Autowired
    protected SimpleService<AttachmentPackage> attachmentPackageService;
    @Autowired
    protected SimpleService<InterfaceLog> interfaceLogService;
    @Autowired
    private EkpConfig ekpConfig;


    /**
     {
     "id": "1909056b992f5fa71ac07364d22945bd",
     "name": "【00000000】金桥地铁上盖J9A-02地块酒店及商业项目设计招标",
     "startTime": "2024-07-10 00:00",
     "endTime": "2024-07-12 00:00",
     "price": 1000,
     "isInvitation": false,
     "openTime": "2024-07-15 00:00",
     "deadlineTime": "2024-07-15 00:00",
     "businessId": "20240101001",
     "hasBalse": true,
     "acceptUnion":true,
     "acceptUpload":true,
     "balse": "耳鸣耳聋综合诊疗设备;全自动真菌/细菌动态检测;射频消融治疗仪;阴道显微镜",
     "balseCode": "01J0A49WTBKVGP2KNPJ2JMDJA7;02J0A49WTBKVGP2KNPJ2JMDJC5;03J0A49WTBKVGP2KNPJ2JMDJC5;04J0A49WTBKVGP2KNPJ2JMDJC5",
     "quoteWay": "0",
     "readerIds": [{
     "id": "1183b0b84ee4f581bba001c47a78b2d9",
     "name": "管理员"
     }],
     "files": [{
     "fileId": "190905752961175548db86c4ad7a081c", //文件id
     "id": "19090575a9fd101134eb3414674ba838",
     //附件id
     "name": "15.LBPM流程引擎宝典.docx",
     //附件名称
     "key": "fdCaiGouAtt",
     //附件标识  标识采购文件
     "size": 5.2622352E7
     //附件大小
     }, {
     "fileId": "178c3ebe82b40c04f7f706d446aa6d10",
     "id": "19090575afc0d1479586bec42f99993e",
     "name": "10.校验框架.doc",
     "key": "fdAttachment",
     //附件标识  标识公告附件
     "size": 1028608
     }, {
     "fileId": "178c3ebce6617981e8b21914b7aa44d4",
     "id": "19090575b3ef84a891e14534907a9b61",
     "name": "LBPM流程引擎宝典.pdf",
     "key": "att",
     //附件标识 公告原稿
     "size": 8006797
     }]
     }
     */


    /**
     * 同步公告
     *
     * @param message
     * @return
     * @throws Exception
     */
    //@JmsListener(destination = "EKP_NEWS_NOTIFY_BID")
    public JSONObject syncNewsNotifyBid(String message) throws Exception {
        JSONObject json = JSONObject.parseObject(message);
        try {
            String id = json.getString("id");//ekp 公告id
            String businessId = json.getString("businessId");//项目编号
            //全量更新，先用id和 businessId 去apps_notice_main 找公告 存在就更新记录，不存在就新增
            JSONObject query = new JSONObject();
            JSONObject queryJson = new JSONObject();
            JSONArray and = new JSONArray();

            JSONObject eq = new JSONObject();
            JSONObject protocolNo = new JSONObject();
            protocolNo.put("fProjectId.fProtocolNo", businessId);
            eq.put("eq", protocolNo);
            and.add(eq);

            JSONObject eq2 = new JSONObject();
            JSONObject ekpId = new JSONObject();
            ekpId.put("fEkpId", id);
            eq2.put("eq", ekpId);
            and.add(eq2);

            queryJson.put("and", and);
            query.put("query", queryJson);
            List<AppsNoticeMain> noticeMains = appsNoticeMainService.findAll(JSONQuerySpecification.getSpecification(query));
            AppsNoticeMain main = new AppsNoticeMain();//默认新增
            if (!noticeMains.isEmpty()) {//有记录就更新
                main = noticeMains.get(0);
            }
            //保存公告
            saveNoticeMain(main, json);
            //保存公告关联附件
            saveNoticePackage(main, json);
            //附件处理
            saveFiles(main,json,"noticeMain",json.getString("balseCode"),"com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeMain",main.getfId());
            //日志留存
            InterfaceLog log= new InterfaceLog();
            log.setfCreateTime(new Date());
            log.setfInterfaceUrl("EKP_NEWS_NOTIFY_BID");
            log.setfInterfaceName("mq接收公告数据");
            log.setfProtocolNo(businessId);
            log.setfInterfaceStatus("1");
            log.setfInterfaceInfo(message);
            interfaceLogService.save(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 同步标书单位
     */
    //@JmsListener(destination = "EKP_SIGN_NOTIFY_BID")
    public JSONObject syncSignNotifyBid(String message) {
        JSONObject json = JSONObject.parseObject(message);
        try {
            //全量更新 根据 newsid applyid applyname 找到对应记录
            JSONObject query = new JSONObject();
            JSONObject queryJson = new JSONObject();
            JSONArray and = new JSONArray();
            JSONObject eq = new JSONObject();
            JSONObject protocolNo = new JSONObject();
            protocolNo.put("fApplyId", json.getString("applyId"));
            eq.put("eq", protocolNo);
            and.add(eq);

            JSONObject eq1 = new JSONObject();
            JSONObject ekpId = new JSONObject();
            ekpId.put("fApplyName", json.getString("applyName"));
            eq1.put("eq", ekpId);
            and.add(eq1);

            JSONObject eq2 = new JSONObject();
            JSONObject newsId = new JSONObject();
            newsId.put("fMain.fEkpId", json.getString("newsId"));
            eq2.put("eq", newsId);
            and.add(eq2);

            queryJson.put("and", and);
            query.put("query", queryJson);
            List<AppsNoticeCompanyBid> companyBids = appsNoticeCompanyBidService.findAll(JSONQuerySpecification.getSpecification(query));
            AppsNoticeCompanyBid companyBid = new AppsNoticeCompanyBid();//默认新增
            if (!companyBids.isEmpty()) {//有记录就更新
                companyBid = companyBids.get(0);
            }
            //找到对应公告记录
            AppsNoticeMain noticeMain = findNoticeMain(json);
            //报名单位信息
            saveNoticeCompanyBid(companyBid, noticeMain, json);
            //公司所购买包件信息
            saveCompanyPackage(noticeMain, json);
            // 附件处理
            saveFiles(noticeMain,json,"companyFile",json.getString("packageCode"),"com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeCompanyBid",companyBid.getfId());
            //日志留存
            InterfaceLog log= new InterfaceLog();
            log.setfCreateTime(new Date());
            log.setfInterfaceUrl("EKP_SIGN_NOTIFY_BID");
            log.setfInterfaceName("mq接收标书单位数据");
            log.setfInterfaceStatus("1");
            log.setfInterfaceInfo(message);
            interfaceLogService.save(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 保存 公告记录
     *
     * @param main 公告main
     * @param json mq请求参数
     * @throws ParseException
     */
    private void saveNoticeMain(AppsNoticeMain main, JSONObject json) throws ParseException {
        // 定义日期格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        main.setfAnnouncementTitle(json.getString("name"));
        main.setfTenderAcquisitionStartDate(dateFormat.parse(json.getString("startTime")));
        main.setfTenderAcquisitionEndDate(dateFormat.parse(json.getString("endTime")));
        main.setfPrice(json.getString("price"));
        main.setfIsInviteShortlists(json.getBoolean("isInvitation").toString());//是否定向邀请
        main.setfOpenTime(dateFormat.parse(json.getString("openTime")));
        main.setfApplicationDocumentDeadline(dateFormat.parse(json.getString("deadlineTime")));
        main.setfCreateTime(new Date());//创建时间
        main.setfAnnouncementPublishedDate(new Date());//公告发布日期

        main.setfAnnouncementUrl(ekpConfig.getEkpUrl()+"/sys/news/sys_news_main/sysNewsMain.do?method=view&fdId=" + json.getString("id"));//公告链接
        //项目编号 查询对应项目 关联起来
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fProtocolNo", json.getString("businessId"));
        queryJson.put("eq", equal);
        query.put("query", queryJson);
        List<AppsProjectMain> projectMains = appsProjectMainService.findAll(JSONQuerySpecification.getSpecification(query));
        if (!projectMains.isEmpty()) {
            main.setfProjectId(projectMains.get(0));
        }
        main.setfIsAccurateMatching(json.getBoolean("hasBalse").toString());//是否模糊匹配
        main.setfQuoteWay(json.getString("quoteWay"));//售卖方式
        main.setfIsUnion(json.getBoolean("acceptUnion").toString());//是否联合体
        main.setfIsPrequalification(json.getBoolean("acceptUpload").toString());//是否资格预审
        main.setfEkpId(json.getString("id"));
        appsNoticeMainService.save(main);
        //
        JSONArray array = json.getJSONArray("readerIds");//邀请列表，需要去除admin管理员
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = (JSONObject) array.get(i);
            // 查询sys_person fid 和 type是3-供应商 的记录 用于存储展示 作为已经邀请的记录
            if (isSupplier(object.getString("id"))) {
                AppsNoticeCompanyBid companyBid = new AppsNoticeCompanyBid();
                companyBid.setfMain(main);
                companyBid.setfApplyName(object.getString("name"));
                companyBid.setfApplyId(object.getString("id"));
                companyBid.setfBusinessStatus("00"); //00-代表已经邀请了供应商
                appsNoticeCompanyBidService.save(companyBid);
            }
        }


    }

    /**
     * 保存 公告关联包件关系
     *
     * @param main 公告main
     * @param json mq请求参数
     */
    private void saveNoticePackage(AppsNoticeMain main, JSONObject json) {
        String packages = json.getString("balseCode");
        //;分割 包件list
        String[] codes = packages.split(";");

        //查询该公告是否已经存在包件关系
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq = new JSONObject();
        JSONObject fmain = new JSONObject();
        fmain.put("fMain.fId", main.getfId());
        eq.put("eq", fmain);
        and.add(eq);

        queryJson.put("and", and);
        query.put("query", queryJson);
        List<AppsNoticePackage> noticePackages = appsNoticePackageService.findAll(JSONQuerySpecification.getSpecification(query));
        if (!noticePackages.isEmpty()) {
            return;
        }
        //找到每个id 的包件对象 直接存储
        for (int i = 0; i < codes.length; i++) {
            String id = codes[i];
            AppsProjectPackage projectPackage = appsProjectPackageService.getById(id);//包件对象
            AppsNoticePackage noticePackage = new AppsNoticePackage();
            noticePackage.setfPackage(projectPackage);
            noticePackage.setfMain(main);
            appsNoticePackageService.save(noticePackage);
        }
    }

    /**
     * 判断邀请列表中是否 是供应商
     */
    private boolean isSupplier(String id) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();
        JSONObject eq = new JSONObject();

        JSONObject protocolNo = new JSONObject();
        protocolNo.put("fEkpUserType", "3");
        eq.put("eq", protocolNo);
        and.add(eq);

        JSONObject eq1 = new JSONObject();
        JSONObject ekpId = new JSONObject();
        ekpId.put("fId", id);
        eq1.put("eq", ekpId);
        and.add(eq1);

        queryJson.put("and", and);
        query.put("query", queryJson);
        List<OrgPerson> personList = orgPersonService.findAll(JSONQuerySpecification.getSpecification(query));
        return !personList.isEmpty();
    }


    /**
     * 保存 报名单位信息
     */
    private void saveNoticeCompanyBid(AppsNoticeCompanyBid companyBid, AppsNoticeMain noticeMain, JSONObject json) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        companyBid.setfName(json.getString("contactName"));
        companyBid.setfEkpid(json.getString("id"));
        companyBid.setfApplyId(json.getString("applyId"));
        companyBid.setfApplyName(json.getString("applyName"));
        companyBid.setfCreateTime(dateFormat.parse(json.getString("createTime")));
        companyBid.setfStatus(json.getString("status"));
        companyBid.setfBusinessStatus(json.getString("businessStatus"));
        companyBid.setfContacts(json.getString("contactName"));
        companyBid.setfPhone(json.getString("phone"));
        companyBid.setfEmail(json.getString("email"));
        companyBid.setfUnitName(json.getString("unitName"));
        companyBid.setfAddress(json.getString("address"));
        companyBid.setfDetailedAddress(json.getString("detailedAddress"));
        companyBid.setfInvoiceTitle(json.getString("invoiceTitle"));
        companyBid.setfInvoiceType(json.getString("invoiceType"));
        companyBid.setfInvoiceCode(json.getString("invoiceCode"));
        companyBid.setfPackageName(json.getString("packageName"));
        companyBid.setfPackageCode(json.getString("packageCode"));
        companyBid.setfMain(noticeMain);

        appsNoticeCompanyBidService.save(companyBid);

    }


    /**
     * 保存 报名单位所购买的包件
     *
     * @param noticeMain
     * @param json
     */
    private void saveCompanyPackage(AppsNoticeMain noticeMain, JSONObject json) {
        String packages[] = json.getString("packageCode").split(";");//购买包件数组
        OrgPerson person = orgPersonService.getById(json.getString("applyId"));//供应商对象
        for (int i = 0; i < packages.length; i++) {
            AppsProjectPackage projectPackage = appsProjectPackageService.getById(packages[i]);
            //查询是否存在记录
            JSONObject query = new JSONObject();
            JSONObject queryJson = new JSONObject();
            JSONArray and = new JSONArray();

            JSONObject eq = new JSONObject();
            JSONObject fPackage = new JSONObject();
            fPackage.put("fPackage.fId", projectPackage.getfId());
            eq.put("eq", fPackage);
            and.add(eq);

            JSONObject eq1 = new JSONObject();
            JSONObject fNoticeMain = new JSONObject();
            fNoticeMain.put("fNoticeMain.fId", noticeMain.getfId());
            eq1.put("eq", fNoticeMain);
            and.add(eq1);

            JSONObject eq2 = new JSONObject();
            JSONObject fSupplier = new JSONObject();
            fSupplier.put("fSupplier.fId", json.getString("applyId"));
            eq2.put("eq", fSupplier);
            and.add(eq2);

            JSONObject eq3 = new JSONObject();
            JSONObject fProjectMain = new JSONObject();
            fProjectMain.put("fProjectMain.fId", noticeMain.getfProjectId().getfId());
            eq3.put("eq", fProjectMain);
            and.add(eq3);

            queryJson.put("and", and);
            query.put("query", queryJson);
            List<AppsCompanyBidPackage> companyBidPackages = appsCompanyBidPackageService.findAll(JSONQuerySpecification.getSpecification(query));
            //
            if (companyBidPackages.isEmpty()) {
                AppsCompanyBidPackage bidPackage = new AppsCompanyBidPackage();
                bidPackage.setfPackage(projectPackage);
                bidPackage.setfNoticeMain(noticeMain);
                bidPackage.setfSupplier(person);
                bidPackage.setfProjectMain(noticeMain.getfProjectId());
                appsCompanyBidPackageService.save(bidPackage);
            }
        }
    }

    /**
     * 报名信息 通过newsid 查询公告记录
     *
     * @param json
     * @return
     */
    private AppsNoticeMain findNoticeMain(JSONObject json) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq = new JSONObject();
        JSONObject newsId = new JSONObject();
        newsId.put("fEkpId",  json.getString("newsId"));
        eq.put("eq", newsId);
        and.add(eq);

        queryJson.put("and", and);
        query.put("query", queryJson);

        List<AppsNoticeMain> noticeMains = appsNoticeMainService.findAll(JSONQuerySpecification.getSpecification(query));
        if (!noticeMains.isEmpty()) {
            return noticeMains.get(0);
        }
        return null;
    }


    /**
     * 附件保存
     */
    private void saveFiles(AppsNoticeMain noticeMain, JSONObject json, String key,String packages,String modelName,String modelId) {
        JSONArray files = json.getJSONArray("files");
        for (int i = 0; i < files.size(); i++) {
            JSONObject obj = files.getJSONObject(i);
            String name = obj.getString("name");
            String id = obj.getString("id");
//            String fileURL = "http://ekpdev1.sitcbeta.loc/ekp/resource/attachment/sys_att_main/sysAttMain.do?method=download&fdId="+id;
            String fileURL = "https://www.shabidding.com/ekp/resource/attachment/sys_att_main/sysAttMain.do?method=download&fdId="+id;
            try {
                URL url = new URL(fileURL);
                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                int responseCode = httpConn.getResponseCode();
                // 检查是否连接成功
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 获取输入流
                    InputStream inputStream = httpConn.getInputStream();
                    //数据流转MultipartFile文件
                    MultipartFile targetMultipartFile = new MockMultipartFile(name, inputStream);
                    JSONObject object = SpringUtil.getContext().getBean(FileTransferController.class).uploadFile(targetMultipartFile, name);
                    String fileId = object.getString("fId");//附件id
                    //用fileid 和 attachment_main 创建关系
                    AttachmentMain attMain = new AttachmentMain();
                    SysFile sysFile = sysFileService.getById(fileId);
                    attMain.setfFile(sysFile);
                    //区分公告原稿
                    if("att".equals(obj.getString("key"))){
                        attMain.setfModelName("com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain");
                        attMain.setfModelId(noticeMain.getfProjectId().getfId());
                        attMain.setfKey("originalNotice");
                    }else{
                        attMain.setfModelName(modelName);
                        attMain.setfModelId(modelId);
                        attMain.setfKey(key);
                    }
                    attMain.setfFileName(name);
                    attMain.setfFileSize(String.valueOf(sysFile.getfSize()));
                    attMain.setfFileLink(fileURL);
                    attachmentMainService.save(attMain);
                    //然后再创建关联包件记录
//                    String packages = json.getString("balseCode");
                    String[] codes = packages.split(";");
                    for (int l = 0; l < codes.length; l++) {
                        AppsProjectPackage appsProjectPackage = appsProjectPackageService.getById(codes[l]);
                        AttachmentPackage prjPackage = new AttachmentPackage();
                        prjPackage.setfPackageId(appsProjectPackage);
                        prjPackage.setfAttachmentId(attMain);
                        attachmentPackageService.save(prjPackage);
                    }
                }
                httpConn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
