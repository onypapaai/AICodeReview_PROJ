package com.cleverse.ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
// JSTL 사용하지 않음

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 리소스 핸들러
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        // JSP 뷰 리졸버 설정 (JSTL 없이)
        InternalResourceViewResolver jspResolver = new InternalResourceViewResolver();
        jspResolver.setPrefix("/WEB-INF/jsp/");
        jspResolver.setSuffix(".jsp");
        jspResolver.setOrder(1);
        registry.viewResolver(jspResolver);
    }
}