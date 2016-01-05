package com.navinfo.dataservice.datahub.chooser.strategy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;


import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.dataservice.datahub.model.DbServer;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: RandomStrategy 
 * @author Xiao Xiaowen 
 * @date 2015-12-1 上午10:10:59 
 * @Description: TODO
 */
public class RandomStrategy extends DbServerStrategy {
	protected Logger log = Logger.getLogger(this.getClass());

	public RandomStrategy(AbstractStrategyLock strategyLock){
		super(strategyLock);
	}
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.datahub.chooser.strategy.DbServerStrategy#getPriorDbServer(java.util.Set)
	 */
	@Override
	public DbServer getPriorDbServer(List<DbServer> dbServers,Map<String,String> params)
			throws DataHubException {
		if(dbServers.size()>1){
			int index = RandomUtils.nextInt(dbServers.size());
			//todo
			return dbServers.get(index);
		}else{
			return dbServers.get(0);
		}
	}
	@Override
	public DbServer getPriorDbServer(String useType,Map<String, String> params)
			throws DataHubException {
		Connection conn = null;
		try{
			String count_sql = "SELECT count(1) as c_num from unified_db_server where USE_TYPE like ?";
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
			int c_num = run.queryForInt(conn, count_sql, "%"+useType+"%");
			DbServer db = null;
			if(c_num>0){
//				int index = RandomUtils.nextInt(c_num);
//				String sql = "SELECT s.server_id,s.SERVER_IP,s.server_port,s.server_type FROM unified_db_server s where s.USE_TYPE like ? limit ?,1";
				int index = RandomUtils.nextInt(c_num);
				index++;
				String sql = "SELECT * FROM (SELECT s.server_id,s.SERVER_IP,s.server_port,s.service_name,s.server_type,ROWNUM AS RN FROM unified_db_server s where s.USE_TYPE like ?) WHERE RN=?";

				db = run.query(conn, sql,new ResultSetHandler<DbServer>(){

					@Override
					public DbServer handle(ResultSet rs) throws SQLException {
						DbServer inDb = null;
						if(rs.next()){
							String ip = rs.getString("SERVER_IP");
							int port = rs.getInt("SERVER_PORT");
							String type = rs.getString("SERVER_TYPE");
							String sname = rs.getString("SERVICE_NAME");
							inDb = new DbServer(type,ip,port,sname);
							inDb.setSid(rs.getInt("SERVER_ID"));
						}
						return inDb;
					}
					
				}, "%"+useType+"%",index);
			}
			return db;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new DataHubException("查询服务器出现错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
