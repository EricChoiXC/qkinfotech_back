package com.qkinfotech.core.tendering.apps.collection.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.tendering.model.apps.collection.AppsCollectionResult;
import com.qkinfotech.core.tendering.model.apps.collection.AppsCollectionResultDetail;
import com.qkinfotech.core.tendering.model.apps.collection.AppsCollectionResultPackage;
import com.qkinfotech.core.tendering.model.apps.finalization.AppsFinalizationResultPackage;
import com.qkinfotech.core.tendering.model.apps.finalization.AppsFinalizationResults;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.util.StringUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@Controller
@RequestMapping("/appsCollectionResult")
public class AppsCollectionResultAction {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected Json2Bean json2bean;

    @Autowired
    protected Bean2Json bean2json;

    @Autowired
    protected SimpleService<AppsCollectionResultDetail> appsCollectionResultDetailService;

    @Autowired
    protected SimpleService<AppsCollectionResultPackage> appsCollectionResultPackageService;

    @Autowired
    protected SimpleService<AppsCollectionResult> appsCollectionResultService;

    @Autowired
    protected SimpleService<AppsFinalizationResults> appsFinalizationResultsService;

    @Autowired
    protected SimpleService<AppsFinalizationResultPackage> appsFinalizationResultPackageService;

    @PostMapping("detailImportFileDownload")
    @ResponseBody
    public void detailImportFileDownload() throws Exception {
        List<List<String>> data = new ArrayList<>();
        data.add(Arrays.asList("公司名称"));
        data.add(Arrays.asList("方案征集费"));
        data.add(Arrays.asList("奖金"));
        data.add(Arrays.asList("评审排序"));
        data.add(Arrays.asList("备注"));
        data.add(Arrays.asList("所选包件"));

        OutputStream outputStream = response.getOutputStream();

        ExcelWriterBuilder builder = EasyExcel.write(outputStream);
        ExcelWriterSheetBuilder sheetBuilder = builder.sheet();
        sheetBuilder.head(data).doWrite(new ArrayList<>());

        outputStream.flush();
        outputStream.close();
    }

    @PostMapping("/detailImport")
    @ResponseBody
    public void detailImport() throws Exception {
        JSONObject body = getPostData();
        Part part = request.getPart("file");
        InputStream is = part.getInputStream();
        List<Map<String, Object>> dataList = new ArrayList<>(); // 存储解析后的数据
        JSONObject json = new JSONObject();

        //读取导入文件信息
        try {
            // 创建 ExcelReaderBuilder 对象
            ExcelReaderBuilder excelReaderBuilder = EasyExcel.read(is).headRowNumber(0);
            // 创建 ExcelReaderSheetBuilder 对象
            ExcelReaderSheetBuilder sheetBuilder = excelReaderBuilder.sheet();
            // 设置监听器处理数据
            sheetBuilder.registerReadListener(new CustomReadListener(dataList));
            // 执行读取操作
            sheetBuilder.doRead();
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("解析 Excel 文件失败：" + e.getMessage());
            json.put("result", false);
            json.put("message", "解析 Excel 文件失败：" + e.getMessage());
        } finally {
            // 关闭输入流
            if (is != null) {
                is.close();
            }
        }

        //处理数据
        if (!json.containsKey("result")) {
            handleImport(body, dataList, json);
        }
        if (!json.containsKey("result")) {
            json.put("result", true);
        }
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().print(json.toJSONString());
    }

