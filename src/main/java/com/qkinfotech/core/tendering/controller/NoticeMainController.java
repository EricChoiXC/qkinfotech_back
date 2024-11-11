package com.qkinfotech.core.tendering.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.app.config.EkpConfig;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeCompanyBid;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeMain;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticePackage;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierInviteCompany;
import com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain;
import com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierPackage;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import com.qkinfotech.core.tendering.service.FileMainService;
import com.qkinfotech.core.tendering.service.SupplierService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资格预审会议
 */

@RestController
@RequestMapping("/notice/main")
public class NoticeMainController {

    @Autowired
    protected HttpServletRequest request;
    @Autowired
    protected SimpleResult result;
    @Autowired
    protected SimpleService<AppsProjectPackage> appsProjectPackageService;
    @Autowired
    protected SimpleService<AppsNoticeMain> appsNoticeMainService;
    @Autowired
    protected SimpleService<AppsNoticePackage> appsNoticePackageService;
    @Autowired
    protected SimpleService<AppsNoticeCompanyBid> appsNoticeCompanyBidService;
    @Autowired
    protected SupplierService supplierService;
    @Autowired
    protected FileMainService fileMainService;
    @Autowired
    private EkpConfig ekpConfig;

    @RequestMapping("/noticeList")
    @ResponseBody
    public void noticeList() throws Exception {
        JSONObject body = getPostData();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); //使用了默认的格式创建了一个日期格式化对象。
        //通过项目id 找到 资格预审公告记录
        String fId = body.getString("fId");
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fProjectId.fId", fId);
        queryJson.put("eq", equal);
        query.put("query", queryJson);
        List<AppsNoticeMain> noticeMainList = appsNoticeMainService.findAll(JSONQuerySpecification.getSpecification(query));
        //资格预审公告
        JSONArray array = new JSONArray();
        for (AppsNoticeMain main : noticeMainList) {
            JSONObject obj = new JSONObject();
            obj.put("title", main.getfAnnouncementTitle());
            obj.put("packageNames", getNoticePackageName(main.getfId()));
            obj.put("noticeId", main.getfId());
            obj.put("noticeName", main.getfAnnouncementTitle());
//            obj.put("noticeurl", "http://ekpdev1.sitcbeta.loc/ekp/sys/news/sys_news_main/sysNewsMain.do?method=view&fdId=" + main.getfEkpId());
            obj.put("noticeurl", ekpConfig.getEkpUrl() + "/sys/news/sys_news_main/sysNewsMain.do?method=view&fdId=" + main.getfEkpId());
            obj.put("noticeReleaseDate", dateFormat.format(main.getfAnnouncementPublishedDate()));
            obj.put("bidTerm", dateFormat.format(main.getfTenderAcquisitionStartDate()) + " ~ " + dateFormat.format(main.getfTenderAcquisitionEndDate()));
            obj.put("ekpId", main.getfEkpId());
//            obj.put("bidCompany", "公司1,公司2,公司3...");
//            obj.put("appInfo", "包件1和包件2的投标文件名");
            obj.put("appDocDeadline", dateFormat.format(main.getfApplicationDocumentDeadline()));
            array.add(obj);
        }

        result.from(array);
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


    private String getNoticePackageName(String fId) {
        String packageName = "";
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fMain.fId", fId);
        queryJson.put("eq", equal);
        query.put("query", queryJson);
        List<AppsNoticePackage> noticePackageList = appsNoticePackageService.findAll(JSONQuerySpecification.getSpecification(query));
        for (AppsNoticePackage noticePackage : noticePackageList) {
            AppsProjectPackage pack = noticePackage.getfPackage();
            packageName += "包件" + pack.getfIndex() + ":" + pack.getfName() + ";";
        }
        return packageName;
    }


    //获取标书单位 所创建的 申请文件
    @RequestMapping("/getApplyFiles")
    @ResponseBody
    public void getApplyFiles() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //使用了默认的格式创建了一个日期格式化对象。

