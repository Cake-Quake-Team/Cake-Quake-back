package com.cakequake.cakequakeback.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String UPLOAD_DIR = "C:/nginx-1.26.3/html/reviewuploads";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/reviewuploads/**")
                .addResourceLocations("file:" + UPLOAD_DIR + "/");
    }
}