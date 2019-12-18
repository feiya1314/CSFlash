package com.yufei.test.plugin.cassandra.server;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureUtil {
    private final static String SHA256_ALGORITHM = "PBKDF2WithHmacSHA256";
    private final static String HMAC_ALGORITHM = "HmacSHA256";
    private final static int ITERATOR_COUNT = 1000;
    private final static String SERVER_KEY = "Server Key";
    private final static String CLIENT_KEY = "Client Key";

    public static String base64Encode(byte[] src) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(src);
    }

    public static byte[] base64Decode(String src) {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(src);
    }

    public static byte[] pbkEncode(String password, byte[] salt) throws Exception {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATOR_COUNT, 128);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(SHA256_ALGORITHM);
        SecretKey secretKey = keyFactory.generateSecret(keySpec);

        return secretKey.getEncoded();
    }

    public static byte[] hmac(byte[] data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKey secretKey = new SecretKeySpec(key, HMAC_ALGORITHM);

        mac.init(secretKey);
        return mac.doFinal(data);
    }

    public static byte[] sha256(byte[] key) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(key);
        return digest.digest();
    }

    public static byte[] random() {
        byte[] values = new byte[18];
        SecureRandom random = new SecureRandom();
        random.nextBytes(values);
        return values;
    }

    public static void main(String[] args) throws Exception {
        String test = "ytest6765756756ghsdfhgsdhsdgh";
        System.out.println(base64Encode(pbkEncode(test, random())));
        System.out.println(base64Encode(sha256(test.getBytes(StandardCharsets.UTF_8))));
        System.out.println(base64Encode(hmac(test.getBytes(StandardCharsets.UTF_8),SERVER_KEY.getBytes(StandardCharsets.UTF_8))));
        System.out.println(base64Encode(test.getBytes(StandardCharsets.UTF_8)));
        System.out.println(new String(base64Decode("eXRlc3Q2NzY1NzU2NzU2Z2hzZGZoZ3NkaHNkZ2g=")));
    }
}
