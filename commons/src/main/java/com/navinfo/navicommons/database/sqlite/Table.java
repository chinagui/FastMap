package com.navinfo.navicommons.database.sqlite;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-7-6
 * Time: 下午1:26
 */
public class Table {
    private String tableName;
    private List<String> columns;

    public Table(String tableName, List<String> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
}
