package com.yufei.test.plugin.cassandra.server;

import org.apache.cassandra.auth.AuthKeyspace;
import org.apache.cassandra.auth.AuthenticatedUser;
import org.apache.cassandra.auth.CassandraRoleManager;
import org.apache.cassandra.auth.RoleOptions;
import org.apache.cassandra.auth.RoleResource;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

public class ScramRoleManager extends CassandraRoleManager {

    public ScramRoleManager() {
        super();
    }

    @Override
    public void createRole(AuthenticatedUser performer, RoleResource role, RoleOptions options) throws RequestValidationException, RequestExecutionException {

        if (!options.getPassword().isPresent()) {
            super.createRole(performer, role, options);
        }
        if (!options.getOptions().containsKey(Option.PASSWORD)) {
            super.createRole(performer, role, options);
        }
        byte[] salt = SecureUtil.random();
        byte[] saltPwd = SecureUtil.pbkEncode(options.getPassword().get(), salt);
        byte[] serverKey = SecureUtil.hmac(saltPwd, SecureUtil.SERVER_KEY.getBytes(StandardCharsets.UTF_8));
        byte[] clientKey = SecureUtil.hmac(saltPwd, SecureUtil.CLIENT_KEY.getBytes(StandardCharsets.UTF_8));
        byte[] storeKey = SecureUtil.sha256(clientKey);

        String savePwd = SecureUtil.getSavePwd(serverKey, storeKey, salt, SecureUtil.ITERATOR_COUNT);
        String insertCql = String.format("INSERT INTO %s.%s (role, is_superuser, can_login, salted_hash) VALUES ('%s', %s, %s, '%s')",
                AuthKeyspace.NAME,
                AuthKeyspace.ROLES,
                escape(role.getRoleName()),
                options.getSuperuser().or(false),
                options.getLogin().or(false),
                escape(savePwd));
        QueryProcessor.process(insertCql, consistencyForRole(role.getRoleName()));
    }


    @Override
    public void setup() {
        super.setup();
    }

    @Override
    public void alterRole(AuthenticatedUser performer, RoleResource role, RoleOptions options) {
        if (!options.getOptions().containsKey(Option.PASSWORD)) {
            super.alterRole(performer, role, options);
        }

    }

    private static String escape(String name) {
        return StringUtils.replace(name, "'", "''");
    }
}
