package com.navinfo.dataservice.engine.man.log;

import java.sql.Clob;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

public class ManLogOperation {
	private static Logger log = LoggerRepos.getLogger(ManLogOperation.class);
	
	public static void insertLog(Connection conn,ManLog manLog) throws Exception{
		try{
			List<Object> values = new ArrayList<Object>();

			String baseSql = "insert into man_log (id,short_desc,logs,operate_date) values(man_log_seq.nextval,?,?,sysdate)";
			QueryRunner run = new QueryRunner();
			
			values.add(manLog.getShortDesc());
			
			Clob clob=ConnectionUtil.createClob(conn);
			clob.setString(1, manLog.getLogs());
			values.add(clob);

			run.update(conn,baseSql,values.toArray());			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.warn("manlog 插入失败，"+e.getMessage());
		}
	}
	
	public static void insertLog(String shortDesc,String logs) throws Exception{
		Connection conn = null;
		try{
			conn=DBConnector.getInstance().getManConnection();
			List<Object> values = new ArrayList<Object>();

			String baseSql = "insert into man_log (id,short_desc,logs,operate_date) values (man_log_seq.nextval,?,?,sysdate)";
			QueryRunner run = new QueryRunner();
			
			values.add(shortDesc);
			
			Clob clob=ConnectionUtil.createClob(conn);
			clob.setString(1, logs);
			values.add(clob);

			run.update(conn,baseSql,values.toArray());			
		}catch(Exception e){
			log.warn("manlog 插入失败，"+e.getMessage());
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
