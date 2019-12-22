package com.yufei.test.plugin.cassandra.client;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Authenticator;
import com.datastax.driver.core.exceptions.AuthenticationException;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class ScramAuthProvider implements AuthProvider {
    private volatile String username;
    private volatile String password;
    private static final String SCRAM_START = "SCRAM_START";
    private String firstClientMessage;
    private String firstServerMessage;
    private String salt;
    private int iteratorCount;

    public ScramAuthProvider(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Authenticator newAuthenticator(InetSocketAddress host, String authenticator) throws AuthenticationException {
        return new ScramAuthenticator(username, password);
    }

    private class ScramAuthenticator implements Authenticator {
        /*private final byte[] username;
        private final byte[] password;*/
        private String n1Str;
        private String serverFirstMsg;
        private String clientFirstMsg;
        private byte[] serverSign;

        public ScramAuthenticator(String username, String password) {
            /*this.username = username.getBytes(StandardCharsets.UTF_8);
            this.password = password.getBytes(StandardCharsets.UTF_8);*/
        }

        @Override
        public byte[] initialResponse() {

            byte[] head = SecureUtil.SCRAM_SUPPORT.getBytes(StandardCharsets.UTF_8);
            byte[] end = username.getBytes(StandardCharsets.UTF_8);
            byte[] initialToken = new byte[head.length + end.length];

            System.arraycopy(SecureUtil.SCRAM_SUPPORT.getBytes(StandardCharsets.UTF_8), 0, initialToken, 0, SecureUtil.SCRAM_SUPPORT.length());
            System.arraycopy(username.getBytes(StandardCharsets.UTF_8), 0, initialToken, SecureUtil.SCRAM_SUPPORT.length(), username.length());

            return initialToken;
        }

        @Override
        public byte[] evaluateChallenge(byte[] challenge) {
            String serverResponse = new String(challenge, StandardCharsets.UTF_8);
            if (SCRAM_START.equals(serverResponse)) {
                byte[] n1 = SecureUtil.random();
                n1Str = SecureUtil.base64Encode(n1);
                clientFirstMsg = username + SecureUtil.STORE_STRING_DELIMITER + n1Str;
                // todo 检查是否需要先每个字段 base64 编码
                return clientFirstMsg.getBytes(StandardCharsets.UTF_8);
            }
            if (serverFirstMsg == null) {
                serverFirstMsg = serverResponse;
                String[] firstMsgStr = serverFirstMsg.split(SecureUtil.STORE_STRING_DELIMITER);
                String n1FromServer = firstMsgStr[0];
                String n2FromServer = firstMsgStr[1];
                String salt = firstMsgStr[3];
                int iteratorCount = Integer.parseInt(firstMsgStr[4]);
                byte[] saltPwd = SecureUtil.pbkEncode(password, SecureUtil.base64Decode(salt), iteratorCount);
                byte[] serverKey = SecureUtil.hmac(saltPwd, SecureUtil.SERVER_KEY.getBytes(StandardCharsets.UTF_8));
                byte[] clientKey = SecureUtil.hmac(saltPwd, SecureUtil.CLIENT_KEY.getBytes(StandardCharsets.UTF_8));
                byte[] storeKey = SecureUtil.sha256(clientKey);

                String authMsg = clientFirstMsg + SecureUtil.STORE_STRING_DELIMITER +
                        serverFirstMsg + SecureUtil.STORE_STRING_DELIMITER +
                        n1Str + SecureUtil.STORE_STRING_DELIMITER +
                        n2FromServer;

                byte[] clientSign = SecureUtil.hmac(storeKey, authMsg.getBytes(StandardCharsets.UTF_8));
                byte[] clientProof = SecureUtil.xor(clientSign, authMsg.getBytes(StandardCharsets.UTF_8));
                serverSign = SecureUtil.hmac(serverKey, authMsg.getBytes(StandardCharsets.UTF_8));

                String finalResponse = n1FromServer + SecureUtil.STORE_STRING_DELIMITER +
                        n2FromServer + SecureUtil.STORE_STRING_DELIMITER +
                        SecureUtil.base64Encode(clientProof);

                return finalResponse.getBytes(StandardCharsets.UTF_8);
            }
            //firstServerMessage
            return new byte[0];
        }

        @Override
        public void onAuthenticationSuccess(byte[] token) {

        }
    }

    public static void main(String[] args) {
        byte[] bytes = new ScramAuthProvider("use", "1213").newAuthenticator(null, "").initialResponse();
        System.out.println(new String(bytes, StandardCharsets.UTF_8));
        //System.out.println(new String());
        //String userInfo = new String(clientResponse,StandardCharsets.UTF_8);
    }
}
