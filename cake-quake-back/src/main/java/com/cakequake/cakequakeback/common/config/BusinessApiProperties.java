package com.cakequake.cakequakeback.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*
    사업자 진위 확인 및 상태조회 서비스 api를 이용하기 위한 키 값 바인딩
 */
@Component
@ConfigurationProperties(prefix = "external.biz-api")
public class BusinessApiProperties {
    @Value("${external.biz-api.service-key}")
    private String serviceKey;

    public String getServiceKey() {
        return serviceKey;
    }
}
