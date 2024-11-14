package com.qkinfotech.core.sys.base;

import com.qkinfotech.core.security.BaseModule;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component("module:sysBase")
public class ModuleSecurity extends BaseModule {

    @Override
    public void configAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
        requests.requestMatchers("/table/**").permitAll();
    }
}