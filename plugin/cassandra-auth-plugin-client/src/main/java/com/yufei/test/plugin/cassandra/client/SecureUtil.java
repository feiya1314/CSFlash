package com.yufei.test.plugin.cassandra.client;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SecureUtil {
    private final static String SHA256_ALGORITHM = "PBKDF2WithHmacSHA256";

    public static String encode(byte[] src) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(src);
    }

    public static byte[] decode(String src) {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(src);
    }

    private static byte[] toKey(String password) throws Exception {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(SHA256_ALGORITHM);
        SecretKey secretKey = keyFactory.generateSecret(keySpec);

        PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 100);
        Cipher cipher = Cipher.getInstance(SHA256_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
        return cipher.doFinal(data);
    }

    public static void main(String[] args) {
        String test = "ytest6765756756ghsdfhgsdhsdgh";
        System.out.println(encode(test.getBytes(StandardCharsets.UTF_8)));
        System.out.println(new String(decode("eXRlc3Q2NzY1NzU2NzU2Z2hzZGZoZ3NkaHNkZ2g=")));
    }
}
