package com.yufei.test.plugin.cassandra.server;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.apache.cassandra.auth.AuthKeyspace;
import org.apache.cassandra.auth.AuthenticatedUser;
import org.apache.cassandra.auth.CassandraRoleManager;
import org.apache.cassandra.auth.RoleOptions;
import org.apache.cassandra.auth.RoleResource;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ScramRoleManager extends CassandraRoleManager {
    private final Set<Option> supportedOptions;
    private final Set<Option> alterableOptions;

    //private SelectStatement authenticateStatement;
    public ScramRoleManager() {
        supportedOptions = DatabaseDescriptor.getAuthenticator().getClass() == ScramAuthenticator.class
                ? ImmutableSet.of(Option.LOGIN, Option.SUPERUSER, Option.PASSWORD)
                : ImmutableSet.of(Option.LOGIN, Option.SUPERUSER);
        alterableOptions = DatabaseDescriptor.getAuthenticator().getClass().equals(ScramAuthenticator.class)
                ? ImmutableSet.of(Option.PASSWORD)
                : ImmutableSet.<Option>of();
    }

    @Override
    public Set<Option> supportedOptions() {
        return supportedOptions;
    }

    @Override
    public Set<Option> alterableOptions() {
        return alterableOptions;
    }

    /*@Override
    public void setup() {
        super.setup();
        String query = String.format("SELECT %s FROM %s.%s WHERE role = ?",
                SecureUtil.SALTED_HASH,
                AuthKeyspace.NAME,
                AuthKeyspace.ROLES);
        authenticateStatement = (SelectStatement) QueryProcessor.getStatement(query, ClientState.forInternalCalls()).statement;

    }*/

    @Override
    public void createRole(AuthenticatedUser performer, RoleResource role, RoleOptions options) throws RequestValidationException, RequestExecutionException {

        if (!options.getPassword().isPresent()) {
            super.createRole(performer, role, options);
        }
        if (!options.getOptions().containsKey(Option.PASSWORD)) {
            super.createRole(performer, role, options);
        }

        String savePwd = SecureUtil.getSavePwd(options.getPassword().get(), SecureUtil.ITERATOR_COUNT);
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
    public void alterRole(AuthenticatedUser performer, RoleResource role, RoleOptions options) {
        if (!options.getOptions().containsKey(Option.PASSWORD)) {
            super.alterRole(performer, role, options);
        }

        String assignments = Joiner.on(',')
                .join(StreamSupport.stream(optionsToAssignments(options.getOptions()).spliterator(), false)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
        if (!Strings.isNullOrEmpty(assignments)) {
            String query = String.format("UPDATE %s.%s SET %s WHERE role = '%s'",
                    AuthKeyspace.NAME,
                    AuthKeyspace.ROLES,
                    assignments,
                    escape(role.getRoleName()));
            QueryProcessor.process(query, consistencyForRole(role.getRoleName()));
        }
    }

    private static String escape(String name) {
        return StringUtils.replace(name, "'", "''");
    }

    private Iterable<String> optionsToAssignments(Map<Option, Object> options) {
        return options.entrySet().stream().map(entry -> {
            switch (entry.getKey()) {
                case LOGIN:
                    return String.format("can_login = %s", entry.getValue());
                case SUPERUSER:
                    return String.format("is_superuser = %s", entry.getValue());
                case PASSWORD:
                    return String.format("salted_hash = '%s'", SecureUtil.getSavePwd((String) entry.getValue(), SecureUtil.ITERATOR_COUNT));
                default:
                    return null;
            }
        }).collect(Collectors.toList());
    }
}
