package com.yufeiblog.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yufeiblog.cassandra.common.CassandraConfiguration;
import com.yufeiblog.cassandra.common.Validate;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;

public class SessionManager {
    private Cluster cluster = null;
    private String contactPoint ;
    private SessionRepository sessionRepository;
    private Map<String, Object> replication = null;
    //private String port
    private CassandraConfiguration configuration = null;
    private String cassandraUserName;
    private String cassandraPassword;
    private boolean keepAlive = true;
    private AuthProvider authProvider;
    private LoadBalancingPolicy loadBalancingPolicy;
    private int port = 9042;
    private int connectTimeoutMillis = 5000;

    private SessionManager(String cassandraUserName,String cassandraPassword,String contactPoints,String replication){
        this.cassandraPassword=cassandraPassword;
        this.cassandraUserName=cassandraUserName;
        this.contactPoint=contactPoints;
        setReplication(replication);
    }

    public static Builder builder(){
        return new Builder();
    }
    public static class Builder {
        private String username;
        private String password;
        private String contactPoints ;
        private boolean keepAlive = true;
        private AuthProvider authProvider;
        private LoadBalancingPolicy loadBalancingPolicy;
        private int connectTimeoutMillis = 5000;
        private String replication;
        public Builder withCredentials(String username,String password){
            this.username=username;
            this.password=password;
            return this;
        }

        public Builder withContactPoints(String contactPoints){
            this.contactPoints=contactPoints;
            return this;
        }

        public Builder withReplication(String replication){
            this.replication=replication;
            return this;
        }
        public Builder withAuthProvider(AuthProvider authProvider){
            this.authProvider=authProvider;
            return this;
        }
        public Builder withKeepAlive(boolean keepAlive){
            this.keepAlive=keepAlive;
            return this;
        }

        public Builder withLoadBalancingPolicy(LoadBalancingPolicy loadBalancingPolicy){
            this.loadBalancingPolicy=loadBalancingPolicy;
            return this;
        }

        public Builder withConnectTimeoutMillis(int connectTimeoutMillis){
            this.connectTimeoutMillis=connectTimeoutMillis;
            return this;
        }

        public SessionManager build(){
            SessionManager sessionManager =  new SessionManager(username,password,contactPoints,replication);
            sessionManager.setAuthProvider(authProvider);
            sessionManager.setLoadBalancingPolicy(loadBalancingPolicy);
            sessionManager.setConnectTimeoutMillis(connectTimeoutMillis);
            sessionManager.setKeepAlive(keepAlive);
            sessionManager.init();
            return sessionManager;
        }
    }

    private void init() {
        Cluster.Builder builder = Cluster.builder()
                .addContactPointsWithPorts(prepareContactPoints(contactPoint))
                .withAuthProvider(new PlainTextAuthProvider(cassandraUserName, cassandraPassword))
                .withPort(port)
                .withPoolingOptions(new PoolingOptions()
                        .setCoreConnectionsPerHost(HostDistance.LOCAL, 4)
                        .setMaxConnectionsPerHost(HostDistance.LOCAL, 10)
                        .setCoreConnectionsPerHost(HostDistance.REMOTE, 2)
                        .setMaxConnectionsPerHost(HostDistance.REMOTE, 4))
                //.withCredentials(cassandraUserName, cassandraPassword)
                .withQueryOptions(prepareQueryOptions())
               // .withCompression(ProtocolOptions.Compression.SNAPPY)
                .withSocketOptions(prepareSocketOptions())
                .withTimestampGenerator(ServerSideTimestampGenerator.INSTANCE)
                .withRetryPolicy(prepareRetryPolicy())
                .withLoadBalancingPolicy(loadBalancingPolicy == null ? defaultLoadBalance() : loadBalancingPolicy);

        cluster = builder.build();
        sessionRepository = new SessionRepository(cluster, configuration, replication);
    }

    private InetSocketAddress[] prepareContactPoints(String contactPoints) {
        String[] contactPointArr = StringUtils.split(contactPoints, ",");
        //校验长度
        Validate.notThan();
        InetSocketAddress[] inetSocketAddress = new InetSocketAddress[contactPointArr.length];
        int i = 0;
        for (String contactPoint : contactPointArr) {
            String[] temp = StringUtils.split(contactPoint, ":");
            try {
                if (temp.length > 1) {
                    InetAddress inetAddresses = InetAddress.getByName(temp[0]);
                    inetSocketAddress[i++] = new InetSocketAddress(inetAddresses, Integer.valueOf(temp[1]));
                } else {
                    InetAddress inetAddresses = InetAddress.getByName(temp[0]);
                    inetSocketAddress[i++] = new InetSocketAddress(inetAddresses, port);
                }
            } catch (UnknownHostException e) {

            }
        }
        return inetSocketAddress;
    }

    public SessionRepository getSessionRepository(){
        return sessionRepository;
    }
    private QueryOptions prepareQueryOptions() {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        queryOptions.setSerialConsistencyLevel(ConsistencyLevel.SERIAL);
        return queryOptions;
    }

    private RetryPolicy prepareRetryPolicy() {
        RetryPolicy retryPolicy = DefaultRetryPolicy.INSTANCE;
        retryPolicy = new LoggingRetryPolicy(retryPolicy);
        //more Policy
        return retryPolicy;
    }

    private SocketOptions prepareSocketOptions() {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.setKeepAlive(keepAlive);
        socketOptions.setConnectTimeoutMillis(connectTimeoutMillis);
        return socketOptions;
    }

    private void prepareReplication() {
        if (replication != null) {
            return;
        }
        Set<Host> hosts = cluster.getMetadata().getAllHosts();
    }

    public CassandraConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(CassandraConfiguration configuration) {
        this.configuration = configuration;
    }

    private void setReplication(String replication) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this.replication = (Map) objectMapper.readValue(replication, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            //throw new exception
        }
    }


    private void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    private void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    private void setPort(int port) {
        this.port = port;
    }

    private void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public void close() {
        sessionRepository.close();
    }

    private void setLoadBalancingPolicy(LoadBalancingPolicy loadBalancingPolicy) {
        this.loadBalancingPolicy = loadBalancingPolicy;
    }

    private LoadBalancingPolicy defaultLoadBalance() {
        return new TokenAwarePolicy(DCAwareRoundRobinPolicy.builder().build());
    }

}
