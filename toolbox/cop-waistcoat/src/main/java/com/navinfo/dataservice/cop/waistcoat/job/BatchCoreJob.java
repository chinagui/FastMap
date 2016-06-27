package com.navinfo.dataservice.cop.waistcoat.job;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.QueryRunner;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/** 
* @ClassName: BatchCoreJob 
* @author Xiao Xiaowen 
* @date 2016年6月21日 上午11:56:42 
* @Description: TODO
*  
*/
public class BatchCoreJob extends AbstractJob {

	public BatchCoreJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() throws JobException {
		BatchCoreJobRequest req = (BatchCoreJobRequest)request;
		BatchCoreParams batchParams = analyzeBatchParams(req);
		try {
			Connection conn = DBConnector.getInstance().getConnectionById(req.getExecuteDBId());
			String batchPrepareResult = prepareBatch(conn, batchParams);
			response("批处理准备步骤完成",null);
			if(batchPrepareResult.equals("批处理准备成功")) {
				String batchExecuteResult = executeBatch(conn, req.getRuleIds());
				response("批处理执行步骤完成",null);
				if(!batchExecuteResult.equals("批处理执行成功")) {
					throw new JobException(batchExecuteResult);
				}
			}
			else {
				throw new JobException(batchPrepareResult);
			}

		} catch (Exception e) {
			throw new JobException(e.getMessage(),e);
		}
	}

	private BatchCoreParams analyzeBatchParams(BatchCoreJobRequest req) {
		BatchCoreParams batchParams = new BatchCoreParams();
		String[] batchDBInfos = req.getExecuteGdbConnInfo().split(",");
		batchParams.setBatchUserName(batchDBInfos[4]);
		batchParams.setBatchPasswd(batchDBInfos[5]);
		batchParams.setBatchHost(batchDBInfos[1]);
		batchParams.setBatchPort(batchDBInfos[2]);
		batchParams.setBatchSid(batchDBInfos[3]);


		String[] backupDBInfos = req.getBackupGdbConnInfo().split(",");
		batchParams.setBackupUserName(backupDBInfos[4]);
		batchParams.setBackupPasswd(backupDBInfos[5]);
		batchParams.setBackupHost(backupDBInfos[1]);
		batchParams.setBackupPort(backupDBInfos[2]);
		batchParams.setBackupSid(backupDBInfos[3]);

		String[] kdbInfos = req.getKdbConnInfo().split(",");
		batchParams.setKdbUserName(kdbInfos[4]);
		batchParams.setKdbPasswd(kdbInfos[5]);
		batchParams.setKdbHost(kdbInfos[1]);
		batchParams.setKdbPort(kdbInfos[2]);
		batchParams.setKdbSid(kdbInfos[3]);

		String[] pidManInfos = req.getPidConnInfo().split(",");
		batchParams.setDmsUserName(pidManInfos[4]);
		batchParams.setDmsPasswd(pidManInfos[5]);
		batchParams.setDmsHost(pidManInfos[1]);
		batchParams.setDmsPort(pidManInfos[2]);
		batchParams.setDmsSid(pidManInfos[3]);

		return batchParams;
	}

	public String prepareBatch(Connection conn, BatchCoreParams batchParams){
		String batchResult = "";
		CallableStatement statement = null;
		String sql = "{call A_NAVI_BATCH.PREPARE_BATCH(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
		try {
			if (conn != null) {
				statement = conn.prepareCall(sql);
				statement.setString(1, batchParams.getDmsUserName());
				statement.setString(2, batchParams.getDmsPasswd());
				statement.setString(3, batchParams.getDmsHost());
				statement.setString(4, batchParams.getDmsPort());
				statement.setString(5, batchParams.getDmsSid());
				statement.setString(6, batchParams.getKdbUserName());
				statement.setString(7, batchParams.getKdbPasswd());
				statement.setString(8, batchParams.getKdbHost());
				statement.setString(9, batchParams.getKdbPort());
				statement.setString(10, batchParams.getKdbSid());
				statement.setString(11, batchParams.getBackupUserName());
				statement.setString(12, batchParams.getBackupPasswd());
				statement.setString(13, batchParams.getBackupHost());
				statement.setString(14, batchParams.getBackupPort());
				statement.setString(15, batchParams.getBackupSid());
				statement.registerOutParameter(16, Types.NVARCHAR);
				statement.execute();

				String errInfo = statement.getNString(16);
				if (errInfo.length() == 0) {
					batchResult = "批处理准备成功";
				} else {
					batchResult = errInfo;
				}
			}
			else {
				batchResult = "获取的批处理子版本数据库连接为空";
			}
		}
		catch(SQLException e) {
			try {
				String errInfo = statement.getNString(16);
				batchResult = errInfo;
			}catch(SQLException ex) {
				batchResult = "获取批处理准备过程中异常信息失败";
			}
		}
		finally
		{
			try {
				if(statement != null)
					statement.close();
			}
			catch(SQLException e) {
				batchResult = "关闭CallableStatement对象时出现异常";
			}
		}
		return batchResult;
	}

