package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.glm.Glm;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.navicommons.database.sql.DbLinkCreator;

import net.sf.json.JSONObject;

/**
 * 17秋线上POI（只新增）导入一体化日大区库（生成新增履历）
 * 
 * @ClassName: ImportPoiToRegionDb
 * @author jch
 * @date 下午5:18:18
 * @Description: ImportPoiToRegionDb.java
 */
public class ImportPoiToRegionDb {

	public static void execute(JSONObject request) throws Exception {
		// 1)调用createDb Job创建空库
		JSONObject createDbReq = new JSONObject();
		createDbReq.put("serverType", "ORACLE");
		createDbReq.put("bizType", "copVersion");
		JobInfo jobInfo = new JobInfo(0, UuidUtils.genUuid());
		jobInfo.setType("createDb");
		jobInfo.setRequest(createDbReq);
		jobInfo.setTaskId(0);
		AbstractJob job = JobCreateStrategy.createAsMethod(jobInfo);
		job.run();
		JSONObject createDbResponse = job.getJobInfo().getResponse();

		// 2)调用diff Job执行差分，生成履历
		JSONObject diffReq = new JSONObject();
		diffReq.put("leftDbId", request.getInt("sourceDbId"));
		diffReq.put("rightDbId", createDbResponse.getInt("outDbId"));
		diffReq.put("specificTables", request.getJSONArray("specificTables"));
		JobInfo jobDiff = new JobInfo(0, UuidUtils.genUuid());
		jobDiff.setType("diff");
		jobDiff.setRequest(diffReq);
		jobDiff.setTaskId(0);
		AbstractJob jobDiffPoi = JobCreateStrategy.createAsMethod(jobDiff);
		jobDiffPoi.run();

		// 3)调用GdbImport刷履历、数据
		JSONObject gdbImpReq = new JSONObject();
		gdbImpReq.put("logDbId", request.getInt("sourceDbId"));
		gdbImpReq.put("targetDbId", request.getInt("targetDbId"));
		JobInfo jobGdbImp = new JobInfo(0, UuidUtils.genUuid());
		jobGdbImp.setType("gdbImport");
		jobGdbImp.setRequest(gdbImpReq);
		jobGdbImp.setTaskId(0);
		AbstractJob jobImp = JobCreateStrategy.createAsMethod(jobGdbImp);
		jobImp.run();

		// 3）写入子任务到履历；
		// 更新log_action表，默认为0,不用修改

		// 4）生成poi_edit_status记录；
		System.out.println("begin genPoiEditRecode");
		genPoiEditRecord(request.getInt("sourceDbId"));
		System.out.println("genPoiEditRecode end");
		// 5）修改日落月标记，改为未落过，默认生成的履历就是未落过（log_operation->con_sta）=0,不用修改
		// updateDaytoMonthFlag(sourceConn);

		// 6）修改出品状态，改为不出品
//		updateProductFlag(sourceConn);

		// 7复制数据
//		copyData(request.getInt("targetDbId"), request.getString("dblink"));
		
		//copy poi_edit_status, 修改log_day_release为不出品
		System.out.println("begin updateData");
		updateData(request.getInt("sourceDbId"),request.getInt("targetDbId"));
		System.out.println("updateData end");

	}

	public static void main(String[] args) throws Exception {
		initContext();
		System.out.println("args.length:" + args.length);
		if (args == null || args.length != 2) {
			System.out.println("ERROR:need args:");
			return;
		}
		JSONObject request = new JSONObject();
		request.put("sourceDbId", args[0]);
		request.put("targetDbId", args[1]);
	
		execute(request);
		// System.out.println(response);
		System.out.println("Over.");
		System.exit(0);
	}

	public static void initContext() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-app-scripts.xml", "dubbo-scripts.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	public static void genPoiEditRecord(int dbId) throws Exception {
		
		Connection conn = DBConnector.getInstance().getConnectionById(dbId);

		PreparedStatement pstmt = null;
		String sql = "INSERT INTO poi_edit_status(pid,status) SELECT pid,3 FROM ix_poi";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate(sql);
		} catch (Exception e) {
			throw new SQLException("加载ix_poi失败：" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(pstmt);
			DbUtils.commitAndCloseQuietly(conn);
		}

	}

