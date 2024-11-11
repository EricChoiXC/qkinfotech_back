package com.qkinfotech.core.tendering;

import com.qkinfotech.core.security.BaseModule;
import com.qkinfotech.core.tendering.apps.project.manager.AppsProjectPackageManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@Configuration("module:core.tendering")
public class ModuleSecurity extends BaseModule {

    @Override
    public void configAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
        requests.requestMatchers("/meeting/web/service/**").permitAll();
        requests.requestMatchers("/project-main/web/service/**").permitAll();

        /* 项目编号申请 */
        requests.requestMatchers("/projectNo/applicationa/save").hasAuthority("AUTH_APP_NEW_PROJECT_NUMBER");
        /* 项目详情 */
        requests.requestMatchers("/projectNo/applicationa/projectView").hasAuthority("AUTH_APP_INTERNAL_EMPLOYEES");

        /* 项目包件更新 */
        requests.requestMatchers("/apps/project/package/save").access(new AppsProjectPackageManager());
        requests.requestMatchers("/apps/project/package/delete?**").access(new AppsProjectPackageManager());

        /* 会议新建 */
        requests.requestMatchers("/pre/meeting/save").hasAuthority("AUTH_APP_INTERNAL_EMPLOYEES");
        requests.requestMatchers("/meeting/kickoff/save").hasAuthority("AUTH_APP_INTERNAL_EMPLOYEES");
        requests.requestMatchers("/report/save").hasAuthority("AUTH_APP_INTERNAL_EMPLOYEES");

        /*入围名单*/
        requests.requestMatchers("/project/Shortlist/updatePackageFinalization").hasAuthority("AUTH_APP_INTERNAL_EMPLOYEES");
        requests.requestMatchers("/project/Shortlist/company/save").hasAuthority("AUTH_APP_INTERNAL_EMPLOYEES");

        /* iso流转单 */
        requests.requestMatchers("/iso/approval/save").hasAuthority("AUTH_APP_INTERNAL_EMPLOYEES");

        /* 征集结果 */
        requests.requestMatchers("/apps/collection/result/save").hasAuthority("AUTH_APP_INTERNAL_EMPLOYEES");
        requests.requestMatchers("/appsCollectionResult/deleteOldDetail").hasAuthority("AUTH_APP_INTERNAL_EMPLOYEES");
        requests.requestMatchers("/apps/collection/result/detail/save").hasAuthority("AUTH_APP_INTERNAL_EMPLOYEES");
        requests.requestMatchers("/collection/result/packageAdd").hasAuthority("AUTH_APP_INTERNAL_EMPLOYEES");

        /* 归档 */
        requests.requestMatchers("/apps/project/documentation/save").hasAuthority("AUTH_APP_INTERNAL_EMPLOYEES");

        /* 未启用方法过滤 */
        requests.requestMatchers("/apps/project/main/history/list").denyAll();
        requests.requestMatchers("/collection/result/packageList").denyAll();
        requests.requestMatchers("/projectNo/applicationa/test/projectId").denyAll();
        requests.requestMatchers("/projectNo/applicationa/list").denyAll();

    }
}
