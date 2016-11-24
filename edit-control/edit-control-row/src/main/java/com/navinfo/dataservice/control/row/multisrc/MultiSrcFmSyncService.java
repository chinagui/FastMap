package com.navinfo.dataservice.control.row.multisrc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.MultiSrcFmSync;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/**
 * 
 * @ClassName MultiSrcFmSyncService
 * @author Han Shaoming
 * @date 2016年11月18日 下午3:43:45
 * @Description TODO
 */
public class MultiSrcFmSyncService {
	protected Logger log = Logger.getLogger(this.getClass());
	
	private volatile static MultiSrcFmSyncService instance=null;
	public static MultiSrcFmSyncService getInstance(){
		if(instance==null){
			synchronized(MultiSrcFmSyncService.class){
				if(instance==null){
					instance=new MultiSrcFmSyncService();
				}
			}
		}
		return instance;
	}
	private MultiSrcFmSyncService(){}
	
	
	
	public String insert(MultiSrcFmSync obj)throws Exception{
		Connection conn = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			//处理数据
			//jobId
			long jobId = obj.getJobId();
			long dbType = obj.getDbType();
			
			String sql = "INSERT INTO MULTISRC_FM_SYNC (SID,SYNC_STATUS,SYNC_TIME,JOB_ID,ZIP_FILE,DB_TYPE) "
					+ "VALUES (MULTISRC_FM_SYNC_SEQ.NEXTVAL,1,SYSDATE,?,NULL,?)";
			Object[] params = {jobId,dbType};
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
	public void updateSync(MultiSrcFmSync obj) throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner queryRunner = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE MULTISRC_FM_SYNC SET ");
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
			log.info("更新MULTISRC_FM_SYNC表的管理记录:"+querySql);
			
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
	 * 查询数据
	 * @author Han Shaoming
	 * @param conn
	 * @param sql
	 * @param params2 
	 * @return
	 * @throws ServiceException
	 */
	public List<MultiSrcFmSync> querySync(Connection conn,String sql, Object[] params) throws ServiceException{
		List<MultiSrcFmSync> msgs = null;
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
	class MultiRowHandler implements ResultSetHandler<List<MultiSrcFmSync>>{
		
		public List<MultiSrcFmSync> handle(ResultSet rs) throws SQLException {
			List<MultiSrcFmSync> msgs = new ArrayList<MultiSrcFmSync>();
			while(rs.next()){
				MultiSrcFmSync msg = new MultiSrcFmSync();
				msg.setSid(rs.getLong("SID"));
				msg.setSyncStatus(rs.getLong("SYNC_STATUS"));
				msg.setSyncTime(rs.getTimestamp("SYNC_TIME"));
				msg.setJobId(rs.getLong("JOB_ID"));
				msg.setZipFile(rs.getString("ZIP_FILE"));
				msg.setDbType(rs.getLong("DB_TYPE"));
				msgs.add(msg);
			}
			return msgs;
		}
	}
	
	/**
	 * @param zipUrl
	 * @return:jobId
	 * @throws Exception
	 */
	public int applyUploadDay(String zipUrl)throws Exception{
		//创建ms->fm day的job,获取jobId
		//insert
		return 0;
	}

}
