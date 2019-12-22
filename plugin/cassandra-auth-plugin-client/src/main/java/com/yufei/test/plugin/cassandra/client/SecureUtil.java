package com.yufei.test.plugin.cassandra.client;

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
    protected static String SCRAM_SUPPORT = "SCRAM_@#SUPPORT#";
    protected final static int ITERATOR_COUNT = 1000;
    protected final static String SERVER_KEY = "Server Key";
    protected final static String CLIENT_KEY = "Client Key";
    protected final static String STORE_STRING_DELIMITER = "-";
    protected static final String SALTED_HASH = "salted_hash";

    protected static String base64Encode(byte[] src) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(src);
    }

    protected static byte[] base64Decode(String src) {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(src);
    }


    protected static byte[] pbkEncode(String password, byte[] salt, int iteratorCount) {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iteratorCount, 128);
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

    protected static byte[] pbkEncode(String password, byte[] salt) {
        return pbkEncode(password, salt, ITERATOR_COUNT);
    }

    protected static byte[] hmac(byte[] data, byte[] key) {
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

    protected static byte[] xor(byte[] key, byte[] auth) {
        if (key == null || key.length == 0 || auth == null || auth.length == 0) {
            return key;
        }

        byte[] result = new byte[key.length];

        // 使用密钥字节数组循环加密或解密
        for (int i = 0; i < key.length; i++) {
            // 数据与密钥异或, 再与循环变量的低8位异或（增加复杂度）
            result[i] = (byte) (key[i] ^ auth[i % auth.length] ^ (i & 0xFF));
        }

        return result;
    }

    protected static byte[] sha256(byte[] key) {
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

    protected static String getSavePwd(String pwd, int iteratorCount) {
        byte[] salt = SecureUtil.random();
        byte[] saltPwd = SecureUtil.pbkEncode(pwd, salt);
        byte[] serverKey = SecureUtil.hmac(saltPwd, SecureUtil.SERVER_KEY.getBytes(StandardCharsets.UTF_8));
        byte[] clientKey = SecureUtil.hmac(saltPwd, SecureUtil.CLIENT_KEY.getBytes(StandardCharsets.UTF_8));
        byte[] storeKey = SecureUtil.sha256(clientKey);

        return SCRAM_SUPPORT + STORE_STRING_DELIMITER + base64Encode(serverKey) + STORE_STRING_DELIMITER +
                base64Encode(storeKey) + STORE_STRING_DELIMITER +
                base64Encode(salt) + STORE_STRING_DELIMITER + iteratorCount;
    }

    protected static byte[] random() {
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
