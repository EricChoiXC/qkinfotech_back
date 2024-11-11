package com.qkinfotech.core.auth.token;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.*;

import com.qkinfotech.core.app.config.EkpConfig;
import com.qkinfotech.core.auth.login.LoginUser;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.tendering.interfaceConfig.model.RequestLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.util.DESUtil;
import com.qkinfotech.util.StringUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class EkpToken implements IToken {

	@Autowired
	private EkpConfig ekpConfig;

	@Autowired
	private SimpleService<RequestLog> requestLogService;
	
	public static final String REQUEST_PARAMETER_NAME = "LRToken";

	public static final String HEADER_NAME = "LRToken";

	protected long expire = 0;
	
	private String tokenType = "LRToken";

	public void setTokenType (String tokenType) {
		this.tokenType = tokenType;
	}
	
	private Map<String, String> tokenMap = new HashMap<String, String>();
	
	public void putMap(String key, String value) {
		this.tokenMap.put(key, value);
	}
	
	public void removeMap(String key) {
		this.tokenMap.remove(key);
	}
	
	public String getMap(String key) {
		return this.tokenMap.get(key);
	}

	private String getTokenFromCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for(int i =0; i < cookies.length; ++ i) {
				if(cookies[i].getName().equals(tokenType)) {
					return cookies[i].getValue();
				}
			}
		}
		return null;
	}
	
	@Override
	public boolean validate(HttpServletRequest request) {
		Authentication authentication = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();
		String tokenInSession = (String)request.getSession().getAttribute(tokenType);
		String tokenInRequest = getTokenString(request);
		String userName = "";
		try {
			String token = getTokenString(request, tokenType);
			Cookie[] cookies = request.getCookies();
			for (Cookie cookie : cookies) {
				if ("id".equals(cookie.getName())) {
					userName = URLDecoder.decode(cookie.getValue());
				}
			}
//			if (StringUtil.isNotNull(ssoToken)) {
//				String tokenInfo = DESUtil.decrypt(ssoToken);
//				String userName = tokenInfo.substring(14);
//				putMap(ssoToken, userName);
//			}
			/*if (!tokenMap.containsKey(token) && StringUtil.isNull(userName)) {
				JSONObject json = null;
				try {
					json = this.soapuiWebService(ekpConfig.getEkpUrl() + "/sys/webservice/loginWebserviceService?wsdl", createSoapContent(token));
					if (json != null) {
						if (!json.isEmpty()) {
							if (json.containsKey("result") && "true".equals(json.getString("result"))) {
								userName = json.getString("loginName");
								tokenMap.put(token, userName);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (StringUtil.isNull(tokenInRequest)) {
			return true;
		}
		if (authentication == null && StringUtil.isNotNull(tokenInRequest)) {
			logger.info(" false : authentication == null && StringUtil.isNotNull(tokenInRequest) ");
			return false;
		}
		if (authentication != null && StringUtil.isNull(userName)) {
			logger.info(" false : authentication != null && StringUtil.isNull(userName) ");
			return false;
		}
		if (authentication != null && StringUtil.isNotNull(userName) && StringUtil.isNotNull(tokenInRequest) && StringUtil.isNotNull(tokenInSession)) {
			if (tokenInRequest.equals(tokenInSession)) {
				LoginUser loginUser = (LoginUser) authentication.getPrincipal();
				if (userName.equals(loginUser.getFLoginUserId())) {
					return true;
				} else {
					logger.info("false : userName ne loginUser.fLoginUserId");
				}
			} else {
				logger.info(" false : cookie token ne storage token ");
			}
			return false;
		}
		if (StringUtil.isNull(userName) && StringUtil.isNull(tokenInRequest)) {
			return true;
		}
		if(StringUtil.isNull(tokenInSession) && StringUtil.isNotNull(tokenInRequest)) {
			logger.info(" false : StringUtil.isNull(tokenInSession) && StringUtil.isNotNull(tokenInRequest) ");
			return false;
		}
		if(StringUtil.isNotNull(tokenInSession) && StringUtil.isNull(tokenInRequest)) {
			request.getSession().removeAttribute(tokenType);
			return true;
		}
		if(StringUtil.isNull(tokenInSession) && StringUtil.isNotNull(tokenInRequest)) {
			logger.info(" false : StringUtil.isNull(tokenInSession) && StringUtil.isNotNull(tokenInRequest) ");
//			request.getSession().setAttribute(tokenType, tokenInRequest);
			return false;
		}
		return tokenInSession.equals(tokenInRequest);
	}
	
	@Override
	public String getUsername(String tokenString) {
		if (tokenMap.containsKey(tokenString) && tokenMap.get(tokenString).equals("admin")) {
			return "admin";
		}
		try {
			JSONObject json = null;
			json = this.soapuiWebService(ekpConfig.getEkpUrl() + "/sys/webservice/loginWebserviceService?wsdl", createSoapContent(tokenString));
			if (json != null) {
				if (!json.isEmpty()) {
					if (json.containsKey("result") && "true".equals(json.getString("result"))) {
						return json.getString("loginName");
					}
				}
			}
		}catch(Exception e) {
			throw new BadCredentialsException("Invalid token", e);
		}
		return null;
	}

	@Override
	public String getTokenString(HttpServletRequest request) {
		String token = request.getParameter(REQUEST_PARAMETER_NAME);
		if(token == null) {
			token = request.getHeader(HEADER_NAME);
		}
		if(token == null) {
			token = getTokenFromCookie(request);
		}
		return token;
	}

	public String getTokenString(HttpServletRequest request, String cookieName) {
		String token = request.getParameter(cookieName);
		if(token == null) {
			token = request.getHeader(cookieName);
		}
		if(token == null) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for(int i =0; i < cookies.length; ++ i) {
					if(cookies[i].getName().equals(cookieName)) {
						return cookies[i].getValue();
					}
				}
			}
		}
		return token;
	}

	@Override
	public void setTokenString(HttpServletRequest request, HttpServletResponse response, String tokenString) {
		request.getSession().setAttribute(tokenType, tokenString);
		String t = getTokenFromCookie(request);
		if(t == null || !t.equals(tokenString)) {
			response.addCookie(new Cookie(tokenType, tokenString));
		}
	}

	@Override
	public String generateTokenString(String username, long expire) {
		if (username == null) {
			return null;
		}
		JSONObject o = new JSONObject();
		o.put("username", username);
		o.put("expire", expire);
		o.put("sault", Math.random());
		return DESUtil.encrypt(o.toString());
	}




	/**
	 * 根据拼接 xml 字符串
	 * @return
	 */
	public static String createSoapContent(String tokenStr) {

		return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sso=\"http://sso.authentication.sys.kmss.landray.com/\">\n" +
				"   <soapenv:Header/>\n" +
				"   <soapenv:Body>\n" +
				"      <sso:getTokenLoginName>\n" +
				"         <arg0>" + tokenStr + "</arg0>\n" +
				"      </sso:getTokenLoginName>\n" +
				"   </soapenv:Body>\n" +
				"</soapenv:Envelope>";
	}

	private JSONObject soapuiWebService(String wsdl, String soapRequestData) throws Exception {
		JSONObject jsonObject = new JSONObject();
		PostMethod postMethod = new PostMethod(wsdl);
		// 然后把Soap请求数据添加到PostMethod中
		byte[] b = soapRequestData.getBytes("utf-8");
		InputStream is = new ByteArrayInputStream(b, 0, b.length);
		org.apache.commons.httpclient.methods.RequestEntity re = new InputStreamRequestEntity(is, b.length, "application/soap+xml; charset=utf-8");
		postMethod.setRequestEntity(re);
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

			Document doc = DocumentHelper.parseText(soapResponseData);//报文转成doc对象
			Element root = doc.getRootElement();//获取根元素，准备递归解析这个XML树
			Map<String, String> map = new HashMap<String, String>();
			List<Map<String, String>> lists = new ArrayList<Map<String, String>>();//存放叶子节点数据
			//获取叶子节点的方法
			getCode(root, map, lists);
			//循环叶子节点数据
			for(Map<String, String> item : lists) {
				//取响应报文中的Json
				for (String key : item.keySet()) {
					jsonObject.put(key, item.get(key));
				}
			}
		}
		requestLogService.save(requestLog);
		return jsonObject;
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

}
