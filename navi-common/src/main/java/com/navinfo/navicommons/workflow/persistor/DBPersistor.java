package com.navinfo.navicommons.workflow.persistor;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-9
 */
public class DBPersistor extends AbstractPersistor
{
    private JdbcTemplate jdbcTemplate;

    protected String doLoadFlowInstance(String instancePk) throws PersistorException
    {
        return null;  
    }

    protected void doSaveFlowInstance(String instancePk, String xml) throws PersistorException
    {
        
    }

    protected String doLoadDefine(String defPk) throws PersistorException
    {
        return null;  
    }

    protected void doSaveDefine(String defPk, String xml) throws PersistorException
    {
        
    }
}
