package com.qkinfotech.core.tendering.apps.notice.controller;

import com.qkinfotech.core.file.FileManager;
import com.qkinfotech.core.file.SysFile;
import com.qkinfotech.core.file.SysFileInputStream;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeCompanyBid;
import com.qkinfotech.core.tendering.model.apps.notice.AppsNoticeMain;
import com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import com.qkinfotech.util.StringUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/companyBid")
@Slf4j
public class CompanyBidController {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected Json2Bean json2bean;

    @Autowired
    protected Bean2Json bean2json;

    @Autowired
    protected SimpleService<AppsNoticeCompanyBid> appsNoticeCompanyBidService;

    @Autowired
    protected SimpleService<AttachmentMain> attachmentMainService;

    @Autowired
    protected SimpleService<SysFile> sysFileService;

    @Autowired
    protected FileManager manager;

    @Autowired
    protected SimpleService<AppsSupplierMain> appsSupplierMainService;

    /**
     * 传递公告id，下载本公告所有领购单位领购附件
     *
     * @throws Exception
     */
    @GetMapping("/doDownloadPackage")
    @ResponseBody
    public void doDownloadPackage() throws Exception {
        String fId = request.getParameter("noticeId");
        String bidIds = request.getParameter("bidIds");
        List<File> tempZipList = new ArrayList<>();
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("包件购买信息.zip", "UTF-8"));

