package com.yufeiblog.cassandra.service;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.schemabuilder.Create;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.yufeiblog.cassandra.SessionRepository;
import com.yufeiblog.cassandra.common.*;
import com.yufeiblog.cassandra.exception.CSException;
import com.yufeiblog.cassandra.model.Column;
import com.yufeiblog.cassandra.model.ColumnOperation;
import com.yufeiblog.cassandra.model.Index;
import com.yufeiblog.cassandra.model.IndexOperation;
import com.yufeiblog.cassandra.result.*;
import com.yufeiblog.cassandra.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CassandraManageService extends BaseService implements ICassandraManageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraManageService.class);

    public CassandraManageService(SessionRepository sessionRepository) {
        super(sessionRepository);
    }

    @Override
    public Result createUser(String username, String password) {
        try {
            sessionRepository.getSession().execute(String.format(createUser, username, password));
            sessionRepository.getSession().execute(String.format(grantPermissionSelect, username));
            sessionRepository.getSession().execute(String.format(grantPermissionModify, username));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            //todo throw new Excetion
        }
        return null;
    }

    @Override
    public Result createKeyspace(int appId) {
        String keyspace = Utils.getKeyspace(appId);
        Metadata metadata = sessionRepository.getCluster().getMetadata();
        ResultSet resultSet = null;
        if (metadata.getKeyspace(keyspace) == null) {
            try {
                resultSet = sessionRepository.getSession().execute(SchemaBuilder.createKeyspace(keyspace)
                        .with()
                        .replication(sessionRepository.getReplication()
                        ));
            } catch (Exception e) {
                e.printStackTrace();
                throw new CSException();
            }
        }
        Result result = new Result();
        result.setReturnCode(ReturnCode.SUCCESS);
        return result;
    }

    @Override
    public Result createTable(int appId, String tableName, Column[] columns, String[] primaryKeys, Index[] indexes, TableOptions options) {
        String keyspace = Utils.getKeyspace(appId);
        Result result = null;
        if (isKeyspaceExist(appId) && isTableExist(appId, tableName)) {
            result = new Result();
            result.setReturnCode(ReturnCode.TABLE_EXIST);
            result.setReturnMsg("table is exist already");
            return result;
        }
        if (!isKeyspaceExist(appId)) {
            createKeyspace(appId);
        }

        Create create = SchemaBuilder.createTable(Utils.getKeyspace(appId), tableName);
        // create.withOptions().clusteringOrder(options.getClusteringOrder());
        String clusterOrder = options.getClusteringOrder();
        String[] orders = StringUtils.split(clusterOrder, ",");

        for (int i = 0; i < primaryKeys.length; i++) {
            if (i < options.getPartitionKeyCount()) {
                create.addPartitionKey(primaryKeys[i], DataType.varchar());
            } else {
                create.addClusteringColumn(primaryKeys[i], DataType.varchar());
            }
        }

        for (Column column : columns) {
            create.addColumn(column.getColumnName(), DataType.varchar());
        }

        Create.Options clusterOptions=create.withOptions();
        for (int i = 0; i < orders.length; i++) {
            String[] clusterKey = StringUtils.split(orders[i], " ");
            SchemaBuilder.Direction direction = SchemaBuilder.Direction.valueOf(clusterKey[1].toUpperCase());
            clusterOptions.clusteringOrder(clusterKey[0],direction);
        }

        ResultSet resultSet = sessionRepository.getSession().execute(clusterOptions);
        result = new Result();
        result.setReturnCode(ReturnCode.SUCCESS);

        return result;
    }

    private void withClusterOrder(Create create, TableOptions options) {
        //  if (options.getClusteringOrder())
    }

    @Override
    public DescribeTableResult describeTable(int appId, String tableName) {
        return null;
    }

    @Override
    public Result deleteTable(int appId, String tableName) {
        return null;
    }

    @Override
    public Result alterTable(int appId, String tableName, ColumnOperation[] columnOperations, IndexOperation[] indexOperations) {
        return null;
    }

    @Override
    public WriteResult insert(int appId, String tableName, Map<String, Object>[] records, UpdateOption updateOption) {
        return null;
    }

    @Override
    public WriteResult update(int appId, String tableName, Map<String, Object>[] records, UpdateOption updateOption) {
        return null;
    }

    @Override
    public WriteResult save(int appId, String tableName, Map<String, Object>[] records, UpdateOption updateOption) {
        String keyspace = Utils.getKeyspace(appId);
        Insert insert = null;
        ResultSet resultSet = null;
        for (Map<String, Object> record : records) {
            insert = QueryBuilder.insertInto(keyspace, tableName);
            String[] strings = record.keySet().toArray(new String[record.keySet().size()]);
            insert.values(strings, record.values().toArray(new Object[record.values().size()]));
            resultSet = sessionRepository.getSession(appId).execute(insert);
        }
        //Statement statement = prepareStatement(appId, sql);
        WriteResult writeResult = new WriteResult();
        return writeResult;
    }

    @Override
    public WriteResult delete(int appId, String tableName, Map<String, Object>[] records, UpdateOption updateOption) {
        return null;
    }

    @Override
    public GetResult get(int appId, String tableName, Map<String, Object>[] records, QueryOption queryOption) {
        return null;
    }

    @Override
    public FindResult find(int appId, String tableName, String[] columns, Condition[] conditions, String cursor, Integer limit, QueryOption options) {
        return null;
    }

    @Override
    public IncrResult incr(int appId, String tableName, Map<String, Object> keys, String fieldName, Object offset, UpdateOption updateOption) {
        return null;
    }

    @Override
    public Result deleteByPK(int appId, String tableName, Map<String, Object> pks, UpdateOption options) {
        return null;
    }
}



