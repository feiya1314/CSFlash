package com.yufei.test.plugin.cassandra.server;

import org.apache.cassandra.auth.AuthenticatedUser;
import org.apache.cassandra.auth.CassandraRoleManager;
import org.apache.cassandra.auth.RoleOptions;
import org.apache.cassandra.auth.RoleResource;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.QueryState;

public class ScramRoleManager extends CassandraRoleManager {
    public ScramRoleManager(String username) {
    }

    @Override
    public void createRole(AuthenticatedUser performer, RoleResource role, RoleOptions options) throws RequestValidationException, RequestExecutionException {
        super.createRole(performer, role, options);
        QueryProcessor.execute("",consistencyForRole(role.getRoleName()),QueryState.forInternalCalls());
    }

    public ScramRoleManager() {
        super();
        //consistencyForRole()
       // QueryProcessor.execute("",QueryState.forInternalCalls())
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
