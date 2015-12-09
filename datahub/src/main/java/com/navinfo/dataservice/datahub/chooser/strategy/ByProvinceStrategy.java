package com.navinfo.dataservice.datahub.chooser.strategy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.datahub.DbServerMonitor;
import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.dataservice.datahub.model.DbServer;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: ByProvinceStrategy 
 * @author Xiao Xiaowen 
 * @date 2015-12-1 上午10:07:19 
 * @Description: 工厂创建，也是单例
 */
public class ByProvinceStrategy extends DbServerStrategy implements Observer{
	protected Logger log = Logger.getLogger(this.getClass());

	public ByProvinceStrategy(AbstractStrategyLock strategyLock){
		super(strategyLock);
		DbServerMonitor.getInstance().addObserver(this);
		loadServerProvinceMap();
	}
	private Map<String,Set<String>> serverProvinceMap = new HashMap<String,Set<String>>();
	private void loadServerProvinceMap(){
		serverProvinceMap.clear();
		//Connection conn = null;
		//SELECT s.SERVER_IP,s.server_port,s.server_type,p.province_cn FROM unified_db_server s,db_server_province p WHERE s.server_id=p.DB_SERVER_ID;
		//todo
	}
	@Override
	public DbServer getPriorDbServer(String useType,Map<String,String> params)
			throws DataHubException {
		if(params==null||StringUtils.isEmpty(params.get("provinceCode"))){
			throw new DataHubException("必须传入省份名称，否则无法选择服务器。");
		}
		//以后可以实现从serverProvinceMap中取
		Connection conn = null;
		try{
			String sql = "SELECT s.server_id,s.SERVER_IP,s.server_port,s.server_type,p.province_code FROM unified_db_server s,db_server_province p WHERE s.server_id=p.DB_SERVER_ID and s.use_type like ? and p.province_code=?";
			QueryRunner run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
			DbServer db = run.query(conn, sql,new ResultSetHandler<DbServer>(){

				@Override
				public DbServer handle(ResultSet rs) throws SQLException {
					DbServer inDb = null;
					if(rs.next()){
						String ip = rs.getString("SERVER_IP");
						String port = rs.getString("SERVER_PORT");
						String type = rs.getString("SERVER_TYPE");
						inDb = new DbServer(ip,port,type);
						inDb.setSid(rs.getInt("SERVER_ID"));
					}
					return inDb;
				}
				
			}, "%"+useType+"%",params.get("provinceCode"));
			return db;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new DataHubException("查询省份和服务器的映射表出现错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.datahub.chooser.strategy.DbServerStrategy#getPriorDbServer(java.util.Set)
	 */
	@Override
	public DbServer getPriorDbServer(List<DbServer> dbServers,Map<String,String> params)
			throws DataHubException {
		if(params==null||StringUtils.isEmpty(params.get("provinceCode"))){
			throw new DataHubException("必须传入省份名称，否则无法选择服务器。");
		}
		if(dbServers.size()>1){
			//以后可以实现从serverProvinceMap中取
			return null;
		}else{
			return dbServers.get(0);
		}
	}
	

	@Override
	public void update(Observable o, Object arg){
		try{
			loadServerProvinceMap();
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}

}
