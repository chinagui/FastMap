package com.navinfo.dataservice.engine.dropbox.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.dropbox.model.UploadInfo;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: UploadInfoService
 * @author xiaoxiaowen4127
 * @date 2017年4月24日
 * @Description: UploadInfoService.java
 */
public class UploadInfoService {
	private volatile static UploadInfoService instance;
	public static UploadInfoService getInstance(){
		if(instance==null){
			synchronized(UploadInfoService.class){
				if(instance==null){
					instance=new UploadInfoService();
				}
			}
		}
		return instance;
	}
	private UploadInfoService(){}
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	//
	public UploadInfo getByJobId(int jobId)throws Exception{
		
		Connection conn = null;
		try {
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();

			String sql = "select * from dropbox_upload where upload_id = ?";

			return new QueryRunner().query(conn, sql, new UploadInfoRsHandler(),jobId);

		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	// rs handler
	class UploadInfoRsHandler implements ResultSetHandler<UploadInfo>{

		@Override
		public UploadInfo handle(ResultSet rs) throws SQLException {
			UploadInfo info = null;
			if(rs.next()){
				info = new UploadInfo();
				info.setUploadId(rs.getInt("UPLOAD_ID"));
				info.setProgress(rs.getInt("PROGRESS"));
				info.setFilePath(rs.getString("FILE_PATH"));
				info.setFileName(rs.getString("FILE_NAME"));
				info.setFileMd5(rs.getString("FILE_MD5"));
				info.setFileSize(rs.getDouble("FILE_SIZE"));
			}
			return info;
		}
		
	}
}
