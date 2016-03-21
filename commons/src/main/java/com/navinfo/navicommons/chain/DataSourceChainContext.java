package com.navinfo.navicommons.chain;

import javax.sql.DataSource;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-4-3
 * Time: 下午9:42
 * To change this template use File | Settings | File Templates.
 */
public class DataSourceChainContext extends ChainContext{
    private DataSource controlDataSource ;
    private DataSource GDBDataSource ;

    public DataSource getControlDataSource() {
        return controlDataSource;
    }

    public void setControlDataSource(DataSource controlDataSource) {
        this.controlDataSource = controlDataSource;
    }

    public DataSource getGDBDataSource() {
        return GDBDataSource;
    }

    public void setGDBDataSource(DataSource GDBDataSource) {
        this.GDBDataSource = GDBDataSource;
    }
}
