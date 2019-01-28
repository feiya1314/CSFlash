package com.yufeiblog.cassandra.common.builder;

import com.datastax.driver.core.*;
import com.yufeiblog.cassandra.SessionRepository;
import com.yufeiblog.cassandra.common.Condition;
import com.yufeiblog.cassandra.common.Cursor;
import com.yufeiblog.cassandra.common.PagingStateCursor;
import com.yufeiblog.cassandra.model.TableDefination;
import com.yufeiblog.cassandra.utils.Utils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class FindResultBuilder {
    private int appId;
    private String tableName;
    private SessionRepository sessionRepository;
    private String prefixSql;
    private String[] columns;
    private String sqlString;
    private PagingState pagingState;
    private Condition[] conditions;
    private Object[] values;
    private int limit;

    public FindResultBuilder(int appId, String tableName, SessionRepository sessionRepository) {
        this.appId = appId;
        this.tableName = tableName;
        this.sessionRepository = sessionRepository;
    }

    public FindResultBuilder withColumns(String[] columns) {
        this.columns = columns;
        preparePrefix();
        return this;
    }

    public FindResultBuilder withCondition(Condition[] conditions) {
        this.conditions = conditions;
        if (Utils.isArrayEmpty(conditions)) {
            return this;
        }
        values = new Object[conditions.length];
        TableDefination tableDefination = sessionRepository.getTableDefination(appId,tableName);
        String[] pks = tableDefination.getPrimaryKeys();
        StringBuilder sb = new StringBuilder();
        sb.append(prefixSql).append(" WHERE ");
        int i = 0;
        for (Condition condition : conditions) {
            String column = condition.getColumnName();
            int indexOfPk = ArrayUtils.indexOf(pks,column);
            if (indexOfPk >= tableDefination.getPartitionKeyCount()){
                sb.append('C').append(column).append(')').append(condition.getOperator()).append("(?)").append(" AND ");
            }else {
                sb.append(column).append(condition.getOperator()).append("?").append(" AND ");
            }
            values[i++] = condition.getValue();
        }
        sb.delete(sb.length() - 5, sb.length());
        sqlString = sb.toString();
        return this;
    }

    public FindResultBuilder withCursor(String cursor, Integer limit) {
        this.limit = limit;
        Cursor pagingState = Cursor.convertToCursor(cursor);
        PagingState csPagingState = null;
        if (pagingState instanceof PagingStateCursor) {
            csPagingState = ((PagingStateCursor) pagingState).getPagingState();
        }
        this.pagingState = csPagingState;
        return this;
    }

    public List<Map<String, Object>> find() {
        PreparedStatement preparedStatement = sessionRepository.prepareStatement(appId, sqlString);
        BoundStatement boundStatement = new BoundStatement(preparedStatement);
        boundStatement.bind(values);
        boundStatement.setFetchSize(limit);
        ResultSet resultSet = sessionRepository.getSession(appId).execute(boundStatement);
        pagingState = resultSet.getExecutionInfo().getPagingState();
        return getResults(resultSet);
    }

    public String getCursor(){
        PagingStateCursor cursor = new PagingStateCursor();
        cursor.setPagingState(pagingState);
        return cursor.cursorToString();
    }
    private List<Map<String, Object>> getResults(ResultSet resultSet) {
        List<Map<String, Object>> results = new ArrayList<>();
        Iterator<Row> iterator = resultSet.iterator();
        int i = 1;
        while (iterator.hasNext()) {
            if (i > limit) {
                break;
            }
            Row row = iterator.next();
            Map<String,Object> result = new HashMap<>();
            for(String str:columns){
                Object obj=row.getObject(str);
                result.put(str,obj);
            }
            results.add(result);
            i++;
        }
        return results;
    }

    private void preparePrefix() {
        StringBuilder stringBuilder = new StringBuilder();
        String columnStr = StringUtils.join(columns, ",");
        stringBuilder.append("SELECT ").append(columnStr)
                .append(" from ")
                .append(Utils.getKeyspace(appId))
                .append(".")
                .append(tableName);
        prefixSql = stringBuilder.toString();
    }

    public static void main(String[] args) {
        String[] ser = new String[]{"a", "b", "c", "d"};
        String str = StringUtils.join(ser, ",");
        System.out.println(str);
    }
}