	public String executeBatch(Connection conn, String ruleIds) {
		String batchResult = "";
		CallableStatement statement = null;
		String sql = "{call A_NAVI_BATCH.RUN(?,?)}";
		try {
			if (conn != null) {
				statement = conn.prepareCall(sql);
				statement.setString(1, ruleIds);
				statement.registerOutParameter(2, Types.NVARCHAR);
				statement.execute();

				String errInfo = statement.getNString(2);
				batchResult = "批处理执行成功";
			}
			else {
				batchResult = "获取的批处理子版本数据库连接为空";
			}
		}
		catch(SQLException e) {
			try {
				String errInfo = statement.getNString(16);
				batchResult = errInfo;
			}catch(SQLException ex) {
				batchResult = "获取批处理执行过程中获取异常信息失败";
			}
		}
		finally
		{
			try {
				if(statement != null)
					statement.close();
			}
			catch(SQLException e) {
				batchResult = "关闭CallableStatement对象时出现异常";
			}
		}
		return batchResult;
	}

	class BatchCoreParams {

		private String batchUserName;
		private String batchPasswd;
		private String batchHost;
		private String batchPort;
		private String batchSid;

		private String backupUserName;
		private String backupPasswd;
		private String backupHost;
		private String backupPort;
		private String backupSid;

		private String kdbUserName;
		private String kdbPasswd;
		private String kdbHost;
		private String kdbPort;
		private String kdbSid;

		private String dmsUserName;
		private String dmsPasswd;
		private String dmsHost;
		private String dmsPort;
		private String dmsSid;

		public String getBatchUserName() {
			return batchUserName;
		}

		public void setBatchUserName(String batchUserName) {
			this.batchUserName = batchUserName;
		}

		public String getBatchPasswd() {
			return batchPasswd;
		}

		public void setBatchPasswd(String batchPasswd) {
			this.batchPasswd = batchPasswd;
		}

		public String getBatchHost() {
			return batchHost;
		}

		public void setBatchHost(String batchHost) {
			this.batchHost = batchHost;
		}

		public String getBatchPort() {
			return batchPort;
		}

		public void setBatchPort(String batchPort) {
			this.batchPort = batchPort;
		}

		public String getBatchSid() {
			return batchSid;
		}

		public void setBatchSid(String batchSid) {
			this.batchSid = batchSid;
		}

		public String getKdbUserName() {
			return kdbUserName;
		}

		public void setKdbUserName(String kdbUserName) {
			this.kdbUserName = kdbUserName;
		}

		public String getKdbPasswd() {
			return kdbPasswd;
		}

		public void setKdbPasswd(String kdbPasswd) {
			this.kdbPasswd = kdbPasswd;
		}

		public String getKdbHost() {
			return kdbHost;
		}

		public void setKdbHost(String kdbHost) {
			this.kdbHost = kdbHost;
		}

		public String getKdbPort() {
			return kdbPort;
		}

		public void setKdbPort(String kdbPort) {
			this.kdbPort = kdbPort;
		}

		public String getKdbSid() {
			return kdbSid;
		}

		public void setKdbSid(String kdbSid) {
			this.kdbSid = kdbSid;
		}

		public String getDmsUserName() {
			return dmsUserName;
		}

		public void setDmsUserName(String dmsUserName) {
			this.dmsUserName = dmsUserName;
		}

		public String getDmsPasswd() {
			return dmsPasswd;
		}

		public void setDmsPasswd(String dmsPasswd) {
			this.dmsPasswd = dmsPasswd;
		}

		public String getDmsHost() {
			return dmsHost;
		}

		public void setDmsHost(String dmsHost) {
			this.dmsHost = dmsHost;
		}

		public String getDmsPort() {
			return dmsPort;
		}

		public void setDmsPort(String dmsPort) {
			this.dmsPort = dmsPort;
		}

		public String getDmsSid() {
			return dmsSid;
		}

		public void setDmsSid(String dmsSid) {
			this.dmsSid = dmsSid;
		}

		public String getBackupUserName() {
			return backupUserName;
		}

		public void setBackupUserName(String backupUserName) {
			this.backupUserName = backupUserName;
		}

		public String getBackupPasswd() {
			return backupPasswd;
		}

		public void setBackupPasswd(String backupPasswd) {
			this.backupPasswd = backupPasswd;
		}

		public String getBackupHost() {
			return backupHost;
		}

		public void setBackupHost(String backupHost) {
			this.backupHost = backupHost;
		}

		public String getBackupPort() {
			return backupPort;
		}

		public void setBackupPort(String backupPort) {
			this.backupPort = backupPort;
		}

		public String getBackupSid() {
			return backupSid;
		}

		public void setBackupSid(String backupSid) {
			this.backupSid = backupSid;
		}

	}

}
