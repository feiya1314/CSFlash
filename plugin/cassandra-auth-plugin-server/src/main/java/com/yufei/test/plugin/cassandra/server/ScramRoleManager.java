package com.yufei.test.plugin.cassandra.server;

import org.apache.cassandra.auth.AuthenticatedUser;
import org.apache.cassandra.auth.CassandraRoleManager;
import org.apache.cassandra.auth.RoleOptions;
import org.apache.cassandra.auth.RoleResource;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;

public class ScramRoleManager extends CassandraRoleManager {
    public ScramRoleManager(String username) {
    }

    @Override
    public void createRole(AuthenticatedUser performer, RoleResource role, RoleOptions options) throws RequestValidationException, RequestExecutionException {
        super.createRole(performer, role, options);
    }

    public ScramRoleManager() {
        super();
        //consistencyForRole()
    }

    @Override
    public void setup() {
        super.setup();
    }

    @Override
    public void alterRole(AuthenticatedUser performer, RoleResource role, RoleOptions options) {
        super.alterRole(performer, role, options);
    }

    @Override
    public void grantRole(AuthenticatedUser performer, RoleResource role, RoleResource grantee) throws RequestValidationException, RequestExecutionException {
        super.grantRole(performer, role, grantee);
    }
}
