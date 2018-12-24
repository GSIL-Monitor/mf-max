package com.mryx.matrix.project.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;

/**
 * @author sdcuike
 * @date 2018/9/26
 * @since 2018/9/26
 */
@Configuration
public class WebConfig {

    @Bean
    @ConditionalOnClass(RequestContextListener.class)
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}
