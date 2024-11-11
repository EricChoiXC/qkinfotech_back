package com.qkinfotech.core.auth;

import com.qkinfotech.core.auth.manager.CustomAuthorizationManager;
import com.qkinfotech.core.auth.manager.FileDownAuthorizationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.qkinfotech.core.auth.token.IToken;
import com.qkinfotech.core.auth.token.TokenAuthenticationFilter;
import com.qkinfotech.core.security.BaseModule;
import com.qkinfotech.core.security.CustomAuthenticationProvider;
import com.qkinfotech.util.DESUtil;
import org.springframework.security.web.header.HeaderWriterFilter;

import java.util.List;

@Configuration("module:core.auth")
public class ModuleSecurity extends BaseModule {

    @Override
    public void configAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
        requests.requestMatchers("/login", "/error", "/login?error").permitAll();
        requests.requestMatchers("/pms/**").permitAll();
        //todo 下面是具体到某个接口过滤权限的操作，具体的实现类中有说明
		requests.requestMatchers("/projectNo/applicationa/test/projectId").permitAll();
//		requests.requestMatchers("/projectNo/applicationa/test/projectId").access(new CustomAuthorizationManager());
        requests.requestMatchers("/pre/audit/announcement/getNotice").permitAll();
        requests.requestMatchers("/apps/shortcuts/**").permitAll();
        requests.requestMatchers("/apps/assembly/**)").permitAll();
        //todo 下面注释是批量过滤接口权限，只要有AUTH_ADMIN角色的就都可以正常返回，反之则返回403
//        requests.requestMatchers("/apps/assembly/**)").hasAuthority("AUTH_ADMIN");
        requests.requestMatchers("/apps/designer/main/delete").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/apps/designer/main/save").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/apps/awards/save").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/apps/awards/delete").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/apps/design/performance/save").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/apps/design/performance/delete").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/apps/designer/achievement/save").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/apps/designer/achievement/delete").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/apps/supplier/info/save").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/apps/supplier/info/delete").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/apps/supplier/invite/company/save").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/apps/supplier/invite/company/delete").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/apps/supplier/main/save").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/apps/supplier/main/delete").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/apps/supplier/package/save)").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/apps/supplier/package/delete)").hasAuthority("AUTH_APP_SUPPLIER");
        requests.requestMatchers("/supplier/main/**").hasAuthority("AUTH_APP_SUPPLIER");

        //文件下载控制
        requests.requestMatchers("/file/download").access(new FileDownAuthorizationManager());

    }

    @Override
    public void configSecurityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, UserDetailsService userDetailsService) {
        System.out.println("configSecurityFilterChain");

        http.authenticationProvider(authenticationProvider(userDetailsService));

        http.addFilterBefore(new TokenAuthenticationFilter(applicationContext.getBeansOfType(IToken.class).values(), userDetailsService), HeaderWriterFilter.class);

        super.configSecurityFilterChain(http, authenticationManager, userDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return DESUtil.encrypt(rawPassword.toString());
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encodedPassword.equals(encode(rawPassword));
            }
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        System.out.println("authenticationProvider");
        CustomAuthenticationProvider authProvider = new CustomAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    public static final List<String> AUTHORITIES = List.of(
            "AUTH_APP_OPERATION_MANAGER",//操作管理员
            "AUTH_APP_INTERNAL_EMPLOYEES",//内部员工
            "AUTH_APP_SUPPLIER",//供应商
            "AUTH_APP_OWNER",//业主
            "AUTH_APP_EXPERT",//专家
            "AUTH_APP_NEW_PROJECT_NUMBER"//新建项目编号
    );

    @Override
    public String group(){
        return "core";
    }

    @Override
    public String module(){
        return "auth";
    }

    @Override
    public List<String> authorities(){
        return AUTHORITIES;
    }


}
