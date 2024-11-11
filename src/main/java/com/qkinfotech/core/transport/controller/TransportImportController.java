package com.qkinfotech.core.transport.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.transport.model.TransportImportKey;
import com.qkinfotech.core.transport.model.TransportImportMain;
import com.qkinfotech.core.transport.model.TransportImportProperties;
import com.qkinfotech.core.transport.model.TransportImportPropertyKey;
import com.qkinfotech.core.user.model.SysUser;
import com.qkinfotech.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.hpsf.Decimal;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 导入定制化Controller
 * @author 蔡咏钦
 */
@Controller
@RequestMapping("/transportImport")
public class TransportImportController {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected Bean2Json bean2json;

    @Autowired
    protected Json2Bean json2bean;

    @Autowired
    private SimpleService<SysUser> sysUserService;

    @Autowired
    private SimpleService<TransportImportMain> transportImportMainService;

    @Autowired
    private SimpleService<TransportImportKey> transportImportKeyService;

    @Autowired
    private SimpleService<TransportImportProperties> transportImportPropertiesService;

    @Autowired
    private SimpleService<TransportImportPropertyKey> transportImportPropertyKeyService;

    private static List<Class<?>> rawTypes = List.of(String.class, byte.class, Byte.class, short.class, Short.class, char.class, Character.class, int.class, Integer.class, long.class, Long.class,
            float.class, Float.class, double.class, Double.class, Number.class, Decimal.class, BigDecimal.class, boolean.class, Boolean.class);


    /**
     * 保存导入模板
     */
    @RequestMapping("/save")
    @ResponseBody
    public void save() throws Exception {
        JSONObject data = new JSONObject();
        try {
            JSONObject body = getPostData();
            JSONObject json = body.getJSONObject("transportImportMain");
            TransportImportMain main = transportImportMainService.getById(json.getString("fId"));
            if (main == null) {
                main = new TransportImportMain();
                main.setfId(json.getString("fId"));
                main.setfModelName(json.getString("fModelName"));
                main.setfCreatTime(new Date());
                if (StringUtil.isNotNull(body.getString("creator"))) {
                    main.setfCreator(sysUserService.getById(body.getString("creator")));
                }
            }
            main.setfName(json.getString("fName"));

            //导入列
            if (main.getfProperties() == null) {
                main.setfProperties(new HashSet<TransportImportProperties>());
            }
            if (!main.getfProperties().isEmpty()) {
                main.getfProperties().removeAll(main.getfProperties());
            }
            List<String> properties = json.getList("fProperties", String.class);
            for (var i = 0; i < properties.size(); i++) {
                String name = properties.get(i);
                TransportImportProperties property = new TransportImportProperties();
                property.getfId();
                property.setfMain(main);
                property.setfOrder(i);
                property.setfName(name);
                property.setfKeys(new ArrayList<TransportImportPropertyKey>());
                //导入对象列的关键字
                List<String> objectKeys = json.getList("fObjectKeys", String.class);
                for (var j = 0; j < objectKeys.size(); j++) {
                    String objectKeyName = objectKeys.get(j);
                    if (objectKeyName.startsWith(name + ".")) {
                        TransportImportPropertyKey objectKey = new TransportImportPropertyKey();
                        objectKey.setfKey(objectKeyName);
                        objectKey.setfProperty(property);
                        property.getfKeys().add(objectKey);
                    }
                }
                main.getfProperties().add(property);
            }

            //关键字
            if (main.getfKey() == null) {
                main.setfKey(new HashSet<TransportImportKey>());
            }
            if (!main.getfKey().isEmpty()) {
                main.getfKey().removeAll(main.getfKey());
            }
            List<String> keys = json.getList("fKey", String.class);
            for (var i = 0; i < keys.size(); i++) {
                TransportImportKey key = new TransportImportKey();
                key.setfMain(main);
                key.setfName(keys.get(i));
                main.getfKey().add(key);
            }

            transportImportMainService.save(main);
            data.put("success", true);
            output(200, data);
        } catch (Exception e) {
            e.printStackTrace();
            data.put("message", e.getMessage());
            output(500, data);
        }
    }

    /**
     * 下载导入模板的excel
     */
    @RequestMapping("/download")
    @ResponseBody
    public void download() throws Exception {
        JSONObject body = getPostData();

        String mainId = body.getString("mainId");
        TransportImportMain main = transportImportMainService.getById(mainId);
        if (main != null) {
            List<List<String>> data = new ArrayList<>();
            List<String> keys = main.getfKey()
                    .stream()
                    .map(TransportImportKey::getfName)
                    .collect(Collectors.toList());

            List<TransportImportProperties> properties = main.getfProperties()
                    .stream()
                    .sorted((item1, item2) -> Integer.compare(item1.getfOrder(), item2.getfOrder()))
                    .collect(Collectors.toList());

            for (int i=0; i<properties.size(); i++) {
                List<String> line = new ArrayList<>();
                TransportImportProperties property = properties.get(i);
                if (property.getfKeys() != null && !property.getfKeys().isEmpty()) {
                    for (int j=0; j<property.getfKeys().size(); j++) {
                        line.add(property.getfKeys().get(j).getfKey());
                        data.add(line);
                        line = new ArrayList<>();
                    }
                } else {
                    if (keys.contains(property.getfName())) {
                        line.add(property.getfName() + "(*)");
                    } else {
                        line.add(property.getfName());
                    }
                    data.add(line);
                }
            }

            OutputStream outputStream = response.getOutputStream();

            ExcelWriterBuilder builder = EasyExcel.write(outputStream);
            ExcelWriterSheetBuilder sheetBuilder = builder.sheet();
            sheetBuilder.head(data).doWrite(new ArrayList<>());

            outputStream.flush();
            outputStream.close();
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

    public void output(int code, JSONObject data) throws Exception {
        data.put("status", code);

        String callback = request.getParameter("callback");
        if (StringUtils.hasText(callback)) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            String script = data.toJSONString();
            String result = callback + "(" + script + ")";
            response.getWriter().print(result);
        } else {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().print(data.toJSONString());
        }
    }

}
