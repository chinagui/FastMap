package com.navinfo.dataservice.engine.man.day2Month;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.FmDay2MonSync;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

public class Day2MonthSyncService {
	private Logger log=LoggerRepos.getLogger(getClass());
	private Day2MonthSyncService(){}

	private static class SingletonHolder{
		private static final Day2MonthSyncService INSTANCE=new Day2MonthSyncService();
	}
	public static Day2MonthSyncService getInstance(){
		return SingletonHolder.INSTANCE;
	}
	/**落月同步信息
	 * @param info
	 * @return
	 * @throws Exception
	 */
	public Long insertSyncInfo(FmDay2MonSync info) throws Exception {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			long sid = DataBaseUtils.fetchSequence(conn, "fm_day2month_sync_seq");
			String sql = "insert into fm_day2month_sync (sync_id,region_id,sync_time,sync_status,job_id)values(?,?,sysdate,?,?)";
			run.update(conn, sql, sid,info.getRegionId(),info.getSyncStatus(),info.getJobId());
			return sid;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("日落月同步信息写入失败[insertSyncInfo]，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**修改日落月同步信息
	 * @param info
	 * @return
	 * @throws Exception
	 */
	public Integer updateSyncInfo(FmDay2MonSync info) throws Exception {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String sql = "update fm_day2month_sync set region_id=?,sync_time=to_date(?,'yyyyMMddhh24miss'),sync_status=?,job_id=? where sync_id=?";
			String syncTime =  new SimpleDateFormat("yyyyMMddHHmmss").format(info.getSyncTime());
			return run.update(conn, sql, info.getRegionId(),syncTime,info.getSyncStatus(),info.getJobId(),info.getSid());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("日落月同步信息修改失败[updateSyncInfo]，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**根据cityid获取最后一次成功同步的日落月同步信息
	 * @param cityId
	 * @return
	 * @throws Exception
	 */
	public  FmDay2MonSync queryLastedSyncInfo(Integer cityId) throws Exception {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String sql = "select * from (select sync_id,region_id,sync_time,sync_status,job_id from  fm_day2month_sync where region_Id=? and sync_status=? order by sync_time desc) where rownum=1 ";
			ResultSetHandler<FmDay2MonSync> rsh = new ResultSetHandler<FmDay2MonSync>(){

				@Override
				public FmDay2MonSync handle(ResultSet rs) throws SQLException {
					if(rs.next()){
						FmDay2MonSync syncInfo = new FmDay2MonSync();
						syncInfo.setSid(rs.getLong("sync_id"));
						syncInfo.setRegionId(rs.getLong("region_id"));
						syncInfo.setJobId(rs.getLong("job_id"));
						syncInfo.setSyncStatus(rs.getInt("sync_status"));
						syncInfo.setSyncTime(rs.getDate("sync_time"));
						return syncInfo;
					}
					return null;
				}};
			return run.query(conn,sql, rsh ,cityId,FmDay2MonSync.SyncStatusEnum.SUCCESS.getValue());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("日落月同步信息查询失败[queryLastedSyncInfo]，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
