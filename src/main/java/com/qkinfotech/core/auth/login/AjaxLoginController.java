package com.qkinfotech.core.auth.login;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.app.config.EkpConfig;
import com.qkinfotech.core.auth.token.EkpToken;
import com.qkinfotech.core.auth.util.ApiRsaUtil;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.QueryBuilder;
import com.qkinfotech.core.tendering.interfaceConfig.InterfaceLog;
import com.qkinfotech.core.tendering.interfaceConfig.model.RequestLog;
import com.qkinfotech.core.user.model.SysUser;
import com.qkinfotech.util.DESUtil;
import com.qkinfotech.util.StringUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.aspectj.util.FileUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName PMSController
 * @Description //TODO
 * @Author JiangWei
 * @Date 2024/5/30 14:58
 * @Version 1.0
 */
@Slf4j
@RestController
public class AjaxLoginController {
	
	@Autowired
	private EkpConfig ekpConfig;
	
    @Autowired
    private SimpleService<SysUser> sysUserService;

    @Autowired
    private SimpleService<InterfaceLog> interfaceLogService;
	
	@Autowired
	private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private SimpleService<RequestLog> requestLogService;

    private SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
	
	private String ekpHost = "http://oa.test.com/ekp";

    @PostMapping("/pms/ajaxLogin")
    public Map<String, Object> ajaxLogin(@RequestBody Map<String, String> request, HttpServletRequest request2, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        String desStr = request.get("desStr");
        String nameStrength = request.get("nameStrength");
        String loginInfo = DESUtil.decrypt(Integer.valueOf(nameStrength), "12345678", desStr);
        //String loginName = request.get("loginName");
        //String password = request.get("password");
        String loginName = loginInfo.substring(0, Integer.valueOf(nameStrength));
        String password = loginInfo.substring(Integer.valueOf(nameStrength));
        String errorMsg = "";

        /* 1.生成pm的token */
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String tokenInfo = df.format(new Date());
        tokenInfo += loginName;
        String pmToken = DESUtil.encrypt(tokenInfo);
        JSONObject qbjson = new JSONObject();
        JSONObject query = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject param = new JSONObject();
        param.put("fLoginName", loginName);
        query.put("eq", param);
        and.add(query);

        JSONObject param2 = new JSONObject();
        JSONObject query2 = new JSONObject();
        param2.put("fDisabled", false);
        query2.put("eq", param2);
        and.add(query2);

        qbjson.put("query", and);
        QueryBuilder<SysUser> qb = QueryBuilder.parse(SysUser.class, qbjson);
        SysUser user = sysUserService.findOne(qb.specification());
        if (user == null) {
            result.put("success", false);
            result.put("msg", "用户不存在");
            return result;
        } else {
            result.put("id", user.getfId());
            result.put("username", URLEncoder.encode(user.getfLoginName()));
        }

        /* 管理员单独进行登陆处理，不参与ekp单点登陆 */
        if ("admin".equals(loginName)) {
            JSONObject json = new JSONObject();
            if (user.getfPassword().equals(password)) {
                json.put("tokenString", pmToken);
                result.put("success", true);
                result.put("LRToken", json.toString());
                result.put("id", user.getfId());
                result.put("username", URLEncoder.encode(user.getfLoginName()));
                // result.put("ssoToken", pmToken);
                doLogin(request2, response, loginName, pmToken);
            } else {
                result.put("success", false);
                result.put("msg", "用户名或密码错误");
            }
            return result;
        }
        
        if (StringUtil.isNull(ekpConfig.getEkpUrl())) {
        	ekpConfig.setEkpUrl(ekpHost);
        }

        /* 非admin，由于pm数据库不同步用户密码，因此需要先进行ekp的lrtoken获取，成功才能进行登陆 */
        JSONObject json = null;
        try {
            json = this.soapuiWebService(ekpConfig.getEkpUrl() + "/sys/webservice/sysExtLoginWebServiceImp?wsdl", createSoapContent(desStr,nameStrength));
        } catch(Exception e) {
            errorMsg = e.getMessage();
            e.printStackTrace();
        }

        if (json != null) {
            if(!json.isEmpty() && json.getBoolean("success")) {
                String token = json.getString("token");
                JSONObject tokenJson = JSONObject.parseObject(token);
                result.put("success", true);
                result.put("LRToken", token);
                result.put("id", user.getfId());
                result.put("username", URLEncoder.encode(user.getfLoginName()));
                // result.put("ssoToken", pmToken);
                doLogin(request2, response, loginName, tokenJson.getString("tokenString"));
            } else {
                result.put("success", false);
                result.put("msg", json.getString("msg"));
                if(json.containsKey("error")){
                    result.put("error", json.getString("error"));
                }
            }
        } else {
            result.put("success", false);
            result.put("msg", "服务器错误:"+errorMsg);
        }
        return result;
    }

