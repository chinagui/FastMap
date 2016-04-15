package com.navinfo.dataservice.expcore.sql.handler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.dataservice.expcore.sql.ExpSQL;
import com.navinfo.dataservice.commons.database.oracle.ConnectionRegister;
import com.navinfo.navicommons.database.sql.DbLinkCreator;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;

/**
 * @author liuqing 此方法主要是创建索引，删除索引之用
 */
public class DDLExecThreadHandler extends ThreadHandler {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String dbLinkName;


    public DDLExecThreadHandler(CountDownLatch doneSignal,
                                ExpSQL sql,
                                DataSource ds, 
                                ThreadLocalContext ctx) {
        super(doneSignal, sql, ds, ctx);
    }

    public DDLExecThreadHandler(CountDownLatch doneSignal,
                                ExpSQL sql,
                                DataSource ds,
                                String dbLinkName,
                                ThreadLocalContext ctx) {
        super(doneSignal, sql, ds, ctx);
        this.dbLinkName = dbLinkName;
    }

    public void run() {

        execute(new ExpSqlProcessor() {

            public void process(ExpSQL sql) throws Exception {
                long t1 = System.currentTimeMillis();
                QueryRunner run = new QueryRunner();
                execSql = sql.getSql();

//                logger.debug(execSql);
                Connection conn = null;
                try {
                    conn=ConnectionRegister.subThreadGetConnection(ctx, dataSource);
                    run.execute(conn, execSql);
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    if (StringUtils.isNotBlank(dbLinkName)) {
                        DbLinkCreator creator = new DbLinkCreator();
                        creator.closeQuietly(dbLinkName, conn);
                    }
                    DbUtils.closeQuietly(conn);
                }

                
                long t2 = System.currentTimeMillis();
                long time = t2 - t1;
               // if (time > 1000)
                    logger.debug("[" + (time) + "ms] " + execSql);

            }
        });

    }


}
