package com.qkinfotech.core.tendering.controller;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.file.FileManager;
import com.qkinfotech.core.file.SysFile;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import com.qkinfotech.core.tendering.model.attachment.AttachmentPackage;
import com.qkinfotech.core.tendering.service.FileMainService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.compress.utils.IOUtils;
import org.openqa.selenium.Pdf;
import org.openqa.selenium.PrintsPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.print.PageSize;
import org.openqa.selenium.print.PrintOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/supplier/pdf/")
public class supplierPdfController<T extends BaseEntity> {
    @Autowired
    protected HttpServletRequest request;
    @Autowired
    protected SimpleResult result;
    @Autowired
    protected SimpleService<SysFile> sysFileService;
    @Autowired
    protected SimpleService<AttachmentMain> attachmentMainService;
    @Autowired
    protected SimpleService<AttachmentPackage> attachmentPackageService;
    @Autowired
    protected SimpleService<AppsProjectPackage> appsProjectPackageService;
    @Autowired
    protected FileMainService fileMainService;
    @Autowired
    protected FileManager fileManager;

    /**
     * 预览pdf 打印
     */
    @RequestMapping("/pdfDL")
    @ResponseBody

    public void pdfDownLoad(@RequestBody JSONObject htmlContent) throws Exception {
        //
        String html = htmlContent.getString("html");
        String id = htmlContent.getString("fId");
        String name = htmlContent.getString("name");//公司名称
        String packageNames = htmlContent.getString("packageNames");//包件名称
        packageNames = packageNames.substring(0, packageNames.length() - 1);//去除最后的;符号
        String packageIds = htmlContent.getString("packageIds");
        String fileName = name + "(" + packageNames + ")" + ".pdf";
        // 指定保存文件的路径（D盘根目录下的example.html）
//        String filePath = "D:/" + id + ".html";
//        String filePath = "/home/html/" + id + ".html";
        String filePath = "/mnt/nfs1/html/" + id + ".html";
        // 写入HTML内容到文件
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println(html);
            System.out.println("HTML文件已生成并保存至：" + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            System.setProperty("webdriver.chrome.driver", "/Users/57422/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe");
            System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");

            WebDriver webDriver = null;
            ChromeOptions options = new ChromeOptions();
//            String outputPath = "/Users/57422/Downloads/";
            String outputPath = "/mnt/nfs1/pdf/";
            com.alibaba.fastjson.JSONObject obj = new com.alibaba.fastjson.JSONObject();
            try {
                options.addArguments("--headless=new");
                options.addArguments("--remote-allow-origins=*");
                options.addArguments("--disable-gpu");
                options.addArguments("--no-sandbox");
                options.addArguments("--print-paper-size=452mm,254mm");
//                options.addArguments("--kiosk-printing");
//                options.addArguments("--print-orientation=landscape");//横向打印
//                options.addArguments("--print-backgrounds=true");//背景图形
                webDriver = new ChromeDriver(options);
                webDriver.get("file://" + filePath);
//                webDriver.get(filePath);
                Thread.sleep(3000);
                PrintOptions op = new PrintOptions();
                op.setPageSize(new PageSize(10.0 ,17.7952755906));
                PrintsPage printer = ((PrintsPage) webDriver);
                Pdf pdf = printer.print(op);
                String content = pdf.getContent();
                byte[] b = Base64.getDecoder().decode(content);
                Path path = Paths.get(outputPath + fileName);
                Files.write(path, b);
                //保存到sys file
                InputStream inputStream = Files.newInputStream(path);
                String fileId = fileMainService.saveInputStream(inputStream, fileName);
                //查询是否已经有记录
                List<AttachmentMain> attachmentMains = fileMainService.getAttMain("com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain", id, "supplierMain");
                if (attachmentMains.isEmpty()) {
                    //没有就新增
                    AttachmentMain attMain = new AttachmentMain();
                    fileMainService.saveAttMain(attMain, fileId, fileName, "com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain", id, "supplierMain", packageIds);
                } else {
                    //有就更新
                    AttachmentMain attMain = attachmentMains.get(0);
                    fileMainService.saveAttMain(attMain, fileId, fileName, "com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain", id, "supplierMain", packageIds);
                }
                //
                obj.put("status", true);
                obj.put("result", content);
            } catch (Exception e) {
                e.printStackTrace();
                obj.put("status", false);
                obj.put("msg", e.getMessage());
            } finally {
                webDriver.quit();
            }
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//        response.getWriter().println(obj.toString());
        }
    }