    /**
     * 跳转内网页面前的请求编码
     */
    @RequestMapping("/pms/oaQuest")
    public String oaQuest(@RequestBody Map<String, String> requestMap, HttpServletRequest request, HttpServletResponse response) {
        JSONObject result = new JSONObject();

        String id = requestMap.get("id");
        String url = requestMap.get("url");
        SysUser user = sysUserService.getById(id);
        if (user != null) {
            /* OA单点登陆 */
            try {
                JSONObject oaJson = new JSONObject();
                oaJson.put("s", user.getfId());
                oaJson.put("u", url);
                oaJson.put("t", System.currentTimeMillis());
                String oaQuest = URLEncoder.encode(ApiRsaUtil.encrypt(oaJson.toString()), "UTF-8");
                result.put("oaQuest", oaQuest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    /**
     * 加密指定报文
     */
    @RequestMapping("/pms/oaQuestMap")
    public String oaQuestMap(@RequestBody JSONObject requestMap, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject result = new JSONObject();
        requestMap.put("t", System.currentTimeMillis());
        if (!requestMap.containsKey("s")) {
            String fId = getFidFromCookies(request);
            if (StringUtil.isNotNull(fId)) {
                requestMap.put("s", fId);
            }
        }
        String oaQuest = ApiRsaUtil.encrypt(requestMap.toString());
        if (requestMap.containsKey("doEncode") && requestMap.get("doEncode").equals(Boolean.TRUE.booleanValue())) {
            oaQuest = URLEncoder.encode(oaQuest, "UTF-8");
        }
        result.put("oaQuest", oaQuest);
        return result.toString();
    }

    @RequestMapping("/pms/oaResponse")
    public void oaResponse(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject json = getPostData(request);
        String requestStr  = json.getString("str");
        InterfaceLog log = new InterfaceLog();
        try {
            requestStr = requestStr.replaceAll(" ", "+");
            log.setfCreateTime(new Date());
            JSONObject result = new JSONObject();
            String res = ApiRsaUtil.decrypt(URLDecoder.decode(requestStr, "UTF-8"));
            log.setfInputParameter(res);
            log.setfInterfaceName("单点登录");
            log.setfInterfaceStatus("1");
            interfaceLogService.save(log);
            response.getWriter().write(res);
        } catch (Exception e) {
            JSONObject resultJson = new JSONObject();
            resultJson.put("s", false);
            resultJson.put("msg", e.getMessage());
            log.setfInterfaceStatus("2");
            log.setfInterfaceInfo(e.getMessage());
            interfaceLogService.save(log);
            response.getWriter().write(JSONObject.toJSONString(resultJson));
        }
    }

    @RequestMapping("/pms/loginOut")
    public void ajaxLogout(HttpSession session)throws Exception {
        session.invalidate();
    }

    public String getFidFromCookies (HttpServletRequest request) {
        String fId = "";
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if ("id".equals(cookie.getName())) {
                fId = cookie.getValue();
            }
        }
        return fId;
    }

    /**
     * 根据拼接 xml 字符串
     * @return
     */
    public static String createSoapContent(String desStr,String nameStrength) {

        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://webservice.ext.sys.kmss.landray.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:getToken>\n" +
                "         <!--Optional:-->\n" +
                "         <arg0>\n" +
                "            <!--Optional:-->\n" +
                "            <desStr>"+desStr+"</desStr>\n" +
                "            <!--Optional:-->\n" +
                "            <nameStrength>"+nameStrength+"</nameStrength>\n" +
                "         </arg0>\n" +
                "      </web:getToken>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
    }

    private JSONObject soapuiWebService(String wsdl, String soapRequestData) throws Exception {
        PostMethod postMethod = new PostMethod(wsdl);
        // 然后把Soap请求数据添加到PostMethod中
        byte[] b = soapRequestData.getBytes("utf-8");
        InputStream is = new ByteArrayInputStream(b, 0, b.length);
        org.apache.commons.httpclient.methods.RequestEntity re = new InputStreamRequestEntity(is, b.length, "application/soap+xml; charset=utf-8");
        postMethod.setRequestEntity(re);
//        postMethod.setRequestHeader("apikey",key);
        // 最后生成一个HttpClient对象，并发出postMethod请求
        org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();
        int statusCode = httpClient.executeMethod(postMethod);
        RequestLog requestLog = new RequestLog();
        requestLog.setfUrl(wsdl);
        requestLog.setfCreateTime(new Date());
        requestLog.setfRequest(soapRequestData);
        requestLog.setfStatus(String.valueOf(statusCode));
        if(statusCode == 200) {
            logger.info("调用成功！");
            String soapResponseData = postMethod.getResponseBodyAsString();
            logger.info(soapResponseData);

            requestLog.setfResponse(soapResponseData);
            requestLogService.save(requestLog);

            Document doc = DocumentHelper.parseText(soapResponseData);//报文转成doc对象
            Element root = doc.getRootElement();//获取根元素，准备递归解析这个XML树
            Map<String, String> map = new HashMap<String, String>();
            List<Map<String, String>> lists = new ArrayList<Map<String, String>>();//存放叶子节点数据
            //获取叶子节点的方法
            getCode(root, map, lists);
            //循环叶子节点数据
            for(Map<String, String> item : lists) {
                //取响应报文中的Json
                String returnJson = item.get("return");
                JSONObject jsonObject = JSONObject.parseObject(returnJson);
                return jsonObject;
            }

        } else {

            requestLogService.save(requestLog);
            return null;
        }
        return null;
    }

    /**
     * 找到soap的xml报文的叶子节点的数据
     * @param root
     * @param map
     * @param lists
     */
    public static void getCode(Element root, Map<String, String> map,List<Map<String, String>> lists) {
        if (root.elements() != null) {
            List<Element> list = root.elements();//如果当前跟节点有子节点，找到子节点
            for (Element e : list) {//遍历每个节点
                if (e.elements().size() > 0) {
                    getCode(e, map ,lists);//当前节点不为空的话，递归遍历子节点；
                }
                if (e.elements().size() == 0) {
                    //如果为叶子节点，那么直接把名字和值放入map
                    map.put(e.getName(), e.getTextTrim());
                    //如果数据都放进map，那就存进list
                    if (map.size() == 1){
                        Map<String, String> mapTo = new HashMap<String, String>();
                        //map全部赋值给maoTo
                        mapTo.putAll(map);
                        lists.add(mapTo);
                        //清空map
                        map.clear();
                    }
                }
            }
        }
    }


    @PostMapping("/pms/singleLogin")
    public Map<String, Object> singleLogin(@RequestBody Map<String, String> request, HttpServletRequest request2, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        String desStr = request.get("desStr");
        String id = request.get("id");
        String errorMsg = "";

        SysUser user = sysUserService.getById(id);
        if (user == null) {
            result.put("success", false);
            result.put("msg", "用户不存在");
        } else {
            result.put("id", user.getfId());
        }

        /* 1.生成pm的token */
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String tokenInfo = df.format(new Date());
        tokenInfo += user.getfLoginName();
        String pmToken = DESUtil.encrypt(tokenInfo);

        if (StringUtil.isNull(ekpConfig.getEkpUrl())) {
            ekpConfig.setEkpUrl(ekpHost);
        }

        /* 非admin，由于pm数据库不同步用户密码，因此需要先进行ekp的lrtoken获取，成功才能进行登陆 */
        JSONObject json = null;
        try {
            json = this.soapuiWebService(ekpConfig.getEkpUrl() + "/sys/webservice/sysExtLoginWebServiceImp?wsdl", createSoapContent(desStr, String.valueOf(id.length()), "getTokenSingle"));
        } catch(Exception e) {
            errorMsg = e.getMessage();
            e.printStackTrace();
        }

        if (json != null) {
            if(!json.isEmpty() && json.getBoolean("success")) {
                String token = json.getString("token");
                JSONObject tokenJson = JSONObject.parseObject(token);
                result.put("success", true);
                result.put("LRToken", token);
                result.put("username", URLEncoder.encode(user.getfLoginName()));
                // result.put("ssoToken", pmToken);
                doLogin(request2, response, user.getfLoginName(), tokenJson.getString("tokenString"));
            } else {
                result.put("success", false);
                result.put("msg", json.getString("msg"));
                if(json.containsKey("error")){
                    result.put("error", json.getString("error"));
                }
            }
        } else {
            result.put("success", false);
            result.put("msg", "服务器错误:"+errorMsg);
        }
        return result;
    }

    /**
     * 根据拼接 xml 字符串
     * @return
     */
    public static String createSoapContent(String desStr,String nameStrength, String method) {

        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://webservice.ext.sys.kmss.landray.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:"+method+">\n" +
                "         <!--Optional:-->\n" +
                "         <arg0>\n" +
                "            <!--Optional:-->\n" +
                "            <desStr>"+desStr+"</desStr>\n" +
                "            <!--Optional:-->\n" +
                "            <nameStrength>"+nameStrength+"</nameStrength>\n" +
                "         </arg0>\n" +
                "      </web:"+method+">\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
    }


    public String doLogin(HttpServletRequest request, HttpServletResponse response, String loginName, String token) {
        LoginUser userDetails = (LoginUser) userDetailsService.loadUserByUsername(loginName);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
                userDetails.getPassword(), userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);

        /*response.addCookie(new Cookie("id", userDetails.getFLoginUserId()));
        response.addCookie(new Cookie("username", user.getfLoginName()));
        response.addCookie(new Cookie("LRToken", token));*/

        request.getSession().setAttribute("LRToken", token);
        return userDetails.getFLoginUserId();
    }



    private JSONObject getPostData(HttpServletRequest request) {
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

    /*@PostMapping("/pms/checkAuthentication")*/
    public void checkAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Authentication authentication = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();
        if (authentication != null) {
            String username = authentication.getName();
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            JSONObject json = new JSONObject();
            json.put("username", URLEncoder.encode(username));
            json.put("id", loginUser.getFLoginUserId());
            response.getWriter().write(JSONObject.toJSONString(json));
            // doLogin(request, response, username, null);
        }
    }
}
