package com.yufei.test;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.UniformReservoir;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.TableMetadata;
import com.yufeiblog.cassandra.SessionManager;
import com.yufeiblog.cassandra.SessionRepository;
import com.yufeiblog.cassandra.common.EtcdConfiguraion;
import com.yufeiblog.cassandra.common.TableOptions;
import com.yufeiblog.cassandra.dcmonitor.DCStatusWatcher;
import com.yufeiblog.cassandra.loadbalance.DCSwitchRoundRobinPolicy;
import com.yufeiblog.cassandra.loadbalance.SimpleDCSwitchPolicy;
import com.yufeiblog.cassandra.loadbalance.SwitchLoadbalancePolicy;
import com.yufeiblog.cassandra.model.Column;
import com.yufeiblog.cassandra.result.Result;
import com.yufeiblog.cassandra.service.CassandraManageService;
import com.yufeiblog.cassandra.service.ICassandraManageService;
import com.yufeiblog.cassandra.utils.Utils;

import java.sql.SQLOutput;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

abstract class BaseTest {

    protected int appId = 20190103;
    protected String replication = "{\"class\": \"NetworkTopologyStrategy\",\"dc1\": \"3\",\"dc2\": \"3\"}";
    protected String tableName = "yftest";
    protected String username = "cassandra";
    protected String password = "cassandra";
    protected String contactPoints = "192.168.3.10";
    protected ICassandraManageService service;
    protected int startUid = 1;
    protected AtomicInteger uid = new AtomicInteger(startUid);
    protected int recordSize = 5;
    protected int lastingTime = 10 * 60 * 1000;
    protected SessionRepository sessionRepository;

    protected int threadNums = 2;
    protected CountDownLatch countDownLatch = new CountDownLatch(threadNums);
    protected String currentDC = "DC1";
    private AtomicInteger successTimes = new AtomicInteger(0);
    private AtomicInteger failureTimes = new AtomicInteger(0);
    private ExponentiallyDecayingReservoir decayingReservoir = new ExponentiallyDecayingReservoir();
    private UniformReservoir uniformReservoir = new UniformReservoir();
    private Histogram histogram = new Histogram(uniformReservoir);

    public BaseTest() {
        init();
    }


    private void init() {
        SwitchLoadbalancePolicy loadBalancingPolicy = new SimpleDCSwitchPolicy(new DCSwitchRoundRobinPolicy());
        loadBalancingPolicy.setLoaclDC(currentDC);
        SessionManager.Builder builder = SessionManager.builder()
                .withCredentials(username, password)
                .withContactPoints(contactPoints)
                .withReplication(replication);
                //.withLoadBalancingPolicy(loadBalancingPolicy);

        SessionManager sessionManager = builder.build();
        String[] etcdhost = new String[1];
        etcdhost[0] = "http://192.168.3.9:2379";
        EtcdConfiguraion etcdConfiguraion = new EtcdConfiguraion();
        etcdConfiguraion.setEctdHosts(etcdhost);
        etcdConfiguraion.setServiceName("yftest");
        etcdConfiguraion.setClusterName("yftest");
        DCStatusWatcher dcStatusWatcher = new DCStatusWatcher(sessionManager.getSessionRepository(), etcdConfiguraion);
        dcStatusWatcher.init();
        service = new CassandraManageService(sessionManager.getSessionRepository());
        sessionRepository = sessionManager.getSessionRepository();
        initTable();
    }

    private void initTable(){
        KeyspaceMetadata keyspaceMetadata = sessionRepository.getCluster().getMetadata().getKeyspace(Utils.getKeyspace(appId));
        if ( keyspaceMetadata == null ) {
            service.createKeyspace(appId);
        }
        TableMetadata tableMetadata = sessionRepository.getCluster().getMetadata().getKeyspace(Utils.getKeyspace(appId)).getTable(tableName);
        if (tableMetadata == null){
            String[] pri = new String[3];
            pri[0] = "uid";
            pri[1] = "prim1";
            pri[2] = "prim2";

            Column[] columns = new Column[5];
            columns[0] = new Column("title", "");
            columns[1] = new Column("author", "");
            columns[2] = new Column("time", "");
            columns[3] = new Column("phone", "");
            columns[4] = new Column("email", "");
            TableOptions tableOptions = new TableOptions();
            tableOptions.setClusteringOrder("prim1 asc,prim2 desc");
            Result result = service.createTable(appId, tableName, columns, pri, null, tableOptions);
            if (result.isSuccess()){
                System.out.println("create table success");
            }
        }
    }
    protected abstract void test();

    public void execute() {
        for (int i = 0; i < threadNums; i++) {
            Thread thread1 = new Thread(new Tester(this,countDownLatch));
            thread1.setName("thread " + (i + 1));
            thread1.start();
        }
        try {
            countDownLatch.await();
            System.out.println("all thread running out");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onSuccess(long exeTime) {
        uniformReservoir.update(exeTime);
        decayingReservoir.update(exeTime);
        successTimes.incrementAndGet();
        //  uniformReservoir.getSnapshot().
    }

    public void onFailure() {
        failureTimes.incrementAndGet();
    }
}
