package com.yufei.test.plugin.cassandra.client;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Authenticator;
import com.datastax.driver.core.exceptions.AuthenticationException;
import com.google.common.base.Charsets;

import java.net.InetSocketAddress;

public class ScramAuthProvider implements AuthProvider {
    private volatile String username;
    private volatile String password;
    private static String SCRAM_SUPPORT = "SCRAM_@#SUPPORT";

    public ScramAuthProvider(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Authenticator newAuthenticator(InetSocketAddress host, String authenticator) throws AuthenticationException {
        return new ScramAuthenticator(username, password);
    }

    private static class ScramAuthenticator implements Authenticator {
        private final byte[] username;
        private final byte[] password;

        public ScramAuthenticator(String username, String password) {
            this.username = username.getBytes(Charsets.UTF_8);
            this.password = password.getBytes(Charsets.UTF_8);
        }

        @Override
        public byte[] initialResponse() {
            byte[] initialToken = new byte[username.length + SCRAM_SUPPORT.length()];
            System.arraycopy(SCRAM_SUPPORT.getBytes(Charsets.UTF_8), 0, initialToken, 0, SCRAM_SUPPORT.length());
            System.arraycopy(username, 0, initialToken, SCRAM_SUPPORT.length(), username.length);

            return initialToken;
        }

        @Override
        public byte[] evaluateChallenge(byte[] challenge) {
            return new byte[0];
        }

        @Override
        public void onAuthenticationSuccess(byte[] token) {

        }
    }
}
