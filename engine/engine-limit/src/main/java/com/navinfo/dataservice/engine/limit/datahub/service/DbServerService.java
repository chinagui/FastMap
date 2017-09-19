package com.navinfo.dataservice.engine.limit.datahub.service;


import com.navinfo.dataservice.engine.limit.datahub.model.DbServer;
import com.navinfo.dataservice.engine.limit.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.engine.limit.datahub.exception.DataHubException;
import com.navinfo.dataservice.engine.limit.commons.database.navi.QueryRunner;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 
 * @ClassName: DbServerManager 
 * @author Xiao Xiaowen 
 * @date 2015-11-30 下午3:42:42 
 * @Description: TODO
 */
public class DbServerService {
	protected Logger log = Logger.getLogger(this.getClass());
	public List<DbServer> loadDbServers()throws DataHubException{
		Connection conn = null;
		try{
			//如果useType的set为空，则忽略该server
			String sql = "select s.SERVER_ID,s.SERVER_TYPE,s.SERVER_IP,s.SERVER_PORT,s.BIZ_TYPE,S.SERVICE_NAME from db_server s";
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			return run.query(conn, sql, new ResultSetHandler<List<DbServer>>(){

				@Override
				public List<DbServer> handle(ResultSet rs) throws SQLException {
					List<DbServer> sers = new ArrayList<DbServer>();
					while(rs.next()){
						DbServer ser = new DbServer(rs.getString("SERVER_TYPE"),rs.getString("SERVER_IP"),rs.getInt("SERVER_PORT"));
						ser.setSid(rs.getInt("SERVER_ID"));
						ser.setServiceName(rs.getString("SERVICE_NAME"));
						String bizTypes = rs.getString("BIZ_TYPE");
						if(StringUtils.isNotEmpty(bizTypes)){
							Set<String> bizSet = new HashSet<String>();
							CollectionUtils.addAll(bizSet, bizTypes.split(","));
							ser.setBizType(bizSet);
							sers.add(ser);
						}else{
							log.warn("**********注意**********");
							log.warn("db_server表中加载use_type字段配置错误的记录，已忽略，请立即检查。");
						}
					}
					return sers;
				}
				
			});
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("查询Db Server列表出错，原因："+e.getMessage(),e);
			throw new DataHubException("从管理库中查询Db Server列表出错，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void addDbServer(DbServer dbServer)throws DataHubException{
		
	}
	public void updateDbServer(DbServer dbServer)throws DataHubException{
		
	}
}
