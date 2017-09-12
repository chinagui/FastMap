package com.navinfo.dataservice.cop.waistcoat.job;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.oracle.MyDriverManagerDataSource;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.RandomUtil;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @ClassName: RoadDtoMCoreJob
 * @author Zhang Runze
 * @date 2016年6月21日 上午11:56:42
 * @Description: TODO: 批处理核心Job实现
 *
 */
public class RoadDtoMCoreJob extends AbstractJob {

	public RoadDtoMCoreJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() throws JobException {
		RoadDtoMCoreJobRequest req = (RoadDtoMCoreJobRequest)request;
		DtoMCoreParams dtoMCoreParams = analyzeDtoMCoreParams(req);
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(req.getExecuteDBId());
			String vcpResult = runVCP(conn, dtoMCoreParams);
			response("VCP道路日落月执行完成",null);
			log.debug(vcpResult);
			if(!vcpResult.equals("VCP道路日落月执行成功")) {
				throw new JobException(vcpResult);
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
	private DtoMCoreParams analyzeDtoMCoreParams(RoadDtoMCoreJobRequest req) {
		DtoMCoreParams dtoMParams = new DtoMCoreParams();
		DatahubApi datahub = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");

		try {
			//解析批处理库(月库)参数
			DbInfo batchDBInfo = datahub.getDbById(req.getExecuteDBId());
			dtoMParams.setBatchUserName(batchDBInfo.getDbUserName());
			dtoMParams.setBatchPasswd(batchDBInfo.getDbUserPasswd());
			dtoMParams.setBatchHost(batchDBInfo.getDbServer().getIp());
			dtoMParams.setBatchPort(Integer.toString(batchDBInfo.getDbServer().getPort()));
			dtoMParams.setBatchSid(batchDBInfo.getDbServer().getServiceName());

			//解析日库参数
			DbInfo dayDBInfo = datahub.getDbById(req.getDayDBId());
			dtoMParams.setDayUserName(dayDBInfo.getDbUserName());
			dtoMParams.setDayPasswd(dayDBInfo.getDbUserPasswd());
			dtoMParams.setDayHost(dayDBInfo.getDbServer().getIp());
			dtoMParams.setDayPort(Integer.toString(dayDBInfo.getDbServer().getPort()));
			dtoMParams.setDaySid(dayDBInfo.getDbServer().getServiceName());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return dtoMParams;
	}

	/**
	 * 批处理准备过程，通过conn调用PREPARE_BATCH存储过程
	 * @param conn
	 * @param dtoMCoreParams：解析好的日落月参数信息
     * @return
     */
	public String runVCP(Connection conn, DtoMCoreParams dtoMCoreParams){
		String batchResult = "";
		CallableStatement statement = null;
		String sql = "{call NAVI_BATCH_MATCH_NEW.RUN_VCP(?,?,?,?,?,?,?,?,?,?,?)}";
		try {
			if (conn != null) {
				String taskName = "VCP_Pro_" + RandomUtil.nextString(10);

				log.info("DayUserName:" +  dtoMCoreParams.getDayUserName());
				log.info("DayPassWord:" +  dtoMCoreParams.getDayPasswd());
				log.info("DayHost:" +  dtoMCoreParams.getDayHost());
				log.info("DayPort:" +  dtoMCoreParams.getDayPort());
				log.info("DaySid:" +  dtoMCoreParams.getDaySid());
				log.info("BatchUserName:" +  dtoMCoreParams.getBatchUserName());
				log.info("BatchPasswd:" +  dtoMCoreParams.getBatchPasswd());
				log.info("BatchHost:" +  dtoMCoreParams.getBatchHost());
				log.info("BatchPort:" +  dtoMCoreParams.getBatchPort());
				log.info("BatchSid:" +  dtoMCoreParams.getBatchSid());
				
				statement = conn.prepareCall(sql);
				statement.setString(1, taskName);
				statement.setString(2, dtoMCoreParams.getDayUserName());
				statement.setString(3, dtoMCoreParams.getDayPasswd());
				statement.setString(4, dtoMCoreParams.getDayHost());
				statement.setString(5, dtoMCoreParams.getDayPort());
				statement.setString(6, dtoMCoreParams.getDaySid());
				statement.setString(7, dtoMCoreParams.getBatchUserName());
				statement.setString(8, dtoMCoreParams.getBatchPasswd());
				statement.setString(9, dtoMCoreParams.getBatchHost());
				statement.setString(10, dtoMCoreParams.getBatchPort());
				statement.setString(11, dtoMCoreParams.getBatchSid());
				statement.execute();

				batchResult = "VCP道路日落月执行成功";
			}
			else {
				batchResult = "获取的批处理子版本数据库连接为空";
			}
		}
		catch(SQLException e) {
			e.getMessage();
			batchResult = "执行日落月过程中发生异常，子版本信息：" + dtoMCoreParams.getBatchUserName() + "/" + dtoMCoreParams.getBatchPasswd() + "," + dtoMCoreParams.getBatchHost();
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
			String driverClassName = "oracle.jdbc.driver.OracleDriver";
			String url = "jdbc:oracle:thin:@192.168.3.105:1521";
			String username = "HUB_ZnryJURnZY";
			String pwd = "HUB_ZnryJURnZY";
			dataSource.setDriverClassName(driverClassName);
			dataSource.setUrl(url);
			dataSource.setUsername(username);
			dataSource.setPassword(pwd);
			conn = dataSource.getConnection();
			RoadDtoMCoreJob job = new RoadDtoMCoreJob(null);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	class DtoMCoreParams {

		private String batchUserName;
		private String batchPasswd;
		private String batchHost;
		private String batchPort;
		private String batchSid;

		private String dayUserName;
		private String dayPasswd;
		private String dayHost;
		private String dayPort;
		private String daySid;

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

		public String getDayUserName() {
			return dayUserName;
		}

		public void setDayUserName(String dayUserName) {
			this.dayUserName = dayUserName;
		}

		public String getDayPasswd() {
			return dayPasswd;
		}

		public void setDayPasswd(String dayPasswd) {
			this.dayPasswd = dayPasswd;
		}

		public String getDayHost() {
			return dayHost;
		}

		public void setDayHost(String dayHost) {
			this.dayHost = dayHost;
		}

		public String getDayPort() {
			return dayPort;
		}

		public void setDayPort(String dayPort) {
			this.dayPort = dayPort;
		}

		public String getDaySid() {
			return daySid;
		}

		public void setDaySid(String daySid) {
			this.daySid = daySid;
		}

	}

}
