package com.navinfo.dataservice.day2mon;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.BizType;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.DbServerType;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.SingleBatchSelRsHandler;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.SqlExec;

/**
 * 详细需求参考zentao任务：http://118.89.222.51/zentao/story-view-860.html
 * 1.日落月时，记录要批引导link的POI；
 * 2.定制生成子版本：尽量降低提取的数据量，只提取cop批引导link所必要的数据；
 * 3.调用cop的批引导link的现有方法，在子版本上批数据；
 * 4.进行POI数据的差分，生成FM的履历：第1步记录的POI 和子版本已经批过的POI 进行差分（和二代两个子版本差分有差别）；
 * 5.刷月库
 * @author MaYunFei
 *
 */
public class PoiGuideLinkBatch {
	private static final String BATCH_TASK_NAME = "day2Mon_FM-BAT-M01-07";
	private static final String BATCH_POI_GUIDELINK = "BATCH_POI_GUIDELINK";
	Logger log = LoggerRepos.getLogger(this.getClass());
	private OperationResult opResult;
	private Connection conn = null;
	private OracleSchema monthDbSchema;
	public PoiGuideLinkBatch(OperationResult opResult, Connection conn, OracleSchema monthDbSchema) {
		super();
		this.opResult = opResult;
		this.conn = conn;
		this.monthDbSchema = monthDbSchema;
	}
	public void execute() throws Exception {
		//1.粗选POI:根据operationResult解析获取要批引导link的poi数据
		Map<Long, BasicObj> changedPois = this.opResult.getChangedObjsByName(ObjectName.IX_POI);
		if(MapUtils.isEmpty(changedPois)) {
			log.info("没有获取到有变更的poi数据");
			return ;	
		}
		//2.把精选的POI.pid放在临时表temp_poi_glink_yyyyMMddhhmmss（临时表不存在则新建）；
		String tempPoiGLinkTab = createTempPoiGLinkTable();
		//3.精选POI:根据粗选的结果，进一步过滤得到(新增POI或修改引导坐标或引导link为0的POI对象或对应引导link不存在rd_link表中)
		Set<Long> refinedPois = new HashSet<Long>();
		for(Long pid :changedPois.keySet()){
			BasicObj poiObj = changedPois.get(pid);
			if(OperationType.INSERT==poiObj.getMainrow().getOpType()
					||poiObj.getMainrow().getChangedColumns().contains("Y_GUIDE")
					||poiObj.getMainrow().getChangedColumns().contains("X_GUIDE")
					||Integer.valueOf(0).equals(poiObj.getMainrow().getAttrByColName("LINK_PID"))
					){
				refinedPois.add(pid);
			}
		}
		insertPois2TempTab(refinedPois,tempPoiGLinkTab);
		insertPoisNotInRdLink2TempTab(CollectionUtils.subtract(changedPois.keySet(), refinedPois),tempPoiGLinkTab);
		//4.创建用于cop批处理的子版本
		DbInfo copVersionDbInfo = createCopVersion();
		OracleSchema copVersionSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(copVersionDbInfo.getConnectParam()));
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		try{
			// 在子版本库上建立月库的db_link
			String dbLinkName = createDbLink(copVersionConn);
			//4.1创建子版本库并初始化子版本数据；
			//4.2创建需要的表结构:ix_poi,ix_poi_address,rd_link,rd_link_form,rd_name
			exeTabelCreateSql(copVersionConn);
			//导入cop的point_feature_batch包
			importCopPck(copVersionConn);
			//4.3导入ix_poi:根据tempPoiGLinkTab 进行关联
			initIxPoi(copVersionConn,tempPoiGLinkTab,dbLinkName);
			//4.4导入ix_poi_address:根据tempPoiGLinkTab 进行关联
			initIxPoiAddress(copVersionConn,tempPoiGLinkTab,dbLinkName);
			//导入ix_poi_flag:根据tempPoiGLinkTab 进行关联
			initIxPoiFlag(copVersionConn,tempPoiGLinkTab,dbLinkName);
			//4.5导入rd_link:按照4.3的poi进行扩圈，计算要导入的rd_link的pid
			initRdLink(copVersionSchema,tempPoiGLinkTab);
			//4.6导入rd_link_form :按照4.5得到的rd_link导入rd_link_form;
			initRdLinkForm(copVersionSchema,tempPoiGLinkTab);
			//4.7导入rd_name:按照4.5得到的rd_link和rd_link_name 进行关联，得到要导入的rd_name的group_id；再关联月库的rd_name； 得到要导出的rd_name
			initRdName(copVersionSchema,tempPoiGLinkTab);
			//4.8备份ix_poi为ix_poi_back(可以只备份pid,x_guid,y_guid,link_pid,name_groupid,side,pmesh_id),为后续的差分及生成履历做准备；
			backupIxPoi(copVersionConn);
			//4.9备份ix_poi为ix_poi_flag_back；
			backupIxPoiFlag(copVersionConn);
			//5.调用cop的批处理程序；
			callCopPackage(copVersionConn);
			//6.差分ix_poi和ix_poi_back;生成差分履历；
			diff(copVersionSchema);
			//7.根据6的差分履历刷新月库；
			flushDiffLog(copVersionSchema);
			//8.把6生成的履历搬移到月库
			moveDiffLog(copVersionSchema);
		}catch(Exception e){
			DbUtils.rollback(copVersionConn);
			log.error(e.getMessage());
		}finally{
			DbUtils.commitAndClose(copVersionConn);
		}
		
	}
	
	private void initIxPoiFlag(Connection copVersionConn, String tempPoiGLinkTab,String dbLinkName) throws SQLException {
		String sql = "INSERT INTO ix_poi_flag"+
				"SELECT q.* FROM (SELECT * FROM ix_poi_flag@dblink_gdb_m_1 p "+
				"WHERE p.poi_pid IN (SELECT * FROM temp_poi_glink_1@dblink_gdb_m_1 t)) q; ";
		new QueryRunner().update(copVersionConn, sql); 
	}
	private void moveDiffLog(OracleSchema copVersionSchema) {
		// TODO Auto-generated method stub
		
	}
	private void flushDiffLog(OracleSchema copVersionSchema) {
		// TODO Auto-generated method stub
		
	}
	private void diff(OracleSchema copVersionSchema) {
		// TODO Auto-generated method stub
		
	}
	private void callCopPackage(Connection copVersionConn) throws Exception {
		
		CallableStatement cs = null;
		long pid=0L;
		try{
			String sql = "{call POINT_FEATURE_BATCH.BATCH_IXPOI_LINK()}";
			cs = copVersionConn.prepareCall(sql);
			cs.execute();
		}catch(Exception e){
			log.error(e);
    		DbUtils.closeQuietly(cs);
		}finally{
    		DbUtils.closeQuietly(cs);
		}
		
	}
	
	private void backupIxPoi(Connection copVersionConn) throws Exception{
		String sql = "INSERT INTO ix_poi_back"+
				"SELECT p.PID,p.X_GUIDE,p.Y_GUIDE,p.LINK_PID,p.SIDE,p.NAME_GROUPID,p.PMESH_ID FROM ix_poi p; ";
		new QueryRunner().update(copVersionConn, sql); 		
	}
	
	private void backupIxPoiFlag(Connection copVersionConn) throws Exception{
		String sql = "INSERT INTO ix_poi_flag_back SELECT p.* FROM ix_poi_flag p";
		new QueryRunner().update(copVersionConn, sql);
	}
	
	private void initRdName(OracleSchema copVersionSchema, String tempPoiGLinkTab) {
		// TODO Auto-generated method stub
		
	}
	private void initRdLinkForm(OracleSchema copVersionSchema, String tempPoiGLinkTab) {
		// TODO Auto-generated method stub
		
	}
	private void initRdLink(OracleSchema copVersionSchema, String tempPoiGLinkTab) {
		// TODO Auto-generated method stub
		
	}
	private void initIxPoiAddress(Connection copVersionConn, String tempPoiGLinkTab,String dbLinkName) throws Exception {
		String sql = "INSERT INTO ix_poi_address"+
		"SELECT q.* FROM (SELECT * FROM ix_poi_address@dblink_gdb_m_1 p "+
		"WHERE p.poi_pid IN (SELECT * FROM temp_poi_glink_1@dblink_gdb_m_1 t)) q; ";
		new QueryRunner().update(copVersionConn, sql); 
	}
	private void initIxPoi(Connection copVersionConn, String tempPoiGLinkTab,String dbLinkName) throws Exception {
		String sql = "INSERT INTO ix_poi"+
		"SELECT q.* FROM (SELECT * FROM ix_poi@dblink_gdb_m_1 p "+
		"WHERE p.pid IN (SELECT * FROM temp_poi_glink_1@dblink_gdb_m_1 t)) q; ";
		new QueryRunner().update(copVersionConn, sql); 
		
	}
	private void importCopPck(Connection copVersionConn) throws Exception{
		String pckFile = "/com/navinfo/dataservice/scripts/resources/point_feature_batch.pck";
		SqlExec sqlExec = new SqlExec(copVersionConn);
		sqlExec.execute(pckFile);
	}
	
	private void exeTabelCreateSql(Connection copVersionConn) throws Exception {
		String sqlFile = "/com/navinfo/dataservice/scripts/resources/poi_guide_link_batch_db_create.sql";
		SqlExec sqlExec = new SqlExec(copVersionConn);
		sqlExec.execute(sqlFile);
	}
	
	private String createDbLink(Connection copVersionConn) throws SQLException{
		String dbLinkName = "DBLINK_GDB_M_1";
		String userName = conn.getMetaData().getUserName();
		String userPassWord = monthDbSchema.getConnConfig().getUserPasswd();
		String URL = conn.getMetaData().getURL();
		String tmpUrl = URL.substring(URL.indexOf("@")+1);
		String sql = "CREATE DATABASE LINK "+ dbLinkName +" CONNECT TO "+ userName +" IDENTIFIED BY "+ userPassWord +" USING '"+ tmpUrl +"';";
		new QueryRunner().update(copVersionConn, sql);
		return dbLinkName;
	}
	private DbInfo createCopVersion() throws Exception {
		// TODO 创建cop子版本库
		DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
		int copDbId = 0;
		DbInfo copDb = datahub.getReuseDb(BizType.DB_COP_VERSION);
		Connection sysConn = null;
		try {
			QueryRunner run = new QueryRunner();
			sysConn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			long jobId = run.queryForLong(sysConn, "SELECT JOB_ID_SEQ.NEXTVAL FROM DUAL");
			String jobGuid = UuidUtils.genUuid();
			AbstractJobRequest createCopDb = JobCreateStrategy.createJobRequest("createDb", null);
			createCopDb.setAttrValue("serverType", DbServerType.TYPE_ORACLE);
			createCopDb.setAttrValue("bizType", BizType.DB_COP_VERSION);
			createCopDb.setAttrValue("descp", "batch guide_link temp db");
			createCopDb.setAttrValue("gdbVersion", "");
			JobInfo createCopDbJobInfo = new JobInfo(jobId, jobGuid);
			AbstractJob createCopDbJob = JobCreateStrategy.create(createCopDbJobInfo,createCopDb);
			createCopDbJob.run();
			if (createCopDbJob.getJobInfo().getStatus() != 3) {
				String msg = (createCopDbJob.getException()==null)?"未知错误。":"错误："+createCopDbJob.getException().getMessage();
				throw new Exception("创建子版本库时job内部发生"+msg);
			}
			//新建子版本库的dbid
			copDbId = createCopDbJob.getJobInfo().getResponse().getInt("outDbId");
			System.out.println("新建子版本库的dbid :" +copDbId);
			//根据返回的自版本库的dbid 获取子版本库的DbInfo
			copDb = datahub.getDbById(copDbId);
		}catch (Exception e){
			DbUtils.rollback(sysConn);
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(), e);
		}finally{
			DbUtils.commitAndCloseQuietly(sysConn);
		}
		return copDb;
		
	}
	private void insertPois2TempTab(Collection<Long> pids,String tempPoiTable) throws Exception{
		String sql = "insert /*+append*/ into "+tempPoiTable+" "
				+ "select column_value from table(clob_to_table(?)) ";
		this.log.debug("sql:"+sql);
		Clob clobPids=ConnectionUtil.createClob(conn);
		clobPids.setString(1, StringUtils.join(pids, ","));
		new QueryRunner().update(conn, sql, clobPids);
	}
	private String createTempPoiGLinkTable() throws Exception {
		String tableName = "temp_poi_glink"+(new SimpleDateFormat("yyyyMMddhhmmssS").format(new Date()));
		String sql ="create table "+tableName+" (pid number(10))";
		new QueryRunner().update(conn, sql);
		return tableName;
	}
	private void insertPoisNotInRdLink2TempTab(Collection<Long> pids,String tempPoiTable) throws Exception {
		String sql = "insert /*+append*/ into "+tempPoiTable+" "
				+ "select pid from ix_poi t  "
				+ "where t.pid in (select column_value from table(clob_to_table(?))) "
				+ "and not exists(select 1 from rd_link r where r.pid=t.rd_link)";
		this.log.debug("sql:"+sql);
		Clob clobPids=ConnectionUtil.createClob(conn);
		clobPids.setString(1, StringUtils.join(pids, ","));
		new QueryRunner().update(conn, sql, clobPids);
	}
	public static void main(String args[]) throws Exception{
		PoiGuideLinkBatch batch = new PoiGuideLinkBatch(null,null,null);
		System.out.print(batch.createTempPoiGLinkTable());
	}
	
}
