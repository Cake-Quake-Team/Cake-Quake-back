package com.cakequake.cakequakeback.config;

import com.cakequake.cakequakeback.payment.sercurity.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeyEncryptorRunner implements CommandLineRunner {

    private final EncryptionService encryptionService;

    // application.yml에 설정된 환경변수(키)들을 주입받습니다.
    @Value("${spring.pg.kakao.admin-key}") private String kakaoAdminKey;
    @Value("${spring.pg.kakao.cid}")       private String kakaoCid;
    @Value("${spring.pg.toss.secret-key}") private String tossSecretKey;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 암호화된 샌드박스 키 ===");
        System.out.println("Encrypted Kakao AdminKey : " + encryptionService.encrypt(kakaoAdminKey));
        System.out.println("Encrypted Kakao CID      : " + encryptionService.encrypt(kakaoCid));
        System.out.println("Encrypted Toss SecretKey : " + encryptionService.encrypt(tossSecretKey));
        System.out.println("=== 끝 ===");

    }
}
