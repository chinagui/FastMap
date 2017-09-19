package com.navinfo.dataservice.engine.limit.datahub.chooser.strategy;


import com.navinfo.dataservice.engine.limit.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.engine.limit.commons.database.navi.QueryRunner;
import com.navinfo.dataservice.engine.limit.datahub.exception.DataHubException;
import com.navinfo.dataservice.engine.limit.datahub.model.DbServer;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
	public DbServer getPriorDbServer(List<DbServer> dbServers,Map<String,Object> params)
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
	public DbServer getPriorDbServer(String bizType,Map<String, Object> params)
			throws DataHubException {
		Connection conn = null;
		try{
			String count_sql = "SELECT count(1) as c_num from db_server where biz_type like ?";
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			int c_num = run.queryForInt(conn, count_sql, "%"+bizType+"%");
			DbServer db = null;
			if(c_num>0){
//				int index = RandomUtils.nextInt(c_num);
//				String sql = "SELECT s.server_id,s.SERVER_IP,s.server_port,s.server_type FROM unified_db_server s where s.USE_TYPE like ? limit ?,1";
				int index = RandomUtils.nextInt(c_num);
				index++;
				String sql = "SELECT * FROM (SELECT s.server_id,s.SERVER_IP,s.server_port,s.server_type,S.SERVICE_NAME,ROWNUM AS RN FROM db_server s where s.biz_type like ?) WHERE RN=?";

				db = run.query(conn, sql,new ResultSetHandler<DbServer>(){

					@Override
					public DbServer handle(ResultSet rs) throws SQLException {
						DbServer inDb = null;
						if(rs.next()){
							String ip = rs.getString("SERVER_IP");
							int port = rs.getInt("SERVER_PORT");
							String type = rs.getString("SERVER_TYPE");
							inDb = new DbServer(type,ip,port);
							inDb.setSid(rs.getInt("SERVER_ID"));
							inDb.setServiceName(rs.getString("SERVICE_NAME"));
						}
						return inDb;
					}
					
				}, "%"+bizType+"%",index);
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
