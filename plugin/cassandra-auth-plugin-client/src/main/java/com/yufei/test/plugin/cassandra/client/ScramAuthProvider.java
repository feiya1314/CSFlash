package com.yufei.test.plugin.cassandra.client;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Authenticator;
import com.datastax.driver.core.exceptions.AuthenticationException;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class ScramAuthProvider implements AuthProvider {
    private volatile String username;
    private volatile String password;
    private static String SCRAM_SUPPORT = "SCRAM_@#SUPPORT";
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

        public ScramAuthenticator(String username, String password) {
            /*this.username = username.getBytes(StandardCharsets.UTF_8);
            this.password = password.getBytes(StandardCharsets.UTF_8);*/
        }

        @Override
        public byte[] initialResponse() {
            byte[] initialToken = new byte[username.length() + SCRAM_SUPPORT.length()];
            System.arraycopy(SCRAM_SUPPORT.getBytes(StandardCharsets.UTF_8), 0, initialToken, 0, SCRAM_SUPPORT.length());
            System.arraycopy(username.getBytes(StandardCharsets.UTF_8), 0, initialToken, SCRAM_SUPPORT.length(), username.length());

            return initialToken;
        }

        @Override
        public byte[] evaluateChallenge(byte[] challenge) {
            //firstServerMessage
            return new byte[0];
        }

        @Override
        public void onAuthenticationSuccess(byte[] token) {

        }
    }
}