    public void handleImport(JSONObject body, List<Map<String, Object>> dataList, JSONObject json) throws Exception {
        //当前项目id
        String projectId = request.getHeader("ProjectId");
        //最大包件数
        String maxLength = request.getHeader("maxLength");
        //获取项目所有入围结果
        Specification<AppsFinalizationResultPackage> spec = new Specification<AppsFinalizationResultPackage>() {
            @Override
            public Predicate toPredicate(Root<AppsFinalizationResultPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("fProjectId").get("fId"), projectId);
            }
        };
        List<AppsFinalizationResultPackage> allResults = appsFinalizationResultPackageService.findAll(spec);
        //获取所有入围结果的供应商和包件
        Map<String, Set<String>> suppliersMap = new HashMap<>();
        Map<String, OrgPerson> suppliers = new HashMap<>();
        Map<String, AppsProjectPackage> packages = new HashMap<>();
        allResults.stream().forEach(appsFinalizationResultPackage -> {
            try {
                String supplierName = appsFinalizationResultPackage.getfCompanyId().getfName();
                String packageName = appsFinalizationResultPackage.getfPackageId().getfName();
                packages.put(packageName, appsFinalizationResultPackage.getfPackageId());
                if (suppliersMap.containsKey(supplierName)) {
                    suppliersMap.get(supplierName).add(packageName);
                } else {
                    Set<String> packageSet = new HashSet<>();
                    packageSet.add(packageName);
                    suppliersMap.put(supplierName, packageSet);
                    suppliers.put(supplierName, appsFinalizationResultPackage.getfCompanyId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        JSONArray successJson = new JSONArray();
        Set<String> inputedLine = new HashSet<>();
        //导入数据，要求供应商和包件在同一条记录中
        for (int i=0; i<dataList.size(); i++) {
            try {
                Map<String, Object> line = dataList.get(i);
                String companyName = String.valueOf(line.get("公司名称"));
                String packageName = String.valueOf(line.get("所选包件"));
                String cost = String.valueOf(line.get("方案征集费"));
                String bonus = String.valueOf(line.get("奖金"));
                String idx = String.valueOf(line.get("评审排序"));
                if ("null".equals(companyName)) {
                    throw new Exception("未填写公司名称");
                } else if (!suppliersMap.containsKey(companyName)) {
                    throw new Exception("该公司不存在入围记录");
                }
                if ("null".equals(packageName)) {
                    throw new Exception("未填写所选包件");
                }
                if ("null".equals(cost)) {
                    throw new Exception("未填写方案征集费（无请填写0）");
                } else if (!isFloat(cost)) {
                    throw new Exception("方案征集费填写错误");
                }
                if ("null".equals(bonus)) {
                    throw new Exception("未填写奖金（无请填写0）");
                } else if (!isFloat(bonus)) {
                    throw new Exception("奖金填写错误");
                }
                //校验评审排序 是否超过包件最大数量
                if(!isInt(idx)){
                    throw new Exception("评审排序填写错误");
                } else if (Integer.parseInt(idx) > Integer.parseInt(maxLength)) {
                    throw new Exception("评审排序不可超过最大包件数");
                }
                //
                Set<String> packageSet = suppliersMap.get(companyName);
                if (!packageSet.contains(packageName)) {
                    throw new Exception("该公司未入围该包件");
                }
                line.put("companyId", suppliers.get(companyName).getfId());
                line.put("packageId", packages.get(packageName).getfId());
                line.put("packageIndex", packages.get(packageName).getfIndex());
                if (inputedLine.contains(suppliers.get(companyName).getfId() + ";" + packages.get(packageName).getfId())) {
                    throw new Exception("重复的记录");
                } else {
                    inputedLine.add(suppliers.get(companyName).getfId() + ";" + packages.get(packageName).getfId());
                }
                successJson.add(new JSONObject(line));
            } catch (Exception e) {
                json.put("result", false);
                String message = json.containsKey("message") ? (String) json.get("message") : "";
                message += "行" + (i+1) + ":" + e.getMessage() + "\n";
                json.put("message", message);
            }
        }
        json.put("successLine", successJson);
    }


    // 自定义监听器处理读取的数据
    private static class CustomReadListener implements ReadListener<Object> {
        private List<Map<String, Object>> dataList;
        private Map<Integer, String> headers = new HashMap<Integer, String>();

        public CustomReadListener(List<Map<String, Object>> dataList) {
            this.dataList = dataList;
        }

        @Override
        public void onException(Exception exception, AnalysisContext context) throws Exception {
            // 异常处理
            exception.printStackTrace();
        }

        @Override
        public void invoke(Object data, AnalysisContext context) {
            if (context.readSheetHolder().getSheetNo() == 0) {
                if (context.readSheetHolder().getRowIndex() == 0) {
                    for (Integer i : ((Map<Integer, Object>)data).keySet()) {
                        String head = ((Map<Integer, Object>) data).get(i).toString();
                        // head = head.replace("(*)", "");
                        headers.put(i, head);
                    }
                } else {
                    Map<String, Object> rowData = new HashMap<>();
                    for (int i = 0; i < headers.size(); i++) {
                        Object value = ((Map<Integer, Object>) data).get(i);
                        rowData.put(headers.get(i), value != null ? value.toString() : "");
                    }
                    dataList.add(rowData);
                }
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            // 所有数据读取完成后的操作
            System.out.println("All data read completed.");
        }
    }



    private JSONObject getPostData() {
        JSONObject data = new JSONObject();
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
        return data;
    }

    public static boolean isFloat(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        try {
            // Try parsing the string as a float
            Float.parseFloat(str);
            return true;
        } catch (NumberFormatException e) {
            // If parsing fails, the string is not a valid float
            return false;
        }
    }


    public static boolean isInt(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        try {
            // Try parsing the string as a float
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            // If parsing fails, the string is not a valid float
            return false;
        }
    }

    @PostMapping("/deleteOldDetail")
    @ResponseBody
    public void deleteOldDetail () throws Exception {
        JSONObject body = getPostData();
        String resultId = body.getString("fResultId");
        if (StringUtil.isNotNull(resultId)) {
            Specification<AppsCollectionResultPackage> spec = new Specification<AppsCollectionResultPackage>() {
                @Override
                public Predicate toPredicate(Root<AppsCollectionResultPackage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.equal(root.get("appsCollectionResultDetail").get("fResultId").get("fId"), resultId);
                }
            };
            List<AppsCollectionResultPackage> list = appsCollectionResultPackageService.findAll(spec);
            for (AppsCollectionResultPackage acrp : list) {
                appsCollectionResultPackageService.delete(acrp);
            }


            Specification<AppsCollectionResultDetail> spec2 = new Specification<AppsCollectionResultDetail>() {
                @Override
                public Predicate toPredicate(Root<AppsCollectionResultDetail> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.equal(root.get("fResultId").get("fId"), resultId);
                }
            };
            List<AppsCollectionResultDetail> list2 = appsCollectionResultDetailService.findAll(spec2);
            for (AppsCollectionResultDetail acrd : list2) {
                appsCollectionResultDetailService.delete(acrd);
            }
        }
    }
}
