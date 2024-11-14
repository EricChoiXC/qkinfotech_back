package com.qkinfotech.core.mvc;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.alibaba.excel.read.listener.ReadListener;
import com.alicp.jetcache.Cache;
import com.qkinfotech.core.app.config.JetcacheConfig;
import com.qkinfotech.core.sys.base.service.PluginService;
import com.qkinfotech.util.EntityUtil;
import com.qkinfotech.util.SpringUtil;
import com.qkinfotech.util.StringUtil;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hpsf.Decimal;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.qkinfotech.core.mvc.util.Bean2Json;
import com.qkinfotech.core.mvc.util.Json2Bean;
import com.qkinfotech.core.mvc.util.QueryBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Transactional
@DependsOn("entityManager")
@Slf4j
public class SimpleController<T extends BaseEntity> {

    protected static final String IDS_DELIMITERS = ",; \t\n";

    @Autowired
    ApplicationContext context;

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected SimpleResult result;

    protected SimpleService<T> service;

    protected Class<T> modelClass;

    @Autowired
    protected Bean2Json bean2json;

    @Autowired
    protected Json2Bean json2bean;

    @Autowired(required = false)
    protected List<IEntityExtension> extensions;

    @Autowired
    JetcacheConfig jetcacheConfig;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionStatus status = null;

    @Autowired
    private EntityManager em;

    @Autowired
    private Cache<String, Object> userCache;

    private static List<Class<?>> rawTypes = List.of(String.class, byte.class, Byte.class, short.class, Short.class, char.class, Character.class, int.class, Integer.class, long.class, Long.class,
            float.class, Float.class, double.class, Double.class, Number.class, Decimal.class, BigDecimal.class, boolean.class, Boolean.class);

    public SimpleController(SimpleService<T> service) {
        this.modelClass = service.getEntityClass();
        this.service = service;
    }

    public Class<T> getModelClass() {
        return modelClass;
    }

