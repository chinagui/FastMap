package com.navinfo.dataservice.engine.man.produce;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

public class ProduceOperation {
	private static Logger log = LoggerRepos.getLogger(ProduceOperation.class);

	public ProduceOperation() {
		// TODO Auto-generated constructor stub
	}
	
	public static int getNewProduceId(Connection conn) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			String querySql = "select PRODUCE_SEQ.NEXTVAL as produceId from dual";
			int produceId = Integer.valueOf(run
					.query(conn, querySql, new MapHandler()).get("produceId")
					.toString());
			return produceId;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}

	public static void updateProduceStatus(Connection conn,int produceId, int status) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
			String sql = "update produce set produce_status="+status+" where produce_Id="+produceId;			
			run.update(conn,sql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}

}
