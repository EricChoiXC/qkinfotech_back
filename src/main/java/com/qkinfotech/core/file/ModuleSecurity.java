package com.qkinfotech.core.file;

import com.qkinfotech.core.security.BaseModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@Configuration("module:core.file")
public class ModuleSecurity extends BaseModule {

    @Override
    public void configAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
        /* 未启用方法过滤 */
        requests.requestMatchers("/file/checkChunkDigest").denyAll();
        requests.requestMatchers("/file/checkFileDigest").denyAll();
        requests.requestMatchers("/file/uploadChunk").denyAll();
    }
}
