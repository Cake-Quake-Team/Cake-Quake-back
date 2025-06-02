package com.cakequake.cakequakeback.payment.sercurity;

public interface EncryptionService {

    //평문을 암호화하여 반환
    String encrypt(String plaintext);

    //암호문을 복호화하여 반환
    String decrypt(String ciphertext);
}
