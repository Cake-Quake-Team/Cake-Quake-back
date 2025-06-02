package com.cakequake.cakequakeback.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JasyptConfig {

    //Jasypt를 위한 String Encryptor 빈을 등록합
    //환경 변수 JASYPT_ENCRYPTOR_PASSWORD가 설정되어 있어야 정상 동작

    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();

        // 시스템 환경 변수로부터 마스터 패스워드를 읽어옴
        String masterPassword = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
        if (masterPassword == null || masterPassword.isBlank()) {
            throw new IllegalArgumentException("JASYPT_ENCRYPTOR_PASSWORD 환경 변수가 설정되어 있지 않습니다");
        }
        config.setPassword(masterPassword);

        //암호화 알고리즘(PBEWithMDSAndDES 등)을 지정
        config.setAlgorithm("PBEWithMD5AndDES");

        //설정값 추가
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");

        encryptor.setConfig(config);
        return encryptor;
    }

}
