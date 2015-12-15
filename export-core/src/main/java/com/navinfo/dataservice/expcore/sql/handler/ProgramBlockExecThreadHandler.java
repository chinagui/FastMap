package com.navinfo.dataservice.expcore.sql.handler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.dataservice.expcore.sql.ExpSQL;
import com.navinfo.dataservice.commons.database.oracle.ConnectionRegister;
import com.navinfo.navicommons.database.sql.DbLinkCreator;
import com.navinfo.navicommons.database.sql.ProcedureBase;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;

@SuppressWarnings("serial")
public class ProgramBlockExecThreadHandler extends ThreadHandler {

	public ProgramBlockExecThreadHandler(CountDownLatch doneSignal, ExpSQL sql,
			DataSource dataSource, ThreadLocalContext ctx) {
		super(doneSignal, sql, dataSource, ctx);
	}


	@Override
	public void run() {
		execute(new ExpSqlProcessor() {

            public void process(ExpSQL sql) throws Exception {
                long t1 = System.currentTimeMillis();
                QueryRunner run = new QueryRunner();
                execSql = sql.getSql()+";";

//                logger.debug(execSql);
                Connection conn = null;
                try {
                	
                    conn=ConnectionRegister.subThreadGetConnection(ctx, dataSource);
                    String schemaName = conn.getMetaData().getUserName();
                    ProcedureBase procedureBase = new ProcedureBase(conn);
                    procedureBase.callProcedure(execSql.replaceAll("\\[schema_name\\]", "'"+schemaName+"'"));
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                   /* if (StringUtils.isNotBlank(dbLinkName)) {
                        DbLinkCreator creator = new DbLinkCreator();
                        creator.closeQuietly(dbLinkName, conn);
                    }*/
                    DbUtils.closeQuietly(conn);
                }

                
                long t2 = System.currentTimeMillis();
                long time = t2 - t1;
               // if (time > 1000)
                    logger.debug("[" + (time) + "ms] " + execSql);

            }
        });

	}
	public static void main(String[] args){
		System.out.println("BEGIN  DBMS_STATS.gather_table_stats([schema_name],'TEMP_IX_POI'); END;".replaceAll("\\[schema_name\\]", "'aa'"));
	}
}
