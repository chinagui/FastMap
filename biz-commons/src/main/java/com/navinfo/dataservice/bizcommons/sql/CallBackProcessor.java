package com.navinfo.dataservice.bizcommons.sql;

/**
 * User: liuqing
 * Date: 2010-9-3
 * Time: 16:27:39
 */
public interface CallBackProcessor {
    /**
     * 具体的执行DDL/DML/QUERY的动作
     *
     * @throws Exception
     */
    public void process(ExpSQL sql) throws Exception;

    public String getExecSql();
}