    @RequestMapping("/load")
    @ResponseBody
    public void load() throws Exception {
        try {
            String fId = request.getParameter("fId");
            JSONObject data = getPostData("load");
            if (!StringUtils.hasText(fId)) {
                if (data != null) {
                    fId = data.getString("fId");
                }
            }
            if (!StringUtils.hasText(fId)) {
                throw new IllegalArgumentException("fId is empty");
            }
            T d = service.getById(fId);

            List<String> output = data.getList("output", String.class);
            if (output == null || output.size() == 0) {
                result.from(bean2json.toJson(d));
            } else {
                result.from((JSONObject) bean2json.toJson(d, output));
            }

        } catch (Exception e) {
            logger.error(String.valueOf(request.getRequestURL()));
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 根据主键删除
     */
    @RequestMapping("/delete")
    @ResponseBody
    public void delete() throws Exception {
        List<String> fId = new ArrayList<>();
        JSONObject data = getPostData("delete");
        String[] v = request.getParameterValues("fId");
        if (v != null) {
            fId.addAll(Arrays.asList(v));
        }
        if (fId.isEmpty()) {
            if (data != null) {
                List<String> d = data.getList("fId", String.class);
                if (d != null) {
                    fId.addAll(d);
                }
            }
        }
        Set<String> ids = new LinkedHashSet<String>();
        for (Object id : fId) {
            if (id instanceof String s) {
                String[] tokenized = StringUtils.tokenizeToStringArray(s, IDS_DELIMITERS);
                Collections.addAll(ids, tokenized);
            } else {
                throw new IllegalArgumentException("typeof fId is unknow");
            }

        }

        // 遍历调用所有pluginService的deleteAll方法
        JSONObject pluginValue = data.getJSONObject("#pluginValue");
        Map<String, PluginService> pluginServiceMap = getPluginServices();
        JSONObject deleteAllJson = new JSONObject();
        deleteAllJson.put("beanName", context.getBeanNamesForType(service.getClass())[0]);
        deleteAllJson.putAll(pluginValue);
        for (PluginService pluginService : pluginServiceMap.values()) {
            try {
                pluginService.deleteAll(ids, deleteAllJson);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        service.delete(StringUtils.toStringArray(ids));
        result.ok();
    }

    @RequestMapping("/save")
    @ResponseBody
    public void save() throws Exception {
        try {
            JSONObject body = getPostData("save");
            if (body == null) {
                throw new IllegalArgumentException("illegal request body");
            }

            T target = json2bean.toBean(body, service.getEntityClass());

            service.save(target);
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

            boolean isUpdate = false;
            TransactionStatus status = transactionManager.getTransaction(def);
            try {
                T model = service.getById(target.getfId());
                if (model != null) {
                    isUpdate = true;
                }
            } catch (RuntimeException ex) {
                throw ex;
            } finally {
                transactionManager.rollback(status);
            }

            JSONObject pluginValue = body.getJSONObject("#pluginValue");
            for (String key : body.keySet()) {
                if (key.startsWith("$")) {
                    try {
                        PluginService pluService = SpringUtil.getContext().getBean(key.replace("$", ""), PluginService.class);
                        JSONObject parameter = new JSONObject();
                        parameter.put("beanName", context.getBeanNamesForType(service.getClass())[0]);
                        parameter.putAll(pluginValue);
                        parameter.put("parameter", body.get(key));
                        pluService.save(target, parameter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            result.ok();

        } catch (Exception e) {
            logger.error(String.valueOf(request.getRequestURL()));
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping("/list")
    @ResponseBody
    public void list() throws Exception {
        try {
            JSONObject body = getPostData("list");

            QueryBuilder<T> qb = QueryBuilder.parse(modelClass, body);

            Object[] auths = getAuths();

            Page<T> data = service.findAll(qb.specification(), qb.pageable());

            //2024-09-02 增加超出查询数量的处理
            if (body.containsKey("pagesize")) {
                int pagesize = body.getIntValue("pagesize");
                if (pagesize > 500 || pagesize < 15) {
                    pagesize = 15;
                }
                int pagenum = body.getIntValue("pagenum");
                if (pagenum < 0) {
                    pagenum = 0;
                }
                if (pagesize * pagenum >= data.getTotalElements()) {
                    pagenum = data.getTotalPages() - 1;
                    if (pagenum < 0) {
                        pagenum = 0;
                    }
                    qb.setPageable(PageRequest.of(pagenum, pagesize, qb.sort()));
                    data = service.findAll(qb.specification(), qb.pageable());
                }
            }

            List<String> output = body.getList("output", String.class);
            if (output == null || output.size() == 0) {
                Page<JSONObject> out = data.map(e -> bean2json.toJson(e));
                result.from(out);
            } else {
                Page<JSONObject> out = data.map(e -> {
                    try {
                        return (JSONObject) bean2json.toJson(e, output);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
                result.from(out);
            }
        } catch (Exception e) {
            logger.error(String.valueOf(request.getRequestURL()));
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping("/init")
    @ResponseBody
    public void init() throws Exception {
        JSONObject body = getPostData("init");

        T model = json2bean.toBean(body, service.getEntityClass());

        if (extensions != null) {
            for (int i = 0; i < extensions.size(); ++i) {
                extensions.get(i).init(model);
            }
        }
        result.from(bean2json.toJson(model));
    }


    private static class Exporter<T> {

        private ExcelWriter writer;

        private WriteSheet sheet;

        private List<String> columns;

        private List<Map<Integer, Object>> output;

        private Map<Integer, Object> map;

        public Exporter(ExcelWriter writer, WriteSheet sheet, List<String> columns) {
            this.writer = writer;
            this.sheet = sheet;
            this.columns = columns;
            this.map = new HashMap<>();
            this.output = new ArrayList<>(1);
            output.add(map);
        }

        public void output(T data) {
            map.clear();
            for (int i = 0; i < columns.size(); ++i) {
                map.put(i, JSONPath.of("$." + columns.get(i)).eval(data));
            }
            writer.write(output, sheet);
        }
    }

    @RequestMapping("/export")
    @ResponseBody
    public void exports() throws Exception {
        JSONObject body = getPostData("export");

        // 解析 post 的 query
        QueryBuilder<T> qb = QueryBuilder.parse(modelClass, body);

        // 解析 post 的 column header
        List<String> columns = body.getList("columns", String.class);

        List<List<String>> head = new ArrayList<>();
        List<String> column = new ArrayList<>();
        if (columns == null || columns.isEmpty()) {
            columns = new ArrayList<>();
            EntityUtil eu = new EntityUtil();
            List<Field> list = eu.getAllBaseFields(modelClass);
            for (Field field : list) {
                if (rawTypes.contains(field.getType())) {
                    columns.add(field.getName());
                }
            }
        }
        for (String val : columns) {
            boolean isJson = false;
            try {
                JSONObject.parseObject(val);
                isJson = true;
            } catch (Exception e) {

            }
            if (isJson) {
                JSONObject json = JSONObject.parseObject(val);
                head.add(List.of(json.getString("title")));
                column.add(json.getString("field"));
            } else {
                head = columns.stream().map(o -> List.of(o)).toList();
                column = columns;
            }
        }
        // head = columns.stream().map(o -> List.of(o)).toList();
        OutputStream outputStream = response.getOutputStream();

        ExcelWriter writer = EasyExcelFactory.write(outputStream).build();
        WriteSheet sheet = EasyExcelFactory.writerSheet("data").head(head).build();

        Exporter<T> exporter = new Exporter<T>(writer, sheet, column);
        service.scroll(qb.specification(), exporter::output);

        writer.finish();
        writer.close();
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
                    for (Integer i : ((Map<Integer, Object>) data).keySet()) {
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

    @RequestMapping("/import")
    @ResponseBody
    public void imports() throws Exception {
        JSONObject body = getPostData("import");
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
            handleImport(dataList, json);
        }

        if (!json.containsKey("result")) {
            json.put("result", true);
        }
        result.from(json);
    }

    /**
     * 处理导入数据
     */
    public void handleImport(List<Map<String, Object>> dataList, JSONObject json) throws Exception {
        for (int i = 0; i < dataList.size(); i++) {
            try {
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                def.setIsolationLevel(TransactionDefinition.PROPAGATION_MANDATORY);
                status = transactionManager.getTransaction(def);
                Map<String, Object> map = dataList.get(i);
                //1.先遍历出必填字段（关键字），基于此找出是否已存在相关数据
                T obj = importFind(map);
                //2.若不存在，则新建
                if (obj == null) {
                    obj = json2bean.toBean(new JSONObject(), service.getEntityClass());
                }
                //3.更新数据
                Map<String, Set<String>> objSet = new HashMap<>();//对象的关键字Set，需要全部遍历之后才能做查询
                for (String key : map.keySet()) {
                    String k = key.replace("(*)", "");
                    if (k.indexOf(".") > 0) {
                        String[] ks = k.split("\\.");
                        if (ks.length == 2) {
                            if (objSet.containsKey(ks[0])) {
                                if (!StringUtil.isNull(map.get(key).toString())) {
                                    objSet.get(ks[0]).add(ks[1]);
                                }
                            } else {
                                Set<String> temp = new HashSet<>();
                                if (!StringUtil.isNull(map.get(key).toString())) {
                                    temp.add(ks[1]);
                                }
                                objSet.put(ks[0], temp);
                            }
                        }
                    } else {
                        Field field = obj.getClass().getDeclaredField(k);
                        field.setAccessible(true);
                        field.set(obj, StringUtil.isNull(map.get(key).toString()) ? null : map.get(key).toString());
                    }
                }
                for (String key : objSet.keySet()) {
                    Set<String> ks = objSet.get(key);
                    if (ks.isEmpty()) {
                        Field field = obj.getClass().getDeclaredField(key);
                        field.setAccessible(true);
                        field.set(obj, null);
                        continue;
                    }
                    Class clz2 = obj.getClass().getDeclaredField(key).getType();
                    Specification<T> spec = new Specification() {
                        @Override
                        public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder criteriaBuilder) {
                            List<Predicate> list = new ArrayList<>();
                            for (String k : ks) {
                                Predicate predicate = criteriaBuilder.equal(root.get(k), map.get(key + "." + k));
                                list.add(predicate);
                            }
                            return query.where(list.toArray(new Predicate[list.size()])).getRestriction();
                        }
                    };
                    service.setRepository(new SimpleRepository<T>(clz2, em));
                    //T target = (T) service.findOne(spec);
                    List list = service.findAll(spec);
                    if (list.size() != 1) {
                        throw new Exception("无法定位出唯一的" + key);
                    }
                    Field field = obj.getClass().getDeclaredField(key);
                    field.setAccessible(true);
                    field.set(obj, list.get(0));
                    service.setRepository(new SimpleRepository<T>(this.modelClass, em));
                }
                //4.保存
                service.save(obj);
                status.flush();
                transactionManager.commit(status);
            } catch (Exception e) {
                //5.若出错，记录行号，问题，并反馈回前台
                e.printStackTrace();
                json.put("result", false);
                String message = json.containsKey("message") ? (String) json.get("message") : "";
                message += "\n 行" + (i + 1) + ":";
                if (e.getClass() != null) {
                    message += e.getClass().getName() + " ";
                }
                message += e.getMessage();
                json.put("message", message);
                service.setRepository(new SimpleRepository<T>(this.modelClass, em));
            }
        }
        service.setRepository(new SimpleRepository<T>(this.modelClass, em));
        // transactionManager.commit(status);
		/*if (json.containsKey("result") && !json.getBoolean("result")) {
			transactionManager.rollback(status);
		} else {
			transactionManager.commit(status);
		}*/
    }

    public T importFind(Map<String, Object> map) throws Exception {
        Specification<T> spec = new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> list = new ArrayList<>();
                for (String key : map.keySet()) {
                    if (key.indexOf("(*)") > 0) {
                        String[] k = key.replace("(*)", "").split("\\.");
                        Path find = root;
                        for (int i = 0; i < k.length; i++) {
                            find = find.get(k[i]);
                        }
                        Predicate predicate = criteriaBuilder.equal(find, map.get(key));
                        list.add(predicate);
                    }
                }
                return query.where(list.toArray(new Predicate[list.size()])).getRestriction();
            }
        };

        T obj = service.findOne(spec);
        return obj;
    }


    private JSONObject getPostData(String method) {
        JSONObject data = new JSONObject();
        if ("POST".equals(request.getMethod())) {
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
        }
        if (extensions != null) {
            for (int i = 0; i < extensions.size(); ++i) {
                extensions.get(i).prepare(modelClass, method, data);
            }
        }
        return data;
    }

    public String getFidFromCookies() {
        String fId = "";
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if ("id".equals(cookie.getName())) {
                fId = cookie.getValue();
            }
        }
        return fId;
    }

    public Object[] getAuths() {
        try {
            String fId = getFidFromCookies();
            JSONObject authJson = (JSONObject) jetcacheConfig.getCache(fId);
            return (Object[]) authJson.get("auths");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取模块的所有基础字段（除集合s）
     *
     * @throws Exception
     */
    @RequestMapping("/getBaseFields")
    @ResponseBody
    public void getBaseFields() throws Exception {
        EntityUtil eu = new EntityUtil();
        List<Field> list = eu.getAllBaseFields(this.modelClass);
        List<JSONObject> columns = new ArrayList<>();
        for (Field field : list) {
            if (rawTypes.contains(field.getType())) {
                if (field.isAnnotationPresent(Column.class)) {
                    JSONObject temp = new JSONObject();
                    temp.put("name", field.getName());
                    Column columnAnnotation = field.getAnnotation(Column.class);
                    temp.put("disabled", !columnAnnotation.nullable());
                    temp.put("label", field.getName());
                    temp.put("value", field.getName());
                    columns.add(temp);
                }
            } else if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
                JSONObject temp = new JSONObject();
                temp.put("name", field.getName());
                temp.put("label", field.getName());
                temp.put("value", field.getName());
                JoinColumn columnAnnotation = field.getAnnotation(JoinColumn.class);
                if (columnAnnotation != null) {
                    temp.put("disabled", !columnAnnotation.nullable());
                } else {
                    temp.put("disabled", false);
                }
                List<Field> childFields = eu.getAllBaseFields(field.getType());
                Set<String> childKeys = new HashSet<>();
                for (Field child : childFields) {
                    if (rawTypes.contains(child.getType())) {
                        if (child.isAnnotationPresent(Column.class)) {
                            childKeys.add(child.getName());
                        }
                    }
                }
                temp.put("keys", childKeys);
                columns.add(temp);
            }
        }
        JSONObject json = new JSONObject();
        json.put("result", columns);
        result.from(json);
    }

    /**
     * 获取所有PluginService的bean
     * @return
     */
    public Map<String, PluginService> getPluginServices() {
        Map<String, PluginService> pluginServices = context.getBeansOfType(PluginService.class);
        return pluginServices;
    }

}
