package com.yufei.test;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.UniformReservoir;
import com.yufeiblog.cassandra.SessionManager;
import com.yufeiblog.cassandra.common.EtcdConfiguraion;
import com.yufeiblog.cassandra.dcmonitor.DCStatusWatcher;
import com.yufeiblog.cassandra.loadbalance.DCSwitchRoundRobinPolicy;
import com.yufeiblog.cassandra.loadbalance.SimpleDCSwitchPolicy;
import com.yufeiblog.cassandra.loadbalance.SwitchLoadbalancePolicy;
import com.yufeiblog.cassandra.service.CassandraManageService;
import com.yufeiblog.cassandra.service.ICassandraManageService;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

abstract class BaseTest implements Runnable {

    protected int appId = 20190103;
    protected String replication = "{\"class\": \"NetworkTopologyStrategy\",\"DC1\": \"3\",\"DC2\": \"3\"}";
    protected String tableName = "yftest88";
    protected String username = "cassandra";
    protected String password = "cassandra";
    protected String contactPoints = "192.168.3.8";
    protected ICassandraManageService service;
    protected int startUid = 1;
    protected AtomicInteger uid = new AtomicInteger(startUid);
    protected int recordSize = 5;
    protected int lastingTime = 60 * 1000;
    protected volatile boolean keepRunning = true;
    protected int threadNums = 2;
    protected CountDownLatch countDownLatch = new CountDownLatch(threadNums);
    protected String currentDC="DC1";
    private AtomicInteger successTimes=new AtomicInteger(0);
    private AtomicInteger failureTimes=new AtomicInteger(0);
    private ExponentiallyDecayingReservoir decayingReservoir = new ExponentiallyDecayingReservoir();
    private UniformReservoir uniformReservoir = new UniformReservoir();
    private Histogram histogram = new Histogram(uniformReservoir);

    public BaseTest() {
        init();
    }

    @Override
    public void run() {

        if (lastingTime > 0) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    keepRunning = false;
                    timer.cancel();
                }
            }, lastingTime);
        }

        while (keepRunning) {
            test();
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(Thread.currentThread().getName() + " stop running");
        countDownLatch.countDown();
    }

    private void init() {
        SwitchLoadbalancePolicy loadBalancingPolicy =  new SimpleDCSwitchPolicy(new DCSwitchRoundRobinPolicy());
        loadBalancingPolicy.setLoaclDC(currentDC);
        SessionManager.Builder builder = SessionManager.builder()
                .withCredentials(username, password)
                .withContactPoints(contactPoints)
                .withReplication(replication)
                .withLoadBalancingPolicy(loadBalancingPolicy);

        SessionManager sessionManager = builder.build();
        String[] etcdhost = new String[1];
        etcdhost[0] = "http://192.168.3.9:2379";
        EtcdConfiguraion etcdConfiguraion = new EtcdConfiguraion();
        etcdConfiguraion.setEctdHosts(etcdhost);
        etcdConfiguraion.setServiceName("yftest");
        etcdConfiguraion.setClusterName("yftest");
        DCStatusWatcher dcStatusWatcher = new DCStatusWatcher(sessionManager.getSessionRepository(),etcdConfiguraion);
        dcStatusWatcher.init();
        service = new CassandraManageService(sessionManager.getSessionRepository());
    }

    protected abstract void test();

    public void execute() {
        for (int i = 0; i < threadNums; i++) {
            Thread thread1 = new Thread(this);
            thread1.setName("thread " + (i + 1));
            thread1.start();
        }
        try {
            countDownLatch.await();
            System.out.println("all thread running out");
            System.exit(0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public  void onSuccess(long exeTime){
        uniformReservoir.update(exeTime);
        decayingReservoir.update(exeTime);
        successTimes.incrementAndGet();
    }

    public void onFailure(){
        failureTimes.incrementAndGet();
    }
}
