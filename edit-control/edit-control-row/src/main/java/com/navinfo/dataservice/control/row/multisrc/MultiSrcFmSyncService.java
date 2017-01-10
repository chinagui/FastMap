package com.navinfo.dataservice.control.row.multisrc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.MultiSrcFmSync;
import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONObject;

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
			String zipFile = obj.getZipFile();
			String sql = "INSERT INTO MULTISRC_FM_SYNC (SID,SYNC_STATUS,SYNC_TIME,JOB_ID,ZIP_FILE,DB_TYPE) "
					+ "VALUES (MULTISRC_FM_SYNC_SEQ.NEXTVAL,1,SYSDATE,?,?,?)";
			Object[] params = {jobId,zipFile,dbType};
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
			String sql = "UPDATE MULTISRC_FM_SYNC SET SYNC_STATUS=? WHERE JOB_ID =?";
			//日志
			log.info("更新MULTISRC_FM_SYNC表的管理记录:"+sql);
			
			queryRunner.update(conn,sql,obj.getSyncStatus(),obj.getJobId());
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
	public MultiSrcFmSync queryLastSuccessSync() throws ServiceException{
		List<MultiSrcFmSync> msgs = null;
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			String sql = "SELECT * FROM MULTISRC_FM_SYNC WHERE SYNC_STATUS IN(11) ORDER BY SYNC_TIME DESC";
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
				msg.setSyncStatus(rs.getInt("SYNC_STATUS"));
				msg.setSyncTime(rs.getTimestamp("SYNC_TIME"));
				msg.setJobId(rs.getLong("JOB_ID"));
				msg.setZipFile(rs.getString("ZIP_FILE"));
				msg.setDbType(rs.getInt("DB_TYPE"));
				msgs.add(msg);
			}
			return msgs;
		}
	}
	
	/**
	 * 日库多源数据包导入FM申请
	 * @param userId 
	 * @param zipUrl
	 * @return:jobId
	 * @throws Exception
	 */
	public String applyUploadDay(long userId, String zipUrl)throws Exception{
		Connection conn = null;
		try {
			//判断是否有未执行完的导入任务
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			String sql = "SELECT * FROM MULTISRC_FM_SYNC WHERE SYNC_STATUS IN(1,2,3,5,7) ORDER BY SYNC_TIME DESC";
			Object[] params = {};
			List<MultiSrcFmSync> list = querySync(conn, sql, params);
			if(list != null && list.size()>0){
				//有未执行完的导入任务
				throw new Exception("申请失败:有未执行完成的日库多源数据包导入FM任务");
			}
			//判断是否为已经导入成功的多源增量包
			MultiSrcFmSync multiSrcFmSync = queryLastSuccessSync();
			String successZipUrl = multiSrcFmSync.getZipFile();
			String zipFile = StringUtils.substringAfterLast(successZipUrl, "/");
			String newZipFile = StringUtils.substringAfterLast(zipUrl, "/");
			if(newZipFile.equals(zipFile)){
				throw new Exception("申请失败:日库多源数据包已经导入FM日库,不能重复导入");
			}
			JSONObject job = new JSONObject();
			JobApi jobApi = (JobApi) ApplicationContextUtil.getBean("jobApi");
			job.put("remoteZipFile", zipUrl);
			//创建job任务,获取jobId
			long jobId = jobApi.createJob("multisrc2FmDay", job, 0, 0,"创建多源日库增量包导入FM");
			//创建管理记录
			MultiSrcFmSync obj = new MultiSrcFmSync();
			obj.setJobId(jobId);
			obj.setDbType(1);
			obj.setZipFile(zipUrl);
			insert(obj);
			return "FM已开始导入";
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("申请失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
