package com.navinfo.dataservice.cop.waistcoat.job;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.oracle.MyDriverManagerDataSource;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @ClassName: BatchCoreJob
 * @author Zhang Runze
 * @date 2016年6月21日 上午11:56:42
 * @Description: TODO: 批处理核心Job实现
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
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(req.getExecuteDBId());
			String batchPrepareResult = prepareBatch(conn, batchParams);
			response("批处理准备步骤完成",null);
			log.debug(batchPrepareResult);
			if(batchPrepareResult.equals("批处理准备成功")) {
				String batchRuleIds = StringUtils.join(req.getRuleIds(), ",");
				String batchExecuteResult = executeBatch(conn, batchRuleIds);
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
		finally
		{
			try {
				if(conn != null)
					conn.close();
			}
			catch(SQLException e) {
				throw new JobException(e.getMessage(),e);
			}
		}
	}

	@Override
	public Exception getException() {
		return super.getException();
	}

	/**
	 * 通过传入seq中的DBId号
	 * @param req
	 * @return
     */
	private BatchCoreParams analyzeBatchParams(BatchCoreJobRequest req) {
		BatchCoreParams batchParams = new BatchCoreParams();
		DatahubApi datahub = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");

		try {
			//解析批处理库参数
			DbInfo batchDBInfo = datahub.getDbById(req.getExecuteDBId());
			batchParams.setBatchUserName(batchDBInfo.getDbUserName());
			batchParams.setBatchPasswd(batchDBInfo.getDbUserPasswd());
			batchParams.setBatchHost(batchDBInfo.getDbServer().getIp());
			batchParams.setBatchPort(Integer.toString(batchDBInfo.getDbServer().getPort()));
			batchParams.setBatchSid(batchDBInfo.getDbServer().getServiceName());


			//解析备份库参数
			DbInfo backupDBInfo = datahub.getDbById(req.getBackupDBId());
			batchParams.setBackupUserName(backupDBInfo.getDbUserName());
			batchParams.setBackupPasswd(backupDBInfo.getDbUserPasswd());
			batchParams.setBackupHost(backupDBInfo.getDbServer().getIp());
			batchParams.setBackupPort(Integer.toString(backupDBInfo.getDbServer().getPort()));
			batchParams.setBackupSid(backupDBInfo.getDbServer().getServiceName());

			//解析元数据库参数
			DbInfo kdbDBInfo = datahub.getDbById(req.getKdbDBId());
			batchParams.setKdbUserName(kdbDBInfo.getDbUserName());
			batchParams.setKdbPasswd(kdbDBInfo.getDbUserPasswd());
			batchParams.setKdbHost(kdbDBInfo.getDbServer().getIp());
			batchParams.setKdbPort(Integer.toString(kdbDBInfo.getDbServer().getPort()));
			batchParams.setKdbSid(kdbDBInfo.getDbServer().getServiceName());

			//解析DMS(PID)库参数
			String[] pidManDBInfos = req.getPidDbInfo().split(",");
			batchParams.setDmsUserName(pidManDBInfos[4]);
			batchParams.setDmsPasswd(pidManDBInfos[5]);
			batchParams.setDmsHost(pidManDBInfos[1]);
			batchParams.setDmsPort(pidManDBInfos[2]);
			batchParams.setDmsSid(pidManDBInfos[3]);
		} catch (Exception e) {
			e.printStackTrace();
		}


		return batchParams;
	}

	/**
	 * 批处理准备过程，通过conn调用PREPARE_BATCH存储过程
	 * @param conn
	 * @param batchParams：解析好的批处理参数信息
     * @return
     */
	public String prepareBatch(Connection conn, BatchCoreParams batchParams){
		String batchResult = "";
		CallableStatement statement = null;
		String sql = "{call A_NAVI_BATCH.PREPARE_BATCH(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
		try {
			if (conn != null) {
				log.info("DMSUserName:" +  batchParams.getDmsUserName());
				log.info("DMSPassWord:" +  batchParams.getDmsPasswd());
				log.info("DMSHost:" +  batchParams.getDmsHost());
				log.info("DMSPort:" +  batchParams.getDmsPort());
				log.info("DMSSid:" +  batchParams.getDmsSid());
				log.info("KdbUserName:" +  batchParams.getKdbUserName());
				log.info("KdbPasswd:" +  batchParams.getKdbPasswd());
				log.info("KdbHost:" +  batchParams.getKdbHost());
				log.info("KdbPort:" +  batchParams.getKdbPort());
				log.info("KdbSid:" +  batchParams.getKdbSid());
				log.info("BackupUserName:" +  batchParams.getBackupUserName());
				log.info("BackupPasswd:" +  batchParams.getBackupPasswd());
				log.info("BackupHost:" +  batchParams.getBackupHost());
				log.info("BackupPort:" +  batchParams.getBackupPort());
				log.info("BackupSid:" +  batchParams.getBackupSid());
				
				
				
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
				statement.registerOutParameter(16, Types.VARCHAR);
				statement.execute();

				String errInfo = statement.getString(16);
				System.out.println(errInfo);
				if (errInfo == null || errInfo.length() == 0) {
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
				String errInfo = statement.getString(16);
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

	/**
	 * 批处理执行过程，通过conn调用RUN存储过程
	 * @param conn
	 * @param ruleIds：执行的规则号，以“，”分割
     * @return
     */
	public String executeBatch(Connection conn, String ruleIds) {
		String batchResult = "";
		CallableStatement statement = null;
		String sql = "{call A_NAVI_BATCH.RUN(?,?)}";
		try {
			if (conn != null) {
				statement = conn.prepareCall(sql);
				statement.setString(1, ruleIds);
				statement.registerOutParameter(2, Types.VARCHAR);
				statement.execute();

				String errInfo = statement.getString(2);
				batchResult = "批处理执行成功";
			}
			else {
				batchResult = "获取的批处理子版本数据库连接为空";
			}
		}
		catch(SQLException e) {
			try {
				String errInfo = statement.getString(2);
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
	

	public static void main(String args[]) {
		Connection conn = null;
		try{
			DriverManagerDataSource dataSource = new MyDriverManagerDataSource();
			String driveClassName = "oracle.jdbc.driver.OracleDriver";
			String url = "jdbc:oracle:thin:@192.168.3.105:1521";
			String username = "HUB_ZnryJURnZY";
			String pwd = "HUB_ZnryJURnZY";
			dataSource.setDriverClassName(driveClassName);
			dataSource.setUrl(url);
			dataSource.setUsername(username);
			dataSource.setPassword(pwd);
			conn = dataSource.getConnection();
			BatchCoreJob job = new BatchCoreJob(null);
			
		}catch(Exception e){
			e.printStackTrace();
		}
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