        JSONObject body = getPostData();
        JSONArray array = new JSONArray();
        //通过公告id 找到 资格预审公告记录
        String fId = body.getString("fId");
        AppsNoticeMain noticeMain = appsNoticeMainService.getById(fId);
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONObject equal = new JSONObject();
        equal.put("fMain.fId", fId);
        queryJson.put("eq", equal);
        query.put("query", queryJson);
        List<AppsNoticeCompanyBid> bidLists = appsNoticeCompanyBidService.findAll(JSONQuerySpecification.getSpecification(query));
        //该公告下的标书单位
        for (AppsNoticeCompanyBid bid : bidLists) {
            if ("true".equals(noticeMain.getfIsAccurateMatching())) {
                //以包件维度 搜索记录 精确匹配
                String[] split = bid.getfPackageCode().split(";");
                for (String s : split) {
                    JSONObject object = new JSONObject();
                    object.put("companyName", bid.getfApplyName());//单位名称
                    object.put("name", bid.getfName());//联系人
                    object.put("phone", bid.getfPhone());//手机
                    object.put("email", bid.getfEmail());//邮箱
                    object.put("fCreateTime", dateFormat.format(bid.getfCreateTime()));//领购时间
                    //查询公司账号 下每个包件id 的 已递交 记录
                    AppsSupplierPackage supplierPackage = supplierService.getSupplierMain(noticeMain.getfId(), bid.getfApplyId(), 2, s);
                    object.put("packages", appsProjectPackageService.getById(s).getfName());//包件
                    object.put("packageCodes", s);
                    if (supplierPackage == null) {
                        object.put("ip", "");
                        object.put("subTime", "");
                        object.put("mainCompany", "");
                        object.put("jointCompany", "");
                        object.put("fileSize", "");
                        object.put("fileId", "");
                    } else {
                        if (supplierPackage.getfSupplier().getfIp() != null) {
                            object.put("ip", supplierPackage.getfSupplier().getfIp());
                        } else {
                            object.put("ip", "");
                        }
                        //递交时间
                        if (supplierPackage.getfSupplier().getfSubTime() != null) {
                            object.put("subTime", dateFormat.format(supplierPackage.getfSupplier().getfSubTime()));
                        } else {
                            object.put("subTime", "");
                        }
                        //判断公告是否联合体  /联合体需要获取主体公司
                        if ("true".equals(noticeMain.getfIsUnion())) {
                            List<AppsSupplierInviteCompany> inviteCompanyList = supplierService.getMainCompany(supplierPackage.getfSupplier());
                            String mainCompanyName = inviteCompanyList.stream().filter(item -> "true".equals(item.getfIsMain())).map(AppsSupplierInviteCompany::getfCompanyName).findFirst().orElse(null);
                            object.put("mainCompany", mainCompanyName);
                            object.put("jointCompany", inviteCompanyList.stream().filter(item -> !"true".equals(item.getfIsMain())).map(AppsSupplierInviteCompany::getfCompanyName).collect(Collectors.joining(";")));
                        }
                        //获奖申请文件 sysfile
                        List<AttachmentMain> attachmentMains = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain", supplierPackage.getfSupplier().getfId(), "supplierMain");
                        if (!attachmentMains.isEmpty()) {
                            BigDecimal sum = BigDecimal.ZERO;
                            for (AttachmentMain att : attachmentMains) {
                                sum = sum.add(BigDecimal.valueOf(att.getfFile().getfSize()));
                            }
                            //文件大小
                            BigDecimal fileSizeInMb = sum.divide(BigDecimal.valueOf(1024).pow(2), 2, RoundingMode.HALF_UP);
                            object.put("fileSize", fileSizeInMb + "MB");
                            //文件id
//                            object.put("fileId", attachmentMains.get(0).getfFile().getfId());
                        }
                    }
                    array.add(object);
                }
            } else {
                JSONObject object = new JSONObject();
                object.put("companyName", bid.getfApplyName());//单位名称
                object.put("name", bid.getfName());//联系人
                object.put("phone", bid.getfPhone());//手机
                object.put("email", bid.getfEmail());//邮箱
                object.put("fCreateTime", dateFormat.format(bid.getfCreateTime()));//领购时间
                object.put("packages", bid.getfPackageName());//包件
                object.put("packageCodes", bid.getfPackageCode());
                //查询该供应商创建的 申请文件 公告/状态2/供应商id
                AppsSupplierPackage supplierPackage = supplierService.getSupplierMain(fId, bid.getfApplyId(), 2, null);
                if (supplierPackage != null) {
                    //提交IP
                    if (supplierPackage.getfSupplier().getfIp() != null) {
                        object.put("ip", supplierPackage.getfSupplier().getfIp());
                    } else {
                        object.put("ip", "");
                    }
                    //递交时间
                    if (supplierPackage.getfSupplier().getfSubTime() != null) {
                        object.put("subTime", dateFormat.format(supplierPackage.getfSupplier().getfSubTime()));
                    } else {
                        object.put("subTime", "");
                    }
                    //判断公告是否联合体  /联合体需要获取主体公司
                    if ("true".equals(noticeMain.getfIsUnion())) {
                        List<AppsSupplierInviteCompany> inviteCompanyList = supplierService.getMainCompany(supplierPackage.getfSupplier());
                        String mainCompanyName = inviteCompanyList.stream().filter(item -> "true".equals(item.getfIsMain())).map(AppsSupplierInviteCompany::getfCompanyName).findFirst().orElse(null);
                        object.put("mainCompany", mainCompanyName);
                        object.put("jointCompany", inviteCompanyList.stream().filter(item -> !"true".equals(item.getfIsMain())).map(AppsSupplierInviteCompany::getfCompanyName).collect(Collectors.joining("；")));
                    }
                    //获奖申请文件 sysfile
                    List<AttachmentMain> attachmentMains = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain", supplierPackage.getfId(), "supplierMain");
                    if (!attachmentMains.isEmpty()) {
                        BigDecimal sum = BigDecimal.ZERO;
                        for (AttachmentMain att : attachmentMains) {
                            sum = sum.add(BigDecimal.valueOf(att.getfFile().getfSize()));
                        }
                        //文件大小
                        BigDecimal fileSizeInMb = sum.divide(BigDecimal.valueOf(1024).pow(2), 2, RoundingMode.HALF_UP);
                        object.put("fileSize", fileSizeInMb + "MB");
                        //文件id
//                            object.put("fileId", attachmentMains.get(0).getfFile().getfId());
                    }
                } else {
                    object.put("ip", "");
                    object.put("subTime", "");
                    object.put("mainCompany", "");
                    object.put("jointCompany", "");
                    object.put("fileSize", "");
                    object.put("fileId", "");
                }
                array.add(object);
            }
        }
        result.from(array);
        result.getResponse().setCharacterEncoding("UTF-8");
    }


}