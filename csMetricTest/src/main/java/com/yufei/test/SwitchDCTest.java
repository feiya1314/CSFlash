package com.yufei.test;

import com.yufeiblog.cassandra.common.TableOptions;
import com.yufeiblog.cassandra.model.Column;
import com.yufeiblog.cassandra.result.Result;

public class SwitchDCTest extends BaseTest {

    @Override
    public void test() {
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
        System.out.println("");
        System.exit(0);
    }

    /*public static void main(String[] args) {
        new SwitchDCTest().run();
    }*/
}
