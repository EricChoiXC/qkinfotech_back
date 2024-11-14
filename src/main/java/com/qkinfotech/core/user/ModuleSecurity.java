package com.qkinfotech.core.user;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.qkinfotech.core.org.model.OrgElement;
import com.qkinfotech.core.org.model.OrgPerson;
import com.qkinfotech.core.sys.base.model.TableFirst;
import com.qkinfotech.core.sys.base.model.TableSecond;
import com.qkinfotech.core.sys.log.model.SysLogChild;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.security.BaseModule;
import com.qkinfotech.core.user.model.SysAuthority;
import com.qkinfotech.core.user.model.SysUser;

@Configuration("module:core.user")
public class ModuleSecurity extends BaseModule implements ApplicationRunner {

	@Autowired
	private SysUserService sysUserService;

	@Autowired
	private SimpleService<SysAuthority> sysAuthorityService;

	@Autowired
	private SimpleService<SysLogChild> sysLogChildService;

	@Autowired
	private SimpleService<OrgPerson> orgPersonService;

	@Autowired
	private SimpleService<OrgElement> orgElementService;

	@Autowired
	private SimpleService<TableFirst> tableFirstService;

	@Autowired
	private SimpleService<TableSecond> tableSecondService;

	private SysAuthority addAuthority(SysAuthority role) {
		if (sysAuthorityService.getById(role.getfId()) == null) {
			sysAuthorityService.save(role);
		}
		return sysAuthorityService.getById(role.getfId());
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		super.run(args);

		SysAuthority authAdmin = new SysAuthority(Strings.repeat("0", 32), "AUTH_ADMIN", "system", "common");
		authAdmin = addAuthority(authAdmin);

		SysAuthority authUser = new SysAuthority(Strings.repeat("1", 32), "AUTH_USER", "system", "common");
		authUser = addAuthority(authUser);

		SysUser sysUser = sysUserService.findByLoginName("admin");
		if (sysUser == null) {
			sysUser = new SysUser();
			sysUser.setfId(Strings.repeat("0", 32));
			sysUser.setfLoginName("admin");
			sysUser.setfAlias("admin");
			sysUser.setfPassword("admin");
			Set<SysAuthority> authorities = new HashSet<>();
			authorities.add(authAdmin);
			authorities.add(authUser);
			sysUser.setfAuthorities(authorities);
			sysUserService.save(sysUser);
		} else {
			
//			sysUser = sysUserService.getById(Strings.repeat("0", 32));
//			Set<SysAuthority> authorities = new HashSet<>();
//			authorities.add(authAdmin);
//			authorities.add(authUser);
//			sysUser.setfAuthorities(authorities);
//			sysUserService.save(sysUser);
			
//			SysAuthority auth = sysAuthorityService.getById(Strings.repeat("0", 32));
//			Set<SysUser> users = new HashSet<>();
//			users.add(sysUserService.getById(Strings.repeat("0", 32)));
//			auth.setfUsers(users);
//			sysAuthorityService.save(auth);
		}

		System.out.println(" =======================  select sysLogChild  ==============================");
		List<SysLogChild> childs = sysLogChildService.findAll();
		System.out.println(" =======================  select sysLogChild end  ==============================");

		Date now = new Date();
		SysLogChild child = new SysLogChild();
		child.setfChild("child");
		child.setfMain("main");
		child.setfDate(now);
		child.setfId(String.valueOf(now.getTime()));
		System.out.println(" =======================  save sysLogChild  ==============================");
		sysLogChildService.save(child);
		System.out.println(" =======================  save sysLogChild end  ==============================");

		if (!childs.isEmpty()) {
			System.out.println(" =======================  delete sysLogChild  ==============================");
			sysLogChildService.delete(childs.get(0));
			System.out.println(" =======================  delete sysLogChild end  ==============================");
		}


		System.out.println("========================== create tableFirst, tableSecond ==================================");

		TableFirst tableFirst = new TableFirst();
		tableFirst.setfId(String.valueOf(now.getTime()));
		tableFirst.setfName(String.valueOf(now.getTime()));
		tableFirstService.save(tableFirst);
		TableSecond tableSecond = new TableSecond();
		tableSecond.setfId(String.valueOf(now.getTime()));
		tableSecond.setfName(String.valueOf(now.getTime()));
		tableSecondService.save(tableSecond);

		System.out.println("========================== create finish ==================================");


	}

	@Override
	public void configAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
		requests.requestMatchers("/sys/user/save").permitAll();

		/* 未启用方法过滤 */
		requests.requestMatchers("/userController/deleteAuths").denyAll();
		requests.requestMatchers("/userController/getUser").denyAll();
		requests.requestMatchers("/userController/updateAuths").denyAll();
		requests.requestMatchers("/userController/updateRoleAuths").denyAll();
		requests.requestMatchers("/userController/updateRoleElements").denyAll();
	}

	@Override
	public void configSecurityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, UserDetailsService userDetailsService) {

	}

}
