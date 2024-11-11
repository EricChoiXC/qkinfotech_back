package com.qkinfotech.core.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.mvc.util.QueryBuilder;
import com.qkinfotech.core.tendering.AssemblyEnums;
import com.qkinfotech.core.tendering.interfaceConfig.InterfaceLog;
import com.qkinfotech.core.tendering.interfaceConfig.model.RequestLog;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMainHistory;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMeetingNotice;
import com.qkinfotech.core.tendering.vo.AppsCalendarVo;
import com.qkinfotech.util.CookieUtil;
import com.qkinfotech.util.DateUtils;
import com.qkinfotech.util.SqlUtil;
import com.qkinfotech.util.xml.XmlToJsonConverter;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.aspectj.util.FileUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.qkinfotech.core.app.config.EkpConfig;
import com.qkinfotech.core.web.model.KmReviewParamterForm;
import com.qkinfotech.util.StringUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@CrossOrigin("http://localhost:3000")
@Slf4j
public class WebController {
	
	@Autowired
	private EkpConfig ekpConfig;

	@Autowired
	private SimpleService<AppsProjectMeetingNotice> appsProjectMeetingNoticeService;

	@Autowired
	protected SimpleService<InterfaceLog> interfaceLogService;

	@Autowired
	private SimpleService<RequestLog> requestLogService;

