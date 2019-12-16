package com.yufei.test.plugin.cassandra.client;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureUtil {
    private final static String SHA256_ALGORITHM = "PBKDF2WithHmacSHA256";
    private final static int ITERATOR_COUNT = 1000;

    public static String base64Encode(byte[] src) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(src);
    }

    public static byte[] base64Decode(String src) {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(src);
    }

    public static byte[] pbkEncode(String password, byte[] salt) throws Exception {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(SHA256_ALGORITHM);
        SecretKey secretKey = keyFactory.generateSecret(keySpec);

        PBEParameterSpec paramSpec = new PBEParameterSpec(salt, ITERATOR_COUNT);
        Cipher cipher = Cipher.getInstance(SHA256_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
        return cipher.doFinal();
    }

    public static byte[] pbkDecode(String password, byte[] salt) throws Exception {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(SHA256_ALGORITHM);
        SecretKey secretKey = keyFactory.generateSecret(keySpec);

        PBEParameterSpec paramSpec = new PBEParameterSpec(salt, ITERATOR_COUNT);
        Cipher cipher = Cipher.getInstance(SHA256_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
        return cipher.doFinal();
    }

    public static byte[] random() {
        byte[] values = new byte[128];
        SecureRandom random = new SecureRandom();
        random.nextBytes(values);
        return values;
    }

    public static void main(String[] args) {
        String test = "ytest6765756756ghsdfhgsdhsdgh";
        System.out.println(base64Encode(test.getBytes(StandardCharsets.UTF_8)));
        System.out.println(new String(base64Decode("eXRlc3Q2NzY1NzU2NzU2Z2hzZGZoZ3NkaHNkZ2g=")));
    }
}
