package com.navinfo.dataservice.control.row.multisrc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.FmMultiSrcSync;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/** 
 * @ClassName: FmMultiSrcSyncService
 * @author xiaoxiaowen4127
 * @date 2016年11月15日
 * @Description: FmMultiSrcSyncService.java
 */
public class FmMultiSrcSyncService {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	private volatile static FmMultiSrcSyncService instance;
	public static FmMultiSrcSyncService getInstance(){
		if(instance==null){
			synchronized(FmMultiSrcSyncService.class){
				if(instance==null){
					instance=new FmMultiSrcSyncService();
				}
			}
		}
		return instance;
	}
	private FmMultiSrcSyncService(){}

	/**
	 * 创建管理记录
	 * @author Han Shaoming
	 * @param obj
	 * @throws ServiceException 
	 */
	public String insert(FmMultiSrcSync obj) throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			//判断今天是否已经同步成功过POI增量数据
			String querySql = "SELECT * FROM FM_MULTISRC_SYNC WHERE SYNC_STATUS=18 AND TO_CHAR(SYNC_TIME,'yyyyMMdd')="
					+ "TO_CHAR(SYSDATE,'yyyyMMdd') ORDER BY SYNC_TIME DESC";
			Object[] queryParams = {};
			List<FmMultiSrcSync> querySync = this.querySync(conn, querySql, queryParams);
			if(querySync != null && querySync.size()>0){
				//当天已经成功向多源同步过POI数据
				return "今天已经向多源同步过POI增量,不能重复同步!";
			}
			
			//处理数据
			//查询最近一次同步时间
			FmMultiSrcSync fmMultiSrcSync = this.queryLastSuccessSync();
			Date lastSyncTime = null;
			if(fmMultiSrcSync != null){
				lastSyncTime = (Date) fmMultiSrcSync.getLastSyncTime();
				
				//日志
				log.info("FM同步到多源最近一次成功数据:"+fmMultiSrcSync.toString());
			}
			//jobId
			long jobId = obj.getJobId();
			
			String sql = "INSERT INTO FM_MULTISRC_SYNC (SID,SYNC_STATUS,LAST_SYNC_TIME,SYNC_TIME,JOB_ID,ZIP_FILE) "
					+ "VALUES (FM_MULTISRC_SYNC_SEQ.NEXTVAL,1, ?, SYSDATE,?,NULL)";
			Object[] params = {lastSyncTime,jobId};
			queryRunner.update(conn, sql, params);
			return "创建管理记录成功";
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 更新管理记录
	 * @author Han Shaoming
	 * @param obj
	 * @throws ServiceException 
	 */
	public void updateSync(FmMultiSrcSync obj) throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE FM_MULTISRC_SYNC SET ");
			if(obj.getSyncStatus() != null){
				//更新管理状态
				long syncStatus = obj.getSyncStatus();
				sql.append(" SYNC_STATUS="+syncStatus);
			}
			if(obj.getZipFile() != null){
				if(obj.getSyncStatus() != null){
					sql.append(",");
				}
				//更新增量包路径
				String zipFile = obj.getZipFile();
				sql.append(" ZIP_FILE='"+zipFile+"' ");
			}
			sql.append(" WHERE TO_CHAR(SYNC_TIME,'yyyyMMdd')=TO_CHAR(SYSDATE,'yyyyMMdd')");
			String querySql = sql.toString();
			
			//日志
			log.info("更新FM_MULTISRC_SYNC表的管理记录:"+querySql);
			
			Object[] params = {};
			queryRunner.update(conn,querySql,params);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 查询最新同步成功的数据
	 * @author Han Shaoming
	 * @return
	 * @throws ServiceException 
	 */
	public FmMultiSrcSync queryLastSuccessSync() throws ServiceException{
		List<FmMultiSrcSync> msgs = null;
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			String sql = "SELECT * FROM FM_MULTISRC_SYNC WHERE SYNC_STATUS=18 ORDER BY SYNC_TIME DESC";
			Object[] params = {};
			msgs = this.querySync(conn, sql,params);
			if(msgs != null && msgs.size()>0){
				return msgs.get(0);
			}else{
				return null;
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 查询数据
	 * @author Han Shaoming
	 * @param conn
	 * @param sql
	 * @param params2 
	 * @return
	 * @throws ServiceException
	 */
	public List<FmMultiSrcSync> querySync(Connection conn,String sql, Object[] params) throws ServiceException{
		List<FmMultiSrcSync> msgs = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			msgs = queryRunner.query(conn, sql, new MultiRowHandler(),params);
			return msgs;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 
	 * @ClassName MultiRowHandler
	 * @author Han Shaoming
	 * @date 2016年11月18日 下午1:55:24
	 * @Description TODO
	 */
	class MultiRowHandler implements ResultSetHandler<List<FmMultiSrcSync>>{
		
		public List<FmMultiSrcSync> handle(ResultSet rs) throws SQLException {
			List<FmMultiSrcSync> msgs = new ArrayList<FmMultiSrcSync>();
			while(rs.next()){
				FmMultiSrcSync msg = new FmMultiSrcSync();
				msg.setSid(rs.getLong("SID"));
				msg.setSyncStatus(rs.getLong("SYNC_STATUS"));
				msg.setSyncTime(rs.getTimestamp("SYNC_TIME"));
				msg.setLastSyncTime(rs.getTimestamp("LAST_SYNC_TIME"));
				msg.setJobId(rs.getLong("JOB_ID"));
				msg.setZipFile(rs.getString("ZIP_FILE"));
				msgs.add(msg);
			}
			return msgs;
		}
	}
}
