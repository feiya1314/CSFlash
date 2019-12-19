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
    public final static int ITERATOR_COUNT = 1000;
    public final static String SERVER_KEY = "Server Key";
    public final static String CLIENT_KEY = "Client Key";
    public final static String STORE_STRING_DELIMITER = "-";

    public static String base64Encode(byte[] src) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(src);
    }

    public static byte[] base64Decode(String src) {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(src);
    }

    public static byte[] pbkEncode(String password, byte[] salt) {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATOR_COUNT, 128);
        SecretKey secretKey = null;
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(SHA256_ALGORITHM);
            secretKey = keyFactory.generateSecret(keySpec);
        } catch (Exception e) {
            //todo
            return null;
        }

        return secretKey.getEncoded();
    }

    public static byte[] hmac(byte[] data, byte[] key) {
        Mac mac = null;
        try {
            mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKey secretKey = new SecretKeySpec(key, HMAC_ALGORITHM);

            mac.init(secretKey);
        } catch (Exception e) {
            // todo
            return null;
        }
        return mac.doFinal(data);
    }

    public static byte[] sha256(byte[] key) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            // todo
            return null;
        }

        digest.update(key);
        return digest.digest();
    }

    public static String getSavePwd(byte[] serverKey, byte[] storeKey, byte[] salt, int iteratorCount) {
        return base64Encode(serverKey) + STORE_STRING_DELIMITER +
                base64Encode(storeKey) + STORE_STRING_DELIMITER +
                base64Encode(salt) + STORE_STRING_DELIMITER + iteratorCount;
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
        System.out.println(base64Encode(hmac(test.getBytes(StandardCharsets.UTF_8), SERVER_KEY.getBytes(StandardCharsets.UTF_8))));
        System.out.println(base64Encode(test.getBytes(StandardCharsets.UTF_8)));
        System.out.println(new String(base64Decode("eXRlc3Q2NzY1NzU2NzU2Z2hzZGZoZ3NkaHNkZ2g=")));
    }
}
