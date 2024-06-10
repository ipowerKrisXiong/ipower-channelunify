package com.ipower.cloud.channelunify.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
//下面是为了保证AuthCenterInterceptorConfig先执行，这样TenantInputInterceptor过滤器的顺序才能在token过滤器之后，AuthCenterInterceptorConfig的顺序是100
@Order(200)
//@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 100)
public class TenantInterceptorConfig
        implements WebMvcConfigurer {

    @Bean
    public TenantInputInterceptor getTenantInputInterceptor() {

        return new TenantInputInterceptor();

    }

    //添加权限转发拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //所有请求走鉴权过滤器
        registry.addInterceptor(getTenantInputInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/actuator/**");
    }


}
