package com.yufeiblog.cassandra.test;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.yufeiblog.cassandra.SessionManager;
import com.yufeiblog.cassandra.service.ICassandraManageService;
import com.yufeiblog.cassandra.service.CassandraManageService;

public class CassandraTest {
    public static void main(String[] args) {
        int a =0;
        int b=12;
        System.out.println(a+b);
       /* Cluster cluster = null;
        String keyspace = "mykeyspace";
        String tablenName = "mytest";
        KeyspaceMetadata keyspaceMetadata = null;
        TableMetadata tableMetadata = null;
        try {
            // Cluster对象是驱动程序的主入口点。 它保存了实际Cassandra集群（特别是元数据）的已知状态。
            // 这个类是线程安全的，你应该创建一个单独的实例（每个目标Cassandra集群），并在你的应用程序中共享它;
            SessionManager sessionManager = new SessionManager();
            sessionManager.setCassandraUserName("cassandra");
            sessionManager.setCassandraPassword("cassandra");
            sessionManager.setContactPoint("192.168.3.8");
            sessionManager.setReplication("{\"class\": \"NetworkTopologyStrategy\",\"DC1\": \"2\",\"DC2\": \"2\"}");
            //Session session = sessionRepository.getSession();
            Session session = null ;
            cluster = session.getCluster();
            ICassandraManageService ICassandraManageService = new CassandraManageService(sessionManager);
            ICassandraManageService.createUser("feiya", "yf.test");

            ICassandraManageService.createKeyspace(121);
            ResultSet resultSet = session.execute("select release_version from system.local");
            Row row = resultSet.one();
            //QueryBuilder.decr()
            Metadata metadata = cluster.getMetadata();
            if (metadata.getKeyspace(keyspace) == null) {
                session.execute("CREATE KEYSPACE " + keyspace + " WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy','DC1':2 } AND DURABLE_WRITES = true;");
            }
            keyspaceMetadata = metadata.getKeyspace(keyspace);
            tableMetadata = keyspaceMetadata.getTable(tablenName);

            if (tableMetadata == null) {
                session.execute("CREATE Table " + keyspace + "." + tablenName + " (pk int PRIMARY KEY,"
                        + "uid int ,"
                        + "username text,"
                        + "password text)");
            }
            //resultSet=session.execute("describe keyspaces");
            System.out.println("out");
            System.out.println(row.getString("release_version"));
            //Statement statement = Create
            //session.execute()
            Insert insert = QueryBuilder.insertInto(tableMetadata);
            //insert.s
            //BoundStatement boundStatement
            session.execute("INSERT INTO " + keyspace + "." + tablenName + "(pk, uid, username,password) VALUES(100000,1,'jack','Hyderabad')");
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }*/
    }

}
