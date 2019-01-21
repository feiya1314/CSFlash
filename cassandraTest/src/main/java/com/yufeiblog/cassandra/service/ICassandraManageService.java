package com.yufeiblog.cassandra.service;

import com.yufeiblog.cassandra.common.Condition;
import com.yufeiblog.cassandra.common.QueryOption;
import com.yufeiblog.cassandra.common.TableOptions;
import com.yufeiblog.cassandra.common.UpdateOption;
import com.yufeiblog.cassandra.model.Column;
import com.yufeiblog.cassandra.model.ColumnOperation;
import com.yufeiblog.cassandra.model.Index;
import com.yufeiblog.cassandra.model.IndexOperation;
import com.yufeiblog.cassandra.result.*;

import java.util.Map;

public interface ICassandraManageService {
    /**
     * 创建Cassandra用户
     *
     * @param username 用户名
     * @param password 密码
     * @return 创建用户的结果
     */
    Result createUser(String username, String password);

    /**
     * 创建keyspace
     * @param appId appid
     * @return 创建结果
     */
    Result createKeyspace(int appId);

    /**
     * 创建表
     * @param appId  业务appid
     * @param tableName 业务表名
     * @param columns 字段
     * @param primaryKeys 主键
     * @param indexes 索引
     * @param options 其他选项
     * @return 创建结果
     */
    Result createTable(int appId, String tableName, Column[] columns, String[] primaryKeys, Index[] indexes, TableOptions options);

    /**
     * 查看表信息
     * @param appId 业务appid
     * @param tableName 表名
     * @return 表信息
     */
    DescribeTableResult describeTable(int appId, String tableName);

    /**
     * 删除表
     * @param appId 业务appId
     * @param tableName 表名
     * @return 删除结果
     */
    Result deleteTable(int appId, String tableName);

    /**
     * 修改表结构
     * @param appId 业务appid
     * @param tableName 表名
     * @param columnOperations 字段操作
     * @param indexOperations 索引操作
     * @return 修改结果
     */
    Result alterTable(int appId, String tableName, ColumnOperation[] columnOperations, IndexOperation[] indexOperations);

    /**
     * 写入数据
     * @param appId 业务appid
     * @param tableName 表名
     * @param records 要写入的数据
     * @param updateOption 选项
     * @return 写结果
     */
    WriteResult insert(int appId, String tableName, Map<String, Object>[] records, UpdateOption updateOption);

    /**
     * 更新数据
     * @param appId 业务appid
     * @param tableName 表名
     * @param records 更新的数据
     * @param updateOption 选项
     * @return 更新结果
     */
    WriteResult update(int appId, String tableName, Map<String, Object>[] records, UpdateOption updateOption);

    /**
     * 新增或者更新数据
     * @param appId appid
     * @param tableName 表名
     * @param records 写入的数据
     * @param updateOption 选项
     * @return 操作结果
     */
    WriteResult save(int appId, String tableName, Map<String, Object>[] records, UpdateOption updateOption);

    /**
     * 删除数据
     * @param appId 业务appid
     * @param tableName 表名
     * @param records 删除的数据
     * @param updateOption 选项
     * @return 删除的结果
     */
    WriteResult delete(int appId, String tableName, Map<String, Object>[] records, UpdateOption updateOption);

    /**
     * 查询数据
     * @param appId appid
     * @param tableName 业务表名
     * @param records 要查询的数据
     * @param queryOption 查询条件
     * @return 查询结果
     */
    GetResult get(int appId, String tableName, Map<String, Object>[] records, QueryOption queryOption);

    /**
     * 分页查询
     * @param appId 业务appid
     * @param tableName 业务表名
     * @param columns 要查询的字段
     * @param conditions 查询条件
     * @param cursor 游标
     * @param limit 查询数据量
     * @param options 查询选项
     * @return 查询结果
     */
    FindResult find(int appId, String tableName, String[] columns, Condition[] conditions, String cursor, Integer limit, QueryOption options);

    /**
     *
     * @param appId
     * @param tableName
     * @param keys
     * @param fieldName
     * @param offset
     * @param updateOption
     * @return
     */
    IncrResult incr(int appId, String tableName, Map<String, Object> keys, String fieldName, Object offset, UpdateOption updateOption);

    /**
     * 根据PK 删除
     * @param appId 业务appid
     * @param tableName 业务表名
     * @param pks 分区主键
     * @param options 选项
     * @return 删除结果
     */
    Result deleteByPK(int appId, String tableName, Map<String, Object> pks, UpdateOption options);
}