	public static void updateProductFlag(Connection conn) throws Exception {

		PreparedStatement pstmt = null;
		String sql = "update log_day_release set rel_poi_sta=1,rel_all_sta=1";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate(sql);
		} catch (Exception e) {
			throw new SQLException("加载log_day_release失败：" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(pstmt);
			DbUtils.commitAndCloseQuietly(conn);
		}

	}

	public static void copyData(int targetDbId, String dblink) throws Exception {

		Glm glm = GlmCache.getInstance()
				.getGlm(SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
		Connection conn = DBConnector.getInstance().getConnectionById(targetDbId);
		String sql = "";
		String[] logAndStatusTable = { "poi_edit_status", "Log_Action", "Log_Day_Release", "Log_Detail",
				"Log_Detail_Grid", "log_operation" };

		try {
			for (GlmTable table : glm.getEditTables().values()) {
				System.out.println(table.getName());
				sql = "INSERT INTO " + table.getName() + " SELECT * FROM " + table.getName() + "@" + dblink;

				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.executeUpdate(sql);
				DbUtils.closeQuietly(pstmt);
			}

			for (int i = 0; i < logAndStatusTable.length; i++) {
				System.out.println(logAndStatusTable[i]);
				sql = "INSERT INTO " + logAndStatusTable[i] + " SELECT * FROM " + logAndStatusTable[i] + "@" + dblink;

				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.executeUpdate(sql);
				DbUtils.closeQuietly(pstmt);
			}

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new SQLException("加载table失败：" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

public static void updateData(int sourceDbId,int targetDbId) throws Exception{
	createTargetDbLink(sourceDbId,targetDbId);
	Connection conn = DBConnector.getInstance().getConnectionById(targetDbId);
	PreparedStatement pstmt=null;
	PreparedStatement pstmt1=null;
	try {
		String sql="INSERT INTO poi_edit_status SELECT * FROM poi_edit_status@DBLINK_"+sourceDbId;
		
		pstmt = conn.prepareStatement(sql);
		pstmt.executeUpdate(sql);
		
		String sql1="UPDATE log_day_release r SET r.rel_poi_sta=1, r.rel_all_sta=1 WHERE EXISTS ( "
			+"	SELECT l.op_id FROM log_detail l,ix_poi@DBLINK_"+sourceDbId 
			+" p WHERE l.ob_nm='IX_POI' AND l.op_id=r.op_id AND l.ob_pid=p.pid)";
		
		pstmt1 = conn.prepareStatement(sql1);
		pstmt.executeUpdate(sql1);
	}catch (Exception e) {
				DbUtils.rollbackAndCloseQuietly(conn);
				throw new SQLException("加载table失败：" + e.getMessage(), e);
			} finally {
				DbUtils.closeQuietly(pstmt);
				DbUtils.closeQuietly(pstmt1);
				DbUtils.commitAndCloseQuietly(conn);
			}	
	}

	private static void createTargetDbLink(int sourceDbId,int targetDbId) throws Exception {
		DbInfo sourcedb = DbService.getInstance().getDbById(sourceDbId);
		DbInfo targetdb = DbService.getInstance().getDbById(targetDbId);
		DbConnectConfig connConfig = DbConnectConfig.createConnectConfig(targetdb.getConnectParam());
		DataSource targetDbDataSource = MultiDataSourceFactory.getInstance().getDataSource(connConfig);
		DbLinkCreator cr = new DbLinkCreator();
		cr.create("DBLINK_" + sourceDbId, false, targetDbDataSource, sourcedb.getDbUserName(), sourcedb.getDbUserPasswd(),
				sourcedb.getDbServer().getIp(), String.valueOf(sourcedb.getDbServer().getPort()),
				sourcedb.getDbServer().getServiceName());
	}
}
