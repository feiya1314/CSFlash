package com.yufeiblog.cassandra.service;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.yufeiblog.cassandra.SessionManager;
import com.yufeiblog.cassandra.SessionRepository;
import com.yufeiblog.cassandra.model.Column;
import com.yufeiblog.cassandra.model.TableDefination;
import com.yufeiblog.cassandra.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BaseService {
    protected String createUser = "CREATE USER %s WITH PASSWORD '%s' NOSUPERUSER";
    protected String grantPermissionSelect = "GRANT SELECT ON ALL KEYSPACES TO '%s'";
    protected String grantPermissionModify = "GRANT MODIFY ON ALL KEYSPACES TO '%s'";

    protected SessionRepository sessionRepository;


    public BaseService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }



    protected boolean isKeyspaceExist(int appId) {
        KeyspaceMetadata keyspaceMetadata = sessionRepository.getSession().getCluster().getMetadata().getKeyspace(Utils.getKeyspace(appId));
        return keyspaceMetadata != null;
    }

    protected boolean isTableExist(int appId, String tableName) {
        TableMetadata tableMetadata = sessionRepository.getSession().getCluster().getMetadata().getKeyspace(Utils.getKeyspace(appId)).getTable(tableName);
        return tableMetadata != null;
    }






}
