package com.savit.card.util;

import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Getter
@Component
public class CodefUtil {

    @Value("${codef.client-id}") private String clientId;
    @Value("${codef.client-secret}") private String clientSecret;
    @Value("${codef.public-key}") private String publicKey;

    public EasyCodef newClient() {
        log.info("[CODEF] clientId = {}", clientId);
        log.info("[CODEF] clientSecret = {}", clientSecret);
        EasyCodef codef = new EasyCodef();
        codef.setClientInfoForDemo(clientId, clientSecret);
        codef.setPublicKey(publicKey);
        return codef;
    }

    public String encryptRSA(String plainText) {
        try {
            return EasyCodefUtil.encryptRSA(plainText, publicKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException |
                 NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {

            log.error("RSA 암호화 실패 – 입력=[{}]", plainText, e);
            throw new IllegalStateException("CODEF RSA 암호화 중 오류가 발생했습니다.", e);
        }
    }
}
