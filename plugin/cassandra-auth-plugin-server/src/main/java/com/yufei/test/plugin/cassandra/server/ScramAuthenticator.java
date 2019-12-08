package com.yufei.test.plugin.cassandra.server;

import com.google.common.collect.ImmutableSet;
import org.apache.cassandra.auth.AuthKeyspace;
import org.apache.cassandra.auth.AuthenticatedUser;
import org.apache.cassandra.auth.DataResource;
import org.apache.cassandra.auth.IAuthenticator;
import org.apache.cassandra.auth.IResource;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.exceptions.AuthenticationException;
import org.apache.cassandra.exceptions.ConfigurationException;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

public class ScramAuthenticator implements IAuthenticator {
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
       // String query = String.format("SELECT %s FROM %s.%s WHERE role = ?",
       //         SALTED_HASH,
            //    AuthKeyspace.NAME,
          //      AuthKeyspace.ROLES);
      //  authenticateStatement = prepare(query);

      //  if (Schema.instance.getCFMetaData(AuthKeyspace.NAME, LEGACY_CREDENTIALS_TABLE) != null)
       //     prepareLegacyAuthenticateStatement();
    }

    @Override
    public SaslNegotiator newSaslNegotiator(InetAddress clientAddress) {
        return new ScramSaslAuthenticator();
    }

    @Override
    public AuthenticatedUser legacyAuthenticate(Map<String, String> credentials) throws AuthenticationException {
        return null;
    }

    private static class ScramSaslAuthenticator implements SaslNegotiator{
        @Override
        public byte[] evaluateResponse(byte[] clientResponse) throws AuthenticationException {
            return new byte[0];
        }

        @Override
        public boolean isComplete() {
            return false;
        }

        @Override
        public AuthenticatedUser getAuthenticatedUser() throws AuthenticationException {
            return null;
        }
    }
}