        Specification<AppsNoticeCompanyBid> specification = new Specification<AppsNoticeCompanyBid>() {
            @Override
            public Predicate toPredicate(Root<AppsNoticeCompanyBid> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("fMain").get("fId"), fId);
            }
        };
        List<AppsNoticeCompanyBid> list = appsNoticeCompanyBidService.findAll(specification);

        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                AppsNoticeCompanyBid bid = list.get(i);
                if ((StringUtil.isNotNull(bidIds) && bidIds.indexOf(bid.getfId()) >= 0) || StringUtil.isNull(bidIds)) {
                    Specification<AttachmentMain> specification1 = new Specification<AttachmentMain>() {
                        @Override
                        public Predicate toPredicate(Root<AttachmentMain> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                            Predicate predicate = criteriaBuilder.equal(root.get("fModelId"), bid.getfId());
                            Predicate predicate1 = criteriaBuilder.equal(root.get("fModelName"), bid.getClass().getName());
                            Predicate predicate2 = criteriaBuilder.equal(root.get("fKey"), "companyFile");
                            return criteriaBuilder.and(predicate, predicate1, predicate2);
                        }
                    };
                    List<AttachmentMain> list1 = attachmentMainService.findAll(specification1);

                    //按照采购记录创建压缩包
                    if (!list1.isEmpty()) {
                        String zipFileName = (i + 1) + "-" + bid.getfApplyName();
                        File tempZip = File.createTempFile(zipFileName, ".zip");
                        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip));
                        Set<String> fileNamesSet = new HashSet<>();
                        for (int j = 0; j < list1.size(); j++) {
                            AttachmentMain attachmentMain = list1.get(j);
                            logger.info("load file : " + attachmentMain.getfFile().getfId());
                            if (!fileNamesSet.contains(attachmentMain.getfFileName())) {
                                fileNamesSet.add(attachmentMain.getfFileName());
                                SysFileInputStream inputStream = manager.getInputStream(attachmentMain.getfFile().getfId());
                                ZipEntry entry = new ZipEntry(inputStream.getSysFile().getfFileName());
                                zos.putNextEntry(entry);
                                StreamUtils.copy(inputStream, zos);
                                inputStream.close();
                                zos.closeEntry();
                                zos.flush();
                            }
                        }
                        zos.finish();
                        tempZipList.add(tempZip);
                    }
                }
            }

            //整合所有压缩包
            OutputStream outputStream = response.getOutputStream();
            ZipOutputStream finalZos = new ZipOutputStream(outputStream);
            for (File tempZip : tempZipList) {
                ZipEntry finalZipEntry = new ZipEntry(tempZip.getName());
                finalZos.putNextEntry(finalZipEntry);
                try (FileInputStream fis = new FileInputStream(tempZip)) {
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        finalZos.write(bytes, 0, length);
                    }
                }
                finalZos.closeEntry();
                finalZos.flush();
            }
            finalZos.finish();

            // 第三步：清理临时文件
            for (File tempZip : tempZipList) {
                if (!tempZip.delete()) {
                    System.out.println("Failed to delete temp zip file: " + tempZip.getAbsolutePath());
                }
            }

        }

    }


    /**
     * 传递公告id，下载获取标书单位 所创建的 申请文件
     *
     * @throws Exception
     */
    @GetMapping("/doDownloadApplyFiles")
    @ResponseBody
    public void doDownloadApplyFiles() throws Exception {
        String fId = request.getParameter("noticeId");
        List<File> tempZipList = new ArrayList<>();
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("申请文件.zip", "UTF-8"));

        Specification<AppsNoticeCompanyBid> specification = new Specification<AppsNoticeCompanyBid>() {
            @Override
            public Predicate toPredicate(Root<AppsNoticeCompanyBid> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("fMain").get("fId"), fId);
            }
        };
        //投标公司
        List<AppsNoticeCompanyBid> list = appsNoticeCompanyBidService.findAll(specification);

        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                AppsNoticeCompanyBid bid = list.get(i);
                //找到投标公司对应的 申请文件 main
                Specification<AppsSupplierMain> specification2 = new Specification<AppsSupplierMain>() {
                    @Override
                    public Predicate toPredicate(Root<AppsSupplierMain> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        Predicate predicate = criteriaBuilder.equal(root.get("fSupplier").get("fId"), bid.getfApplyId());
                        Predicate predicate1 = criteriaBuilder.equal(root.get("fNotice").get("fId"), fId);
                        return criteriaBuilder.and(predicate, predicate1);
                    }
                };
                List<AppsSupplierMain> list2 = appsSupplierMainService.findAll(specification2);

                for (int l = 0; l < list2.size(); l++) {
                    AppsSupplierMain main = list2.get(l);
                    //找到申请文件附件
                    Specification<AttachmentMain> specification1 = new Specification<AttachmentMain>() {
                        @Override
                        public Predicate toPredicate(Root<AttachmentMain> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                            Predicate predicate = criteriaBuilder.equal(root.get("fModelId"), main.getfId());
                            Predicate predicate1 = criteriaBuilder.equal(root.get("fModelName"), main.getClass().getName());
                            Predicate predicate2 = criteriaBuilder.equal(root.get("fKey"), "supplierMain");
                            Predicate predicate3 = criteriaBuilder.equal(root.get("fKey"), "supplierPDF");
                            // 组合 fKey 条件，使用逻辑或
                            Predicate predicateFKey = criteriaBuilder.or(predicate2, predicate3);
                            // 最终组合所有条件，使用逻辑与
                            return criteriaBuilder.and(predicate, predicate1, predicateFKey);
                        }
                    };


                    List<AttachmentMain> list1 = attachmentMainService.findAll(specification1);
                    if (!list1.isEmpty()) {
                        String zipFileName = (i + 1) + "-" + bid.getfApplyName();//公司名称
                        File tempZip = File.createTempFile(zipFileName, ".zip");
                        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip));
                        Set<String> fileNamesSet = new HashSet<>();
                        for (int j = 0; j < list1.size(); j++) {
                            AttachmentMain attachmentMain = list1.get(j);
                            logger.info("load file : " + attachmentMain.getfFile().getfId());
                            if (!fileNamesSet.contains(attachmentMain.getfFileName())) {
                                fileNamesSet.add(attachmentMain.getfFileName());
                                SysFileInputStream inputStream = manager.getInputStream(attachmentMain.getfFile().getfId());
                                ZipEntry entry = new ZipEntry(inputStream.getSysFile().getfFileName());
                                zos.putNextEntry(entry);
                                StreamUtils.copy(inputStream, zos);
                                inputStream.close();
                                zos.closeEntry();
                                zos.flush();
                            }
                        }
                        zos.finish();
                        tempZipList.add(tempZip);
                    }


                }
            }

            //整合所有压缩包
            OutputStream outputStream = response.getOutputStream();
            ZipOutputStream finalZos = new ZipOutputStream(outputStream);
            for (File tempZip : tempZipList) {
                ZipEntry finalZipEntry = new ZipEntry(tempZip.getName());
                finalZos.putNextEntry(finalZipEntry);
                try (FileInputStream fis = new FileInputStream(tempZip)) {
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        finalZos.write(bytes, 0, length);
                    }
                }
                finalZos.closeEntry();
                finalZos.flush();
            }
            finalZos.finish();

            // 第三步：清理临时文件
            for (File tempZip : tempZipList) {
                if (!tempZip.delete()) {
                    System.out.println("Failed to delete temp zip file: " + tempZip.getAbsolutePath());
                }
            }

        }

    }


}
