package com.cakequake.cakequakeback.payment.sercurity;

import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EncryptionsServiceImpl implements  EncryptionService{
    private final StringEncryptor encryptor;

    //평문을 암호화하여 반환
    @Override
    public String encrypt(String plaintext) {
        return encryptor.encrypt(plaintext);
    }

    //암호문을 복호화하여 반환
    @Override
    public String decrypt(String ciphertext) {
        return encryptor.decrypt(ciphertext);
    }
}
