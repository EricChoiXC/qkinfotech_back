package com.qkinfotech.core.auth.login;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.tendering.interfaceConfig.InterfaceLog;
import com.qkinfotech.core.user.model.SysUser;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.qkinfotech.core.mvc.SimpleResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class LoginController {

	@Autowired
	private SimpleService<SysUser> sysUserService;

	@Autowired
	private SimpleService<InterfaceLog> interfaceLogService;

	@Autowired
	HttpServletRequest request;

	@Autowired
	HttpServletResponse response;

	@Autowired
	SimpleResult result;

	@Autowired
	private UserDetailsService userDetailsService;

	private SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
	
	@GetMapping("/login")
	@ResponseBody
	public void login() throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("errmsg", getLoginErrorMessage());
		result.vue(request.getRequestURL().toString().replace(request.getRequestURI(), "") + "/login", params);
	}

	@RequestMapping("/")
	@ResponseBody
	public void home() throws Exception {
		result.vue("/sys/portal/main.vue");
	}
	
	private String getLoginErrorMessage() {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return "";
		}
		if(session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) == null) {
			return "";
		}
		if (session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) instanceof AuthenticationException) {
			return "Invalid credentials";
		}
		if (session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) instanceof Throwable exception) {
			if (!StringUtils.hasText(exception.getMessage())) {
				return "Invalid credentials";
			}
		}
		return "";
	}

	@PostMapping("/pms/doLogin")
	public Map<String, Object> doLogin() throws Exception {
		Map<String, Object> result = new HashMap<>();
		JSONObject body = getPostData();
		String loginName = body.getString("loginName");
		String password = body.getString("password");

		Specification<SysUser> spec = new Specification<SysUser>() {
			private static final long serialVersionUID = 1L;
			@Override
			public Predicate toPredicate(Root<SysUser> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate = cb.equal(root.get("fLoginName"), loginName);
				Predicate predicate2 = cb.equal(root.get("fDisabled"), false);
				return cb.and(predicate, predicate2);
			}
		};

		List<SysUser> sysUsers = sysUserService.findAll(spec);

		if (sysUsers.isEmpty()) {
			result.put("success", false);
			result.put("msg", "用户不存在");
		} else if (sysUsers.size() > 1) {
			result.put("success", false);
			result.put("msg", "存在重复用户名，请联系管理员处理");
		} else {
			SysUser sysUser = sysUsers.get(0);
			/*if (sysUser.getfPassword().equals(password)) {
				result.put("success", true);
				result.put("id", sysUser.getfId());
				doLogin(sysUser.getfLoginName(), sysUser.getfId());
			} else {
				result.put("success", false);
				result.put("msg", "用户名或密码错误");
			}*/
			result.put("success", true);
			result.put("id", sysUser.getfId());
			doLogin(sysUser.getfLoginName(), sysUser.getfId());
		}
		return result;
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

	@PostMapping("/pms/checkAuthentication")
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

	public String doLogin(String loginName, String token) {
		LoginUser userDetails = (LoginUser) userDetailsService.loadUserByUsername(loginName);

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
				userDetails.getPassword(), userDetails.getAuthorities());

		SecurityContext context = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		this.securityContextRepository.saveContext(context, request, response);

		request.getSession().setAttribute("LRToken", token);
		return userDetails.getFLoginUserId();
	}
}
