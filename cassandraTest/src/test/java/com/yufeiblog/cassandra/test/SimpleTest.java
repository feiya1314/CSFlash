package com.yufeiblog.cassandra.test;

import com.datastax.driver.core.Session;
import com.yufeiblog.cassandra.SessionManager;
import com.yufeiblog.cassandra.SessionRepository;
import com.yufeiblog.cassandra.result.FindResult;
import com.yufeiblog.cassandra.service.CassandraManageService;
import com.yufeiblog.cassandra.service.ICassandraManageService;

public class SimpleTest {

    public static void main(String[] args) {
        SessionManager.Builder builder = SessionManager.builder()
                .withCredentials("feiya1314", "feiya1314")
                .withContactPoints("192.168.3.8");
        //.withLoadBalancingPolicy()
        SessionManager sessionManager = builder.build();
        SessionRepository sessionRepository = sessionManager.getSessionRepository();
        ICassandraManageService service = new CassandraManageService(sessionRepository);
        Session session = sessionRepository.getSession();
        FindResult findResult = service.find(20190103,"yftest",null,null,null,10,null);
        System.out.println(findResult);
        sessionManager.close();
        sessionRepository.close();
    }
}