    @RequestMapping("/getPic")
    @ResponseBody

    public void getPic(@RequestBody JSONArray array) throws Exception {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            String fModelId = array.getJSONObject(i).getString("fId");
            String fModelName = array.getJSONObject(i).getString("fModelName");
            String fKey = array.getJSONObject(i).getString("fKey");
            List<AttachmentMain> attachmentMains = fileMainService.getAttMain(fModelName, fModelId, fKey);
            if (!attachmentMains.isEmpty()) {
                InputStream inputStream = fileManager.getInputStream(attachmentMains.get(0).getfFile().getfId());
                byte[] bytes = IOUtils.toByteArray(inputStream);
                String base64Image = Base64.getEncoder().encodeToString(bytes);
                jsonObject.put("src", base64Image);
            } else {
                jsonObject.put("src", "");
            }
            jsonArray.add(jsonObject);
        }
        result.from(jsonArray);
    }

    private String randomUUID() {
        // 生成一个UUID
        UUID uuid = UUID.randomUUID();
        // 转换为带横杠的字符串
        String uuidWithDashes = uuid.toString();
        // 去除横杠
        return uuidWithDashes.replace("-", "");
    }

    /**
     * html转Pdf
     *
     * @param htmlContent
     * @throws Exception
     */
    @RequestMapping("/htmlConvertPdf")
    @ResponseBody
    public void htmlConvertPdf(@RequestBody JSONObject htmlContent) throws Exception {
        String html = htmlContent.getString("html");
        String id = htmlContent.getString("fId");
        String key = htmlContent.getString("fkey");
        String fileName = randomUUID();
        // 指定保存文件的路径（D盘根目录下的example.html）
//        String filePath = "D:/temp/" + fileName + ".html";
        String filePath = "/mnt/nfs1/html/" + fileName + ".html";
        // 写入HTML内容到文件
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println(html);
            System.out.println("HTML文件已生成并保存至：" + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            System.setProperty("webdriver.chrome.driver", "/Users/57422/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe");
            System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
            WebDriver webDriver = null;
            ChromeOptions options = new ChromeOptions();
//            String outputPath = "/Users/57422/Downloads/";
            String outputPath = "/mnt/nfs1/html/pdf/";
            com.alibaba.fastjson.JSONObject obj = new com.alibaba.fastjson.JSONObject();
            try {
                options.addArguments("--headless");
                options.addArguments("--remote-allow-origins=*");
                options.addArguments("--disable-gpu");
                options.addArguments("--no-sandbox");
                webDriver = new ChromeDriver(options);
                webDriver.get("file://" + filePath);
                Thread.sleep(3000);
                PrintOptions op = new PrintOptions();
                PrintsPage printer = ((PrintsPage) webDriver);
                Pdf pdf = printer.print(op);
                String content = pdf.getContent();
                byte[] b = Base64.getDecoder().decode(content);
                String pdfFileName = fileName + ".pdf";
                Path path = Paths.get(outputPath + pdfFileName);
                Files.write(path, b);
                //保存到sys file
                InputStream inputStream = Files.newInputStream(path);
                String fileId = fileMainService.saveInputStream(inputStream, pdfFileName);
                String modelName = "com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain";
//                String key = "electronDocument";
                //查询是否已经有记录
                List<AttachmentMain> attachmentMains = fileMainService.getAttMain(modelName, id, key);
                //附件对象
                AttachmentMain attMain = null;
                if (attachmentMains.isEmpty()) {
                    //没有就新增
                    attMain = new AttachmentMain();
                    fileMainService.saveAttMain(attMain, fileId, pdfFileName, modelName, id, key, "");
                } else {
                    //有就更新
                    attMain = attachmentMains.get(0);
                    fileMainService.saveAttMain(attMain, fileId, pdfFileName, modelName, id, key, "");
                }
                obj.put("status", true);
                obj.put("result", content);
            } catch (Exception e) {
                e.printStackTrace();
                obj.put("status", false);
                obj.put("msg", e.getMessage());
            } finally {
                webDriver.quit();
            }
        }
    }
}
