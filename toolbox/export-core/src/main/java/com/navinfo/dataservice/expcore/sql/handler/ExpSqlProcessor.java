package com.navinfo.dataservice.expcore.sql.handler;

import com.navinfo.dataservice.expcore.sql.ExpSQL;

/**
 * User: liuqing
 * Date: 2010-9-6
 * Time: 15:32:56
 */
public abstract class ExpSqlProcessor implements CallBackProcessor {
    protected String execSql;

    /**
     * 具体的执行DDL/DML/QUERY的动作
     *
     * @throws Exception
     */
    public abstract void process(ExpSQL sql) throws Exception;

    public String getExecSql() {
        return execSql; 
    }
}
