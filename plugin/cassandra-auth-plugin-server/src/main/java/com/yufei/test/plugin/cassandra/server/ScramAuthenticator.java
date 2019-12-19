package com.yufei.test.plugin.cassandra.server;

import com.google.common.collect.ImmutableSet;
import org.apache.cassandra.auth.AuthKeyspace;
import org.apache.cassandra.auth.AuthenticatedUser;
import org.apache.cassandra.auth.DataResource;
import org.apache.cassandra.auth.IAuthenticator;
import org.apache.cassandra.auth.IResource;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.cql3.statements.SelectStatement;
import org.apache.cassandra.exceptions.AuthenticationException;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.service.ClientState;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

public class ScramAuthenticator implements IAuthenticator {
    private static String SCRAM_SUPPORT = "SCRAM_@#SUPPORT";
    private static final String SALTED_HASH = "salted_hash";
    private SelectStatement legacyAuthenticateStatement;

    @Override
    public boolean requireAuthentication() {
        return true;
    }

    @Override
    public Set<? extends IResource> protectedResources() {
        return ImmutableSet.of(DataResource.table(AuthKeyspace.NAME, AuthKeyspace.ROLES));
    }

    @Override
    public void validateConfiguration() throws ConfigurationException {

    }

    @Override
    public void setup() {
        String query = String.format("SELECT %s FROM %s.%s WHERE role = ?",
                SALTED_HASH,
                AuthKeyspace.NAME,
                AuthKeyspace.ROLES);
        legacyAuthenticateStatement = (SelectStatement) QueryProcessor.getStatement(query, ClientState.forInternalCalls()).statement;
    }

    @Override
    public SaslNegotiator newSaslNegotiator(InetAddress clientAddress) {
        return new ScramSaslAuthenticator();
    }

    @Override
    public AuthenticatedUser legacyAuthenticate(Map<String, String> credentials) throws AuthenticationException {
        throw new AuthenticationException("do not support thrift connection");
    }

    private static class ScramSaslAuthenticator implements SaslNegotiator {
        private boolean complete = false;

        @Override
        public byte[] evaluateResponse(byte[] clientResponse) throws AuthenticationException {
            return new byte[0];
        }

        @Override
        public boolean isComplete() {
            return complete;
        }

        @Override
        public AuthenticatedUser getAuthenticatedUser() throws AuthenticationException {
            return null;
        }
    }
}
