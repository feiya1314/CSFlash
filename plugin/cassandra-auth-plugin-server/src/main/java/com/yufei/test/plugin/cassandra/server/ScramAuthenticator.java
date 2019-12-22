package com.yufei.test.plugin.cassandra.server;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.cassandra.auth.AuthKeyspace;
import org.apache.cassandra.auth.AuthenticatedUser;
import org.apache.cassandra.auth.DataResource;
import org.apache.cassandra.auth.IAuthenticator;
import org.apache.cassandra.auth.IResource;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.cql3.statements.SelectStatement;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.exceptions.AuthenticationException;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;


public class ScramAuthenticator implements IAuthenticator {
    private SelectStatement authenticateStatement;
    private static final String DEFAULT_SUPERUSER_NAME = "cassandra";
    private static final String SCRAM_START = "SCRAM_START";
    private static final Logger logger = LoggerFactory.getLogger(ScramAuthenticator.class);
    private static final byte NUL = 0;

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
                SecureUtil.SALTED_HASH,
                AuthKeyspace.NAME,
                AuthKeyspace.ROLES);
        authenticateStatement = (SelectStatement) QueryProcessor.getStatement(query, ClientState.forInternalCalls()).statement;
    }

    @Override
    public SaslNegotiator newSaslNegotiator(InetAddress clientAddress) {
        return new ScramSaslAuthenticator();
    }

    @Override
    public AuthenticatedUser legacyAuthenticate(Map<String, String> credentials) throws AuthenticationException {
        throw new AuthenticationException("do not support thrift connection");
    }

    private AuthenticatedUser doAuthenticate(String username, String password, SelectStatement authenticationStatement)
            throws RequestExecutionException, AuthenticationException {
        ResultMessage.Rows rows = authenticationStatement.execute(QueryState.forInternalCalls(),
                QueryOptions.forInternalCalls(consistencyForRole(username),
                        Lists.newArrayList(ByteBufferUtil.bytes(username))));
        UntypedResultSet result = UntypedResultSet.create(rows.result);

        if ((result.isEmpty() || !result.one().has(SecureUtil.SALTED_HASH)) || !checkpw(password, result.one().getString(SecureUtil.SALTED_HASH)))
            throw new AuthenticationException("Username and/or password are incorrect");

        return new AuthenticatedUser(username);
    }

    private static boolean checkpw(String password, String hash) {
        try {
            return BCrypt.checkpw(password, hash);
        } catch (Exception e) {
            // Improperly formatted hashes may cause BCrypt.checkpw to throw, so trap any other exception as a failure
            logger.warn("Error: invalid password hash encountered, rejecting user", e);
            return false;
        }
    }

    private static ConsistencyLevel consistencyForRole(String role) {
        if (role.equals(DEFAULT_SUPERUSER_NAME))
            return ConsistencyLevel.QUORUM;
        else
            return ConsistencyLevel.LOCAL_ONE;
    }

    private class ScramSaslAuthenticator implements SaslNegotiator {
        private boolean complete = false;
        private String username;
        private String password;
        private ScramEvaluator scramEvaluator;
        private String clientFirstResponse;

        private ScramEvaluator parseSavePwd(String username) {
            if (username.isEmpty()) {
                throw new AuthenticationException("Username is empty");
            }

            ResultMessage.Rows rows = authenticateStatement.execute(QueryState.forInternalCalls(),
                    QueryOptions.forInternalCalls(consistencyForRole(username),
                            Lists.newArrayList(ByteBufferUtil.bytes(username))));

            UntypedResultSet result = UntypedResultSet.create(rows.result);
            String pwdInDB = result.one().getString(SecureUtil.SALTED_HASH);
            if (pwdInDB == null || pwdInDB.isEmpty()) {
                throw new AuthenticationException("Username and/or password are incorrect");
            }
            String[] saveStr = pwdInDB.split(SecureUtil.STORE_STRING_DELIMITER);
            if (!SecureUtil.SCRAM_SUPPORT.equals(saveStr[0])) {
                throw new AuthenticationException("This username is not support Scram");
            }
            this.username = username;
            return new ScramEvaluator(username, saveStr[1], saveStr[2], saveStr[3], Integer.parseInt(saveStr[4]));
        }

        @Override
        public byte[] evaluateResponse(byte[] clientResponse) throws AuthenticationException {
            if (scramEvaluator != null) {
                byte[] serverResponse = scramEvaluator.evaluateResponse(clientResponse);
                complete = scramEvaluator.checkComplete();

                return serverResponse;
            }

            String userInfo = new String(clientResponse, StandardCharsets.UTF_8);
            if (userInfo.startsWith(SecureUtil.SCRAM_SUPPORT)) {
                String responseUser = userInfo.substring(SecureUtil.SCRAM_SUPPORT.length());
                scramEvaluator = parseSavePwd(responseUser);
                complete = false;
                return SCRAM_START.getBytes(StandardCharsets.UTF_8);
            }

            decodeCredentials(clientResponse);
            complete = true;

            return null;

        }

        @Override
        public boolean isComplete() {
            return complete;
        }

        @Override
        public AuthenticatedUser getAuthenticatedUser() throws AuthenticationException {
            return null;
        }

        private void decodeCredentials(byte[] bytes) throws AuthenticationException {
            logger.trace("Decoding credentials from client token");
            byte[] user = null;
            byte[] pass = null;
            int end = bytes.length;
            for (int i = bytes.length - 1; i >= 0; i--) {
                if (bytes[i] == NUL) {
                    if (pass == null)
                        pass = Arrays.copyOfRange(bytes, i + 1, end);
                    else if (user == null)
                        user = Arrays.copyOfRange(bytes, i + 1, end);
                    end = i;
                }
            }

            if (user == null)
                throw new AuthenticationException("Authentication ID must not be null");
            if (pass == null)
                throw new AuthenticationException("Password must not be null");

            username = new String(user, StandardCharsets.UTF_8);
            password = new String(pass, StandardCharsets.UTF_8);
        }
    }

    private class ScramEvaluator {
        private String serverKey;
        private String storeKey;
        private String salt;
        private int iteratorCount;
        private boolean complete = false;
        private String clientFirstResponse;
        private String clientFinalResponse;
        private String initUsername;
        private String n1Str;
        private String n2Str;

        private ScramEvaluator(String initUsername, String serverKey, String storeKey, String salt, int iteratorCount) {
            this.initUsername = initUsername;
            this.serverKey = serverKey;
            this.storeKey = storeKey;
            this.salt = salt;
            this.iteratorCount = iteratorCount;
        }

        private byte[] evaluateResponse(byte[] clientResponse) {
            if (clientFirstResponse == null) {
                clientFirstResponse = new String(clientResponse, StandardCharsets.UTF_8);
                String[] clientMsg = clientFirstResponse.split(SecureUtil.STORE_STRING_DELIMITER);
                String usernameFromClient = clientMsg[0];
                n1Str = clientMsg[2];
                if (!initUsername.equals(usernameFromClient)) {
                    throw new AuthenticationException("Username is not equal with init");
                }
                byte[] n2 = SecureUtil.random();
                n2Str = SecureUtil.base64Encode(n2);
                String response = n1Str + SecureUtil.STORE_STRING_DELIMITER + n2Str +
                        SecureUtil.STORE_STRING_DELIMITER + salt + SecureUtil.STORE_STRING_DELIMITER + iteratorCount;
                complete = false;

                return response.getBytes(StandardCharsets.UTF_8);
            }

            clientFinalResponse = new String(clientResponse, StandardCharsets.UTF_8);
            return null;
        }

        private boolean checkComplete() {
            return complete;
        }
    }

}