	@RequestMapping("/web/updateReviewInfo")
	@ResponseBody
	public String updateReviewInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		KmReviewParamterForm form = new KmReviewParamterForm();
		String url = ekpConfig.getEkpUrl() + "/api/km-review/kmReviewRestService/updateReviewInfo";
		return post(url, ekpConfig.getEkpCookie(), form.toString());
	}

	@RequestMapping("/web/approveProcessNode")
	@ResponseBody
	public String approveProcessNode(HttpServletRequest request, HttpServletResponse response) throws Exception {
		com.alibaba.fastjson2.JSONObject reqJson = getPostData(request);
		JSONObject json = new JSONObject();
		json.put("handler", reqJson.getString("handler"));
		json.put("ApplicationNo", reqJson.getString("reviewId"));
		json.put("handleInfo", reqJson.getString("handleInfo"));
		String url = ekpConfig.getEkpUrl() + "/api/km-review/kmReviewRestService/approveProcessNode";
		return post(url, ekpConfig.getEkpCookie(), json.toString());
	}

	/**
	 * 推送ekp生产流程，formValue内容： docSubject:标题；creatorId:流程创建人id；formValues:表单信息
	 */
	@RequestMapping("/web/addReview")
	@ResponseBody
	public com.alibaba.fastjson2.JSONObject addReview(@RequestBody com.alibaba.fastjson2.JSONObject formValue, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String fdTemplateId = formValue.getString("fdTemplateId");
		if (formValue.containsKey("fdTemplateKey") && StringUtil.isNotNull(formValue.getString("fdTemplateKey"))) {
			String fdTemplateKey = formValue.getString("fdTemplateKey");
			if ("ekpProjectApprovalTemplateId".equals(fdTemplateKey)) {
				if (StringUtil.isNotNull(ekpConfig.getEkpProjectApprovalTemplateId())) {
					fdTemplateId = ekpConfig.getEkpProjectApprovalTemplateId();
				}
			}
			if ("ekpIsoApprovalTemplateId".equals(fdTemplateKey)) {
				if (StringUtil.isNotNull(ekpConfig.getEkpIsoApprovalTemplateId())) {
					fdTemplateId = ekpConfig.getEkpIsoApprovalTemplateId();
				}
			}
		}

		String content =
				"<docSubject>"+formValue.getString("docSubject")+"</docSubject>" +
				"<fdTemplateId>"+fdTemplateId+"</fdTemplateId>" +
				"<formValues>"+formValue.get("formValues").toString()+"</formValues>" +
				"<docStatus>20</docStatus>" +
				"<docCreator>{Id:\""+formValue.getString("creatorId")+"\"}</docCreator>";

		String result = soapuiWebService(ekpConfig.getEkpUrl() + "/sys/webservice/kmReviewWebserviceService?wsdl", createSoapContent(content, "addReview"));
		com.alibaba.fastjson2.JSONObject resultJson = new com.alibaba.fastjson2.JSONObject();
		resultJson.put("result", JSON.parseObject(result));
		//日志留存
		InterfaceLog log= new InterfaceLog();
		log.setfCreateTime(new Date());
		log.setfInterfaceUrl(ekpConfig.getEkpUrl() + "/sys/webservice/kmReviewWebserviceService?wsdl");
		log.setfInterfaceName("ekp发起流程");
//
		if(StringUtil.isNotNull(result)){
			log.setfInterfaceStatus("1");
		}else {
			log.setfInterfaceStatus("2");
		}
		log.setfInputParameter(content);
		log.setfInterfaceInfo(result);
		interfaceLogService.save(log);
		return resultJson;
	}

	@RequestMapping("/web/approveProcess")
	@ResponseBody
	public com.alibaba.fastjson2.JSONObject approveProcess(@RequestBody com.alibaba.fastjson2.JSONObject json, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String content =
				"<fdId>" + json.getString("reviewId") + "</fdId>" +
						"<flowParam> " + json.getString("formParam") + "</flowParam>";
		String result = soapuiWebService(ekpConfig.getEkpUrl() + "/sys/webservice/kmReviewWebserviceService?wsdl", createSoapContent(content, "approveProcess"));
		com.alibaba.fastjson2.JSONObject resultJson = new com.alibaba.fastjson2.JSONObject();
		resultJson.put("result", JSON.parseObject(result));
		return resultJson;
	}

	/**
	 * 日程
	 * @param fId 人员id
	 */
	@RequestMapping("/web/getCalendar")
	@ResponseBody
	public String getCalendar(HttpServletRequest request, HttpServletResponse response, @RequestParam String fId) throws Exception {
		String url = ekpConfig.getEkpUrl() + "/api/km-calendar/kmCalendarRestService/listCalendar";
		JSONObject json = new JSONObject();
		// 获取当前月份的第一天
		LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
		// 创建第一天的最开始时间
		LocalDateTime firstMoment = LocalDateTime.of(firstDay, java.time.LocalTime.MIN);
		// 获取当前月份的最后一天
		LocalDate lastDay = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
		// 创建最后一天的最后时间
		LocalDateTime lastMoment = LocalDateTime.of(lastDay, java.time.LocalTime.MAX);
		// 定义日期时间格式
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		// 格式化为字符串
		String docStartTime = firstMoment.format(formatter);
		String docFinishTime = lastMoment.format(formatter);

		//登录人id
		String persons = "{'Id':'" + fId + "'}";

		//appKey	日程或笔记来源	字符串(String)	允许为空	标识日程或笔记来源的系统
//		String appKey="";

		//fdType	查询类型	字符串(String)	允许为空	"note":查询笔记 "event":查询日程 为空查询所有
		String fdType="event";

//		json.put("appKey", appKey);
		json.put("fdType", fdType);
		//日程人员
		json.put("persons", persons);
		//开始时间
		json.put("docStartTime", docStartTime);
		//结束时间
		json.put("docFinishTime", docFinishTime);

		String post = post(url, ekpConfig.getEkpCookie(), json.toString());

		JSONArray dateJsonArray = new JSONArray();
		if(StringUtil.isNotNull(post)){
			com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSON.parseObject(post);
			if ("1".equals(jsonObject.getString("returnState"))){
				//请求成功
				JSONArray dataJson = jsonObject.getJSONObject("message").getJSONArray("datas");
				Map<String,List<AppsCalendarVo>> resultMap = new HashMap<>();
				if(null != dataJson){
					dataJson.forEach(data -> {
						com.alibaba.fastjson2.JSONObject jsonData = com.alibaba.fastjson2.JSON.parseObject(data.toString());
						//开始时间
						String startTime = jsonData.getString("docStartTime");
						//结束时间
						String endTime = jsonData.getString("docFinishTime");
						//根据开始时间和结束时间判定该日程持续几天
						List<String> dateList = DateUtils.getAllDatesBetween(startTime,endTime);
						//设置map数据
						setDateMap(resultMap,dateList,jsonData);
					});
					//遍历Map数据
					Set<String> calendarSet = resultMap.keySet();
					if(!calendarSet.isEmpty()){
						calendarSet.forEach(dateKey -> {
							List<AppsCalendarVo> calendarList = resultMap.get(dateKey).stream()
									.sorted(Comparator.comparing(AppsCalendarVo::getDate)) // 升序排序
									.toList();
							com.alibaba.fastjson2.JSONObject dateJson = new com.alibaba.fastjson2.JSONObject();
							dateJson.put("date",dateKey);
							dateJson.put("list",calendarList);
							dateJsonArray.add(dateJson);
						});
					}
				}
			}
		}
		com.alibaba.fastjson2.JSONObject resultJson = new com.alibaba.fastjson2.JSONObject();
		resultJson.put("list",dateJsonArray);
		resultJson.put("host",ekpConfig.getEkpUrl());
		resultJson.put("reqUrl","/km/calendar/");
		return resultJson.toJSONString();
	}

	/**
	 * 设置Map数据
	 * @param resultMap
	 * @param dateList
	 * @param jsonData
	 */
	private void setDateMap(Map<String,List<AppsCalendarVo>> resultMap,List<String> dateList,com.alibaba.fastjson2.JSONObject jsonData){
		for (int i = 0; i < dateList.size(); i++) {
			String key = dateList.get(i);
			List<AppsCalendarVo> list = new ArrayList<>();
			if(resultMap.containsKey(key)){
				list = resultMap.get(key);
			}
			AppsCalendarVo calendarVo = new AppsCalendarVo();
			calendarVo.setfId(jsonData.getString("fdId"));
			calendarVo.setSubject(jsonData.getString("docSubject"));
			String dateString = key + " 00:00";
			if(i == 0){
				dateString = jsonData.getString("docStartTime");

			}else if (i == dateList.size() - 1){
				dateString = jsonData.getString("docFinishTime");
			}
			try {
				calendarVo.setDate(DateUtils.parseDate(dateString,DateUtils.YYYY_MM_DD_HH_MM));
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			calendarVo.setFdAppKey(jsonData.getString("fdAppKey"));
			calendarVo.setFdAppUUId(jsonData.getString("fdAppUUId"));
			list.add(calendarVo);

			resultMap.put(key,list);
		}
	}

	/**
	 * 待办
	 * @param fId 人员id
	 */
	@RequestMapping("/web/todoWebService")
	@ResponseBody
	public String getTodoWebService(HttpServletRequest request, HttpServletResponse response, @RequestParam String fId) throws Exception {
		String url = ekpConfig.getEkpUrl() + "/api/sys-notify/sysNotifyTodoRestService/getTodo";
		String targets = "{'Id':'" + fId + "'}";
//		如果没有传入任何查询条件时，此参数为必填
		JSONObject json = new JSONObject();
		//待办人员
		json.put("targets", targets);
		json.put("type", 0);
		String result = post(url, ekpConfig.getEkpCookie(), json.toString());
		if(StringUtil.isNotNull(result)){
			com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSON.parseObject(result);
			if ("2".equals(jsonObject.getString("returnState"))){
				//请求成功
				com.alibaba.fastjson2.JSONObject dataJson = jsonObject.getJSONObject("message");
				return dataJson.toJSONString();
			}
		}
		return "";
	}

	public String post(String url, String auth, String body) {
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		String result = "";

		RequestLog requestLog = new RequestLog();
		requestLog.setfUrl(url);
		requestLog.setfCreateTime(new Date());
		requestLog.setfRequest(body);
		try {
			client = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(url);
			
			httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
			if (StringUtil.isNotNull(auth)) {
				httpPost.setHeader("Authorization", auth);
			}
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build();
			httpPost.setConfig(requestConfig);
			
			httpPost.setEntity(new StringEntity(body.toString()));
			
			response = client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				result = EntityUtils.toString(entity, "UTF-8");
			}
			requestLog.setfStatus(String.valueOf(response.getStatusLine().getStatusCode()));
			requestLog.setfResponse(result);
			
		} catch (Exception e) {
			requestLog.setfStatus("failure");
			e.printStackTrace();
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// requestLogService.save(requestLog);
		return result;
	}

	/**
	 * 根据拼接 xml 字符串
	 * @return
	 */
	public static String createSoapContent(String content, String methodName) {

		return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://webservice.review.km.kmss.landray.com/\">\n" +
				"   <soapenv:Header/>\n" +
				"   <soapenv:Body>\n" +
				"      <web:"+methodName+">\n" +
				"         <arg0>\n" +
				content +
				"         </arg0>\n" +
				"      </web:"+methodName+">\n" +
				"   </soapenv:Body>\n" +
				"</soapenv:Envelope>";
	}

	private String soapuiWebService(String wsdl, String soapRequestData) throws Exception {
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
			System.out.println("调用成功！");
			String soapResponseData = postMethod.getResponseBodyAsString();
			System.out.println(soapResponseData);

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
				JSONObject result = new JSONObject();
				result.put("success", true);
				result.put("message", item.get("return"));
				return result.toString();
			}

		} else {
			JSONObject result = new JSONObject();
			result.put("success", false);
			result.put("message", postMethod.getResponseBodyAsString());
			requestLogService.save(requestLog);
			return result.toString();
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


	@RequestMapping("/web/getReviewNo")
	@ResponseBody
	public String getReviewNo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		com.alibaba.fastjson2.JSONObject body = getPostData(request);
		String reviewId = body.getString("reviewId");
		if (StringUtil.isNull(reviewId)) {
			return null;
		}
		String reviewNo = null;
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = DriverManager.getConnection(
					ekpConfig.getEkpDatabaseUrl(),
					ekpConfig.getEkpDatabaseUsername(),
					ekpConfig.getEkpDatabasePassword());
			statement = conn.prepareStatement("select fd_number from km_review_main where fd_id = ? ");
			statement.setString(1, reviewId);
			rs = statement.executeQuery();

			ResultSetMetaData data = rs.getMetaData();
			int columnNum = data.getColumnCount();

			while(rs.next()) {
				reviewNo = rs.getObject("fd_number").toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (Objects.nonNull(rs)) {
				rs.close();
			}
			if (Objects.nonNull(statement)){
				statement.close();
			}
			if (Objects.nonNull(conn)) {
				conn.close();
			}
		}
		return reviewNo;
	}

	private com.alibaba.fastjson2.JSONObject getPostData(HttpServletRequest request) {
		if (!"POST".equals(request.getMethod())) {
			return new com.alibaba.fastjson2.JSONObject();
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
				return new com.alibaba.fastjson2.JSONObject();
			}
			return com.alibaba.fastjson2.JSONObject.parseObject(txt);

		} catch (IOException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	public String postCookie(String url, String cookie, String body) {
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		String result = "[]";
		try {
			client = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(url);

			httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
			if (StringUtil.isNotNull(cookie)) {
				String cookieData = "LRToken=" + cookie;
				httpPost.addHeader("Cookie", cookieData);
			}
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build();
			httpPost.setConfig(requestConfig);

			httpPost.setEntity(new StringEntity(body.toString()));

			response = client.execute(httpPost);
			HttpEntity entity = response.getEntity();

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				result = EntityUtils.toString(entity, "UTF-8").replace("&amp;","&");
			} else {
				logger.warn("url:" + url);
				logger.warn("body:" + body);
				Header[] headers = httpPost.getAllHeaders();
				for (Header header : headers) {
					logger.warn(header.getName() + ":" + header.getValue());
				}
				logger.warn("status code:" + response.getStatusLine().getStatusCode());
				logger.warn("result:" + EntityUtils.toString(entity, "UTF-8"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * 组件-统一获取数据接口
	 */
	@RequestMapping("/web/assemblyProcess")
	@ResponseBody
	public String getEkpInterfaceData(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//判断获取的是哪个组件的数据
		String key = request.getParameter("key");
		String userId = request.getParameter("userId");
		com.alibaba.fastjson2.JSONObject body = getPostData(request);
		if (body.containsKey("key")) {
			key = body.getString("key");
		}
		if (body.containsKey("userId")) {
			userId = body.getString("userId");
		}
		AssemblyEnums assembly = AssemblyEnums.getAssembly(key);
		com.alibaba.fastjson2.JSONObject result = new com.alibaba.fastjson2.JSONObject();
		if(null != assembly){
			if(assembly.getKey().equals(AssemblyEnums.TODO.getKey())){
				//待办
				result.put("host",ekpConfig.getEkpUrl());
				result.put("url",assembly.getReqUrl());
				result.put("pagekey",assembly.getPageKey());
				//获取数据列表
				String url = ekpConfig.getEkpUrl() + assembly.getListUrl();
				String targets = "{'Id':'" + userId + "'}";
				JSONObject json = new JSONObject();
				//待办人员
				json.put("targets", targets);
				json.put("type", 1);
				String resultString = post(url, ekpConfig.getEkpCookie(), json.toString());
				if(StringUtil.isNotNull(resultString)){
					com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSON.parseObject(resultString);
					if ("2".equals(jsonObject.getString("returnState"))){
						//请求成功
						com.alibaba.fastjson2.JSONObject dataJson = jsonObject.getJSONObject("message");
						result.put("data", dataJson.getJSONArray("docs"));
					}else{
						result.put("data", "");
					}
				}
			}else if ((assembly.getKey().equals(AssemblyEnums.PROJECT_MEETING_REMINDER.getKey()))) {
				//项目日程提醒
				result.put("host","");
				result.put("url","");
				result.put("pagekey",assembly.getPageKey());
				com.alibaba.fastjson2.JSONObject queryJson = new com.alibaba.fastjson2.JSONObject();
				//构建参数
				SqlUtil.setParameter(queryJson,"fMeetingDate",DateUtils.getDate(),"query","eq");
				SqlUtil.setParameter(queryJson,"fUser.fId",userId,"query","eq");
				List<AppsProjectMeetingNotice> dataList = appsProjectMeetingNoticeService.findAll(JSONQuerySpecification.getSpecification(queryJson));
				result.put("data",dataList);
			}else{
				String lrtToken = CookieUtil.getCookie(request,"LRToken");
				result.put("host",ekpConfig.getEkpUrl());
				result.put("url",assembly.getReqUrl());
				result.put("pagekey",assembly.getPageKey());
				if(StringUtil.isNotNull(lrtToken)){
					String url = ekpConfig.getEkpUrl() + assembly.getListUrl();
					String post = postCookie(url, lrtToken,"");
					if(assembly.getReturnType().equals("xml")){
						result.put("data",XmlToJsonConverter.getXmlToJsoArray(post));
					}else if (assembly.getReturnType().equals("json")){
						// 正则表达式匹配 "onclick" 属性及其值
						String regex = ",?\\s*\"onclick\"\\s*:\\s*function\\s*\\([^)]*\\)\\s*\\{[^}]*\\}\\s*,?";
						// 使用非贪婪匹配来确保只匹配到最近的 }
						String cleanedJsonString = post.replaceAll(regex, "");
						result.put("data", com.alibaba.fastjson2.JSONObject.parseObject(cleanedJsonString));
					}else{
						com.alibaba.fastjson2.JSONArray resultListData = com.alibaba.fastjson2.JSONArray.parseArray(post);
						result.put("data",resultListData);
					}
				}else{
					result.put("data","");
				}
			}
		}else{
			result.put("data","");
			result.put("url","");
			result.put("pageKey","");
		}
		return result.toJSONString();
	}

}
