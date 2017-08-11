package com.navinfo.dataservice.day2mon;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.BizType;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculator;
import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculatorFactory;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.DbServerType;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.RandomUtil;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flusher.CopVersion2MonLogFlusher;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DbLinkCreator;
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
	private String tempPoiGLinkTab;
	private OracleSchema monthDbSchema;
	public PoiGuideLinkBatch(String tempPoiGLinkTab,OracleSchema monthDbSchema) {
		super();
		this.tempPoiGLinkTab = tempPoiGLinkTab;
		this.monthDbSchema = monthDbSchema;
	}
	public void execute() throws Exception {

		//4.创建用于cop批处理的子版本
		DbInfo copVersionDbInfo = createCopVersion();
		OracleSchema copVersionSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(copVersionDbInfo.getConnectParam()));
		log.info("子版本库信息 dbId:"+copVersionDbInfo.getDbId());
		log.info("子版本库信息 用户名:"+copVersionSchema.getConnConfig().getUserName());
		log.info("子版本库信息 密码:"+copVersionSchema.getConnConfig().getUserPasswd());
		log.info("子版本库信息 IP:"+copVersionSchema.getConnConfig().getServerIp());
		log.info("子版本库信息 port:"+copVersionSchema.getConnConfig().getServerPort());

		//子版本链接：用来搬移履历，在最后提交或回滚
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		//子版本链接：用来刷库，在最后提交或回滚
		Connection monthConn = monthDbSchema.getPoolDataSource().getConnection();
		try{
			// 在子版本库上建立月库的db_link
			String dbLinkName = createDbLink(copVersionSchema);
			//4.1创建需要的表结构:ix_poi,ix_poi_address,ix_poi_flag,rd_link,rd_link_form,rd_link_name,ni_rd_name
			exeTabelCreateSql(copVersionSchema);
			//导入cop的point_feature_batch包(不用导入，直接使用navisys上的包)
			//importCopPck(copVersionSchema);
			//4.2导入ix_poi:根据tempPoiGLinkTab 进行关联
			initIxPoi(copVersionSchema,dbLinkName);
			//4.3导入ix_poi_address:根据tempPoiGLinkTab 进行关联
			initIxPoiAddress(copVersionSchema,dbLinkName);
			//4.4导入ix_poi_flag:根据tempPoiGLinkTab 进行关联
			initIxPoiFlag(copVersionSchema,dbLinkName);
			//4.5导入rd_link:按照4.2的poi进行扩圈，计算要导入的rd_link的pid
			initRdLink(copVersionSchema,dbLinkName);
			//4.6导入rd_link_form :按照4.5得到的rd_link导入rd_link_form;
			initRdLinkForm(copVersionSchema,dbLinkName);
			//4.7导入rd_link_name:按照4.5得到的rd_link导入rd_link_name;
			initRdLinkName(copVersionSchema, dbLinkName);
			//4.8导入rd_name:按照4.7得到的rd_link和rd_link_name 进行关联，得到要导入的rd_name的group_id；再关联月库的rd_name； 得到要导出的rd_name
			initRdName(copVersionSchema,dbLinkName);
			//4.9导入rd_link_limit:按照4.5得到的rd_link导入rd_link_limit;
			initRdLinkLimit(copVersionSchema, dbLinkName);
			//4.10备份ix_poi为ix_poi_back(可以只备份pid,x_guid,y_guid,link_pid,name_groupid,side,pmesh_id),为后续的差分及生成履历做准备；
			backupIxPoi(copVersionSchema);
			//4.11备份ix_poi为ix_poi_flag_back；
			backupIxPoiFlag(copVersionSchema);
			//5.调用cop的批处理程序；
			callCopPackage(copVersionSchema);
			//6.差分ix_poi和ix_poi_back;生成差分履历；
			diff(copVersionSchema);
			//7.根据6的差分履历刷新月库,把6生成的履历搬移到月库；
			flushDiffLog(copVersionSchema,copVersionConn,dbLinkName,monthConn);
			log.info("flushGlinkDataLog over!");
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(copVersionConn);
			DbUtils.rollbackAndCloseQuietly(monthConn);
			log.error(e.getMessage());
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
			DbUtils.commitAndCloseQuietly(monthConn);
		}
		
	}
	
	private void initIxPoiFlag(OracleSchema copVersionSchema,String dbLinkName) throws SQLException {
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		try{
			String sql = "INSERT /*+append*/ INTO ix_poi_flag "+
					"SELECT * FROM ix_poi_flag@"+dbLinkName+" p "+
					"WHERE p.poi_pid IN (SELECT t.pid FROM "+tempPoiGLinkTab+"@"+dbLinkName+" t) ";
			new QueryRunner().update(copVersionConn, sql); 
		}catch (Exception e){
			DbUtils.rollback(copVersionConn);
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
		}

	}
	private void moveDiffLog(Connection copVersionConn ,String dbLinkName,String tempTab)  throws Exception {
		QueryRunner run = new QueryRunner();
		try{
			run.update(copVersionConn, actionSql(dbLinkName,tempTab));
			run.update(copVersionConn,operationSql(dbLinkName,tempTab));
			run.update(copVersionConn, dayReleaseSql(dbLinkName,tempTab));
			run.update(copVersionConn, detailSql(dbLinkName,tempTab));
			run.update(copVersionConn, gridSql(dbLinkName,tempTab));	
		}catch(Exception e){
			//DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
			//DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	private void flushDiffLog(OracleSchema copVersionSchema,Connection copVersionConn,String dbLinkName,Connection monthConn) throws Exception {
		
		try{
			FlushResult flushResult= new CopVersion2MonLogFlusher(copVersionSchema,copVersionConn,monthConn,true,"day2MonSync").flush();
			if(0==flushResult.getTotal()){
				log.info("没有符合条件的履历，不执行引导LINK批处理，返回");
			}else{
				log.info("开始将履历搬到月库：logtotal:"+flushResult.getTotal());
				moveDiffLog(copVersionConn,dbLinkName,flushResult.getTempFailLogTable());
			}
		}catch(Exception e){
			throw e;
		}
	}
	private void diff(OracleSchema copVersionSchema) throws Exception {
		//完成数据差分，及履历写入
		diffPoiGData(copVersionSchema);
		//待确认，是否需要差分处理
		//diffPoiFlagGData(copVersionSchema);
		//更新LOG_DETAIL_GRID
		updateLogGrid(copVersionSchema);
	}

	private void updateLogGrid(OracleSchema copVersionSchema) throws Exception {
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		//查询需要赋值的数据
		Map<Long,String> grids = getLogDetailDridData(copVersionConn);
		QueryRunner run = new QueryRunner();

		try {
			for (long pid : grids.keySet()) {
				String sql = "UPDATE LOG_DETAIL_GRID SET GRID_ID= "+grids.get(pid)+" WHERE log_row_id IN (SELECT ROW_ID FROM LOG_DETAIL WHERE OB_PID="+pid+")";
				run.execute(copVersionConn, sql);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(copVersionConn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
		}
	}
	private Map<Long,String> getLogDetailDridData(Connection conn) throws Exception {

		String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		GlmGridCalculator calculator = GlmGridCalculatorFactory.getInstance().create(gdbVersion);
		Statement sourceStmt =null;
		ResultSet rs = null;
		try{
			sourceStmt = conn.createStatement();
			String sql = "SELECT DISTINCT LD.OB_PID FROM LOG_DETAIL LD,LOG_DETAIL_GRID LDG WHERE LD.ROW_ID=LDG.LOG_ROW_ID ";
			rs = sourceStmt.executeQuery(sql);
			Map<Long,String> grids = new HashMap<Long,String>();
			while(rs.next()){
				String grid =calculator.calc("IX_POI","PID",rs.getLong("OB_PID"),conn).getGrids()[0];
				grids.put(rs.getLong("OB_PID"), grid);
			}
			return grids;
		}finally{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(sourceStmt);
		}
		
	}

	private void diffPoiGData(OracleSchema copVersionSchema) throws Exception {
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		StringBuilder sb = new StringBuilder();
        //该sql语句功能：实现对子版本库中数据进行差分，且将差分履历写入到5张履历表中
		sb.append(" INSERT ALL ");
		sb.append("     INTO LOG_ACTION (ACT_ID,US_ID,OP_CMD) VALUES (ACT_ID,0,'FM-BAT-M01-07') ");
		sb.append(" 	INTO LOG_OPERATION (OP_ID,ACT_ID,OP_DT,OP_SEQ) VALUES (OP_ID,ACT_ID,SYSTIMESTAMP,LOG_OP_SEQ.NEXTVAL)");
		sb.append(" 	INTO LOG_DAY_RELEASE (OP_ID) VALUES (OP_ID)");
		sb.append(" 	INTO LOG_DETAIL (OP_ID,ROW_ID,OB_NM,OB_PID,GEO_NM,GEO_PID,TB_NM,OLD,NEW,FD_LST,OP_TP,TB_ROW_ID)VALUES (OP_ID,S_GUID,'IX_POI',PID,'IX_POI',PID,'IX_POI',OLD_VALUE,NEW_VALUE,PD_LST,3,RN)");
		sb.append(" 	INTO LOG_DETAIL_GRID (LOG_ROW_ID,GRID_ID,GRID_TYPE) VALUES (S_GUID,0,1)");
		sb.append(" WITH TAB1 AS");
		sb.append("  (SELECT IX.PID,IX.ROW_ID RN,");
		sb.append(" 	(CASE WHEN IX.X_GUIDE <> BAK.X_GUIDE THEN ',\"X_GUIDE\"' ELSE NULL END) ||");
		sb.append(" 	(CASE WHEN IX.Y_GUIDE <> BAK.Y_GUIDE THEN ',\"Y_GUIDE\"' ELSE NULL END) ||");
		sb.append(" 	(CASE WHEN IX.LINK_PID <> BAK.LINK_PID THEN ',\"LINK_PID\"' ELSE NULL END) ||");
		sb.append(" 	(CASE WHEN IX.NAME_GROUPID <> BAK.NAME_GROUPID THEN ',\"NAME_GROUPID\"' ELSE NULL END) ||");
		sb.append(" 	(CASE WHEN IX.SIDE <> BAK.SIDE THEN ',\"SIDE\"' ELSE NULL END) ||");
		sb.append(" 	(CASE WHEN IX.PMESH_ID <> BAK.PMESH_ID THEN ',\"PMESH_ID\"' ELSE NULL END) PD_LST,");
		sb.append(" 	(CASE WHEN IX.X_GUIDE <> BAK.X_GUIDE THEN ',\"X_GUIDE\":' || BAK.X_GUIDE ELSE NULL END) ||");
		sb.append(" 	(CASE WHEN IX.Y_GUIDE <> BAK.Y_GUIDE THEN ',\"Y_GUIDE\":' || BAK.Y_GUIDE ELSE NULL END) ||");
		sb.append(" 	(CASE WHEN IX.LINK_PID <> BAK.LINK_PID THEN  ',\"LINK_PID\":' || BAK.LINK_PID ELSE NULL END) ||");
		sb.append(" 	(CASE WHEN IX.NAME_GROUPID <> BAK.NAME_GROUPID THEN ',\"NAME_GROUPID\":' || BAK.NAME_GROUPID ELSE NULL END) ||");
		sb.append(" 	(CASE WHEN IX.SIDE <> BAK.SIDE THEN ',\"SIDE\":' || BAK.SIDE ELSE NULL END) ||");
		sb.append(" 	(CASE WHEN IX.PMESH_ID <> BAK.PMESH_ID THEN ',\"PMESH_ID\":' || BAK.PMESH_ID ELSE NULL END) OLD_VALUE,");
		sb.append(" 	(CASE WHEN IX.X_GUIDE <> BAK.X_GUIDE THEN ',\"X_GUIDE\":' || IX.X_GUIDE ELSE NULL END) ||");
		sb.append(" 	(CASE WHEN IX.Y_GUIDE <> BAK.Y_GUIDE THEN ',\"Y_GUIDE\":' || IX.Y_GUIDE ELSE NULL END) ||");
		sb.append(" 	(CASE WHEN IX.LINK_PID <> BAK.LINK_PID THEN ',\"LINK_PID\":' || IX.LINK_PID ELSE NULL END) ||");
		sb.append(" 	(CASE WHEN IX.NAME_GROUPID <> BAK.NAME_GROUPID THEN ',\"NAME_GROUPID\":' || IX.NAME_GROUPID ELSE NULL END) ||");
		sb.append(" 	(CASE WHEN IX.SIDE <> BAK.SIDE THEN ',\"SIDE\":' || IX.SIDE ELSE NULL END) ||");
		sb.append(" 	(CASE WHEN IX.PMESH_ID <> BAK.PMESH_ID THEN ',\"PMESH_ID\":' || IX.PMESH_ID ELSE NULL END) NEW_VALUE");
		sb.append("  FROM IX_POI IX, IX_POI_BACK BAK ");
		sb.append("  WHERE IX.PID = BAK.PID ");
		sb.append(" 	AND (IX.X_GUIDE <> BAK.X_GUIDE OR IX.Y_GUIDE <> BAK.Y_GUIDE OR");
		sb.append(" 		IX.LINK_PID <> BAK.LINK_PID OR IX.NAME_GROUPID <> BAK.NAME_GROUPID OR");
		sb.append(" 		IX.SIDE <> BAK.SIDE OR IX.PMESH_ID <> BAK.PMESH_ID)),");
		sb.append(" TAB2 AS");
		sb.append("  (SELECT /*+ no_merge */ PID,RN,'['||SUBSTR(PD_LST,2,LENGTH(PD_LST)-1)||']' PD_LST ,'{'||SUBSTR(OLD_VALUE,2,LENGTH(OLD_VALUE)-1)||'}' OLD_VALUE,'{'||SUBSTR(NEW_VALUE,2,LENGTH(NEW_VALUE)-1)||'}' NEW_VALUE,SYS_GUID() S_GUID,SYS_GUID() ACT_ID ,SYS_GUID() OP_ID FROM TAB1) ");
		sb.append("SELECT rn,S_GUID,ACT_ID,OP_ID,PID,OLD_VALUE,NEW_VALUE,PD_LST FROM tab2");
		
		try {
			new QueryRunner().update(copVersionConn, sb.toString()); 
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(copVersionConn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
		}
		
	}
	private void diffPoiFlagGData(OracleSchema copVersionSchema) throws Exception {
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		StringBuilder sb = new StringBuilder();
        //该sql语句功能：实现对子版本库中数据进行差分，且将差分履历写入到5张履历表中
		sb.append(" INSERT ALL ");
		sb.append("     INTO LOG_ACTION (ACT_ID,US_ID,OP_CMD) VALUES (ACT_ID,0,'FM-BAT-M01-07') ");
		sb.append(" 	INTO LOG_OPERATION (OP_ID,ACT_ID,OP_DT,OP_SEQ) VALUES (OP_ID,ACT_ID,SYSTIMESTAMP,LOG_OP_SEQ.NEXTVAL)");
		sb.append(" 	INTO LOG_DAY_RELEASE (OP_ID) VALUES (OP_ID)");
		sb.append(" 	INTO LOG_DETAIL (OP_ID,ROW_ID,OB_NM,OB_PID,GEO_NM,GEO_PID,TB_NM,OLD,NEW,FD_LST,OP_TP,TB_ROW_ID)VALUES (OP_ID,S_GUID,'IX_POI',POI_PID,'IX_POI',POI_PID,'IX_POI_FLAG',OLD_VALUE,NEW_VALUE,PD_LST,3,RN)");
		sb.append(" 	INTO LOG_DETAIL_GRID (LOG_ROW_ID,GRID_ID,GRID_TYPE) VALUES (S_GUID,0,1)");
		sb.append(" WITH TAB1 AS");
		sb.append("  (SELECT IX.POI_PID,IX.ROW_ID RN,");
		sb.append(" 	'\"FLAG_CODE\"' PD_LST,");
		sb.append(" 	'\"FLAG_CODE\":' || BAK.FLAG_CODE OLD_VALUE,");
		sb.append(" 	'\"FLAG_CODE\":' || IX.FLAG_CODE NEW_VALUE");
		sb.append(" 	FROM IX_POI_FLAG IX, IX_POI_FLAG_BACK BAK");
		sb.append(" 	WHERE IX.POI_PID = BAK.POI_PID");
		sb.append(" 	 AND IX.FLAG_CODE <> BAK.FLAG_CODE AND IX.ROW_ID = BAK.ROW_ID),");
		sb.append(" TAB2 AS");
		sb.append("  (SELECT /*+ no_merge */  POI_PID,RN,'['||PD_LST||']' PD_LST ,'{'||OLD_VALUE||'}' OLD_VALUE,'{'||NEW_VALUE||'}' NEW_VALUE,SYS_GUID() S_GUID,SYS_GUID() ACT_ID ,SYS_GUID() OP_ID FROM TAB1) ");
		sb.append(" SELECT rn,S_GUID, ACT_ID,OP_ID,POI_PID,OLD_VALUE,NEW_VALUE,PD_LST FROM tab2");

		try {
			new QueryRunner().update(copVersionConn, sb.toString()); 
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(copVersionConn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
		}
	}
	public void callCopPackage(OracleSchema copVersionSchema) throws Exception {
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		CallableStatement cs = null;
		long pid=0L;
		try{
			String sql = "{call POINT_FEATURE_BATCH.BATCH_IXPOI_LINK()}";
			cs = copVersionConn.prepareCall(sql);
			cs.execute();
		}catch(Exception e){
			log.error(e);
    		DbUtils.closeQuietly(cs);
    		DbUtils.rollbackAndCloseQuietly(copVersionConn);
		}finally{
    		DbUtils.closeQuietly(cs);
    		DbUtils.commitAndCloseQuietly(copVersionConn);
		}
		
	}
	
	private void backupIxPoi(OracleSchema copVersionSchema) throws Exception{
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		try{
			String sql = "INSERT /*+append*/ INTO ix_poi_back "+
					"SELECT p.PID,p.X_GUIDE,p.Y_GUIDE,p.LINK_PID,p.SIDE,p.NAME_GROUPID,p.PMESH_ID FROM ix_poi p";
			new QueryRunner().update(copVersionConn, sql); 	
		}catch (Exception e){
			DbUtils.rollbackAndCloseQuietly(copVersionConn);
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
		}	
	}
	
	private void backupIxPoiFlag(OracleSchema copVersionSchema) throws Exception{
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		try{
			String sql = "INSERT /*+append*/ INTO ix_poi_flag_back SELECT p.* FROM ix_poi_flag p";
			new QueryRunner().update(copVersionConn, sql);
		}catch (Exception e){
			DbUtils.rollbackAndCloseQuietly(copVersionConn);
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
		}

	}
	
	
	private void initRdLinkLimit(OracleSchema copVersionSchema, String dbLinkName) throws SQLException {
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		try{
			String sql = "insert /*+append*/ into rd_link_limit "
					+ " select l.* from rd_link_limit@"+dbLinkName +" l "
					+ " where l.link_pid in (select r.link_pid from rd_link r)";
			new QueryRunner().update(copVersionConn, sql); 
		}catch (Exception e){
			DbUtils.rollbackAndCloseQuietly(copVersionConn);
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
		}
	}
	private void initRdName(OracleSchema copVersionSchema, String dbLinkName) throws SQLException {
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		try{
			String sql = "insert /*+append*/ into ni_rd_name "
					+ "select n.* from rd_name@"+dbLinkName+" n "
					+ " where n.name_groupid in (select r.name_groupid from rd_link l,rd_link_name r where l.link_pid=r.link_pid )";
			new QueryRunner().update(copVersionConn, sql);
		}catch (Exception e){
			DbUtils.rollbackAndCloseQuietly(copVersionConn);
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
		}
	}
	private void initRdLinkName(OracleSchema copVersionSchema, String dbLinkName) throws SQLException{
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		try{
			String sql = "insert /*+append*/ into rd_link_name "
					+ " select n.* from rd_link_name@"+dbLinkName +" n "
					+ " where n.link_pid in (select r.link_pid from rd_link r)";
			new QueryRunner().update(copVersionConn, sql);
		}catch (Exception e){
			DbUtils.rollbackAndCloseQuietly(copVersionConn);
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
		}
	}
	private void initRdLinkForm(OracleSchema copVersionSchema, String dbLinkName) throws SQLException {
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		try{
			String sql = "insert /*+append*/ into rd_link_form "
					+ " select f.* from rd_link_form@"+dbLinkName +" f "
					+ " where f.link_pid in (select r.link_pid from rd_link r)";
			new QueryRunner().update(copVersionConn, sql); 
		}catch (Exception e){
			DbUtils.rollbackAndCloseQuietly(copVersionConn);
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
		}
	}
	private String createTempLinkGLinkTable(Connection conn) throws Exception {
		String tableName = "tmp_l_glink"+(new SimpleDateFormat("yyyyMMddhhmmssS").format(new Date()));
		String sql ="create table "+tableName+" (link_pid number(10))";
		new QueryRunner().update(conn, sql);
		return tableName;
	}
	private void initRdLink(OracleSchema copVersionSchema, String dbLinkName) throws Exception {
		
		Connection conn = monthDbSchema.getPoolDataSource().getConnection();
		String tempLinkGLinkTab  = createTempLinkGLinkTable(conn);
		
		StringBuilder sb = new StringBuilder();
		sb.append(" INSERT /*+append*/ INTO "+tempLinkGLinkTab+" SELECT L.link_pid FROM ix_poi P,rd_link L ");
		sb.append(" WHERE P.pid IN (SELECT t.pid FROM "+tempPoiGLinkTab+" t)");
		sb.append(" AND SDO_NN(L.GEOMETRY,");
		sb.append(" NAVI_GEOM.CREATEPOINT(P.X_GUIDE, P.Y_GUIDE),");
		sb.append(" 'SDO_NUM_RES=4 DISTANCE=80000 UNIT=METER') = 'TRUE'");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();
		}catch(Exception e){
			log.error(e.getMessage());
			e.printStackTrace();
			throw e;
		}finally{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		try{
			String sql = "INSERT /*+append*/ INTO RD_LINK "
					+ "SELECT r.* FROM RD_LINK@"+dbLinkName+" r  "
					+ "WHERE r.link_pid IN (SELECT t.link_pid FROM "+tempLinkGLinkTab+"@"+dbLinkName+" t) ";
			this.log.debug("sql:"+sql);
			new QueryRunner().update(copVersionConn, sql);
		
		}catch (Exception e){
			DbUtils.rollback(copVersionConn);
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
		}
		
	}
	private void initIxPoiAddress(OracleSchema copVersionSchema, String dbLinkName) throws Exception {
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		try{
			String sql = "INSERT /*+append*/ INTO ix_poi_address "+
					"SELECT * FROM ix_poi_address@"+dbLinkName+" p "+
					"WHERE p.poi_pid IN (SELECT t.pid FROM "+tempPoiGLinkTab+"@"+dbLinkName+" t) ";
					new QueryRunner().update(copVersionConn, sql); 
		}catch (Exception e){
			DbUtils.rollbackAndCloseQuietly(copVersionConn);
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
		}
	}
	private void initIxPoi(OracleSchema copVersionSchema,String dbLinkName) throws Exception {
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		try{
			String sql = "INSERT /*+append*/ INTO ix_poi "+
					"SELECT * FROM ix_poi@"+dbLinkName+" p "+
					"WHERE p.pid IN (SELECT t.pid FROM "+tempPoiGLinkTab+"@"+dbLinkName+" t)";
					new QueryRunner().update(copVersionConn, sql); 
		}catch (Exception e){
			DbUtils.rollbackAndCloseQuietly(copVersionConn);
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
		}	
	}
	private void importCopPck(OracleSchema copVersionSchema) throws Exception{
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		try{
			String pckFile = "/com/navinfo/dataservice/importcore/resources/point_feature_batch.pck";
			SqlExec sqlExec = new SqlExec(copVersionConn);
			sqlExec.execute(pckFile);
		}catch (Exception e){
			DbUtils.rollbackAndCloseQuietly(copVersionConn);
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
		}
	}
	
	private void exeTabelCreateSql(OracleSchema copVersionSchema) throws Exception {
		Connection copVersionConn = copVersionSchema.getPoolDataSource().getConnection();
		try{
			String sqlFile = "/com/navinfo/dataservice/importcore/resources/poi_guide_link_batch_db_create.sql";
			SqlExec sqlExec = new SqlExec(copVersionConn);
			sqlExec.execute(sqlFile);
		}catch (Exception e){
			DbUtils.rollbackAndCloseQuietly(copVersionConn);
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(copVersionConn);
		}
	}
	
	/**
	 * 子版本库上创建月库的db_link
	 * @param copVersionSchema
	 * @return dbLinkName
	 * @throws Exception
	 */
	public String createDbLink(OracleSchema copVersionSchema) throws Exception{
		DbLinkCreator cr = new DbLinkCreator();
		String dbLinkName = "";
		try{
			dbLinkName = monthDbSchema.getConnConfig().getUserName()+"_"+RandomUtil.nextNumberStr(4);
			cr.create(dbLinkName, false, copVersionSchema.getPoolDataSource(), monthDbSchema.getConnConfig().getUserName(),
					monthDbSchema.getConnConfig().getUserPasswd(), monthDbSchema.getConnConfig().getServerIp(), 
					String.valueOf(monthDbSchema.getConnConfig().getServerPort()), monthDbSchema.getConnConfig().getServiceName());
			return dbLinkName;
		}catch (Exception e){
			log.error(e.getMessage(), e);
			throw e;
		}
	}
	public DbInfo createCopVersion() throws Exception {
		//创建cop子版本库
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
			DbUtils.rollbackAndCloseQuietly(sysConn);
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(), e);
		}finally{
			DbUtils.commitAndCloseQuietly(sysConn);
		}
		return copDb;
		
	}
	public static void main(String args[]) throws Exception{
		PoiGuideLinkBatch batch = new PoiGuideLinkBatch(null,null);
		System.out.print(batch.tempPoiGLinkTab);
	}
	private String actionSql(String dbLinkName,String tempFailLogTable){
		StringBuilder sb = new StringBuilder();
		sb.append("MERGE INTO log_action@");
		sb.append(dbLinkName);
		sb.append(" tt USING (select la.* from log_action la where la.act_id in (select distinct lp.act_id from log_operation lp ");
		if(StringUtils.isNotEmpty(tempFailLogTable)){
			sb.append(" where NOT EXISTS(SELECT 1 FROM ");
			sb.append(tempFailLogTable);
			sb.append(" f WHERE f.OP_ID=LP.OP_ID)");
		}
		sb.append(" ) ) TP");
		sb.append(" ON (TP.ACT_ID = TT.ACT_ID)");
		sb.append(" WHEN NOT MATCHED THEN INSERT");
		sb.append(" (ACT_ID, US_ID, OP_CMD, SRC_DB, STK_ID) VALUES");
		sb.append(" (TP.ACT_ID, TP.US_ID, TP.OP_CMD, TP.SRC_DB, TP.STK_ID)");
		return sb.toString();
	}
	private String detailSql(String dbLinkName,String tempFailLogTable){
		StringBuilder sb = new StringBuilder();
		sb.append("insert into log_detail@");
		sb.append(dbLinkName);
		sb.append(" select l.* from log_detail l ");
		if(StringUtils.isNotEmpty(tempFailLogTable)){
			sb.append(" where NOT EXISTS(SELECT 1 FROM ");
			sb.append(tempFailLogTable);
			sb.append(" f WHERE f.row_id=l.row_Id)");
		}
		return sb.toString();
	}
	private String gridSql(String dbLinkName,String tempFailLogTable){
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO LOG_DETAIL_GRID@");
		sb.append(dbLinkName);
		sb.append(" SELECT P.* FROM LOG_DETAIL_GRID P,LOG_DETAIL L ");
		sb.append("  WHERE L.ROW_ID=P.LOG_ROW_ID");
		if(StringUtils.isNotEmpty(tempFailLogTable)){
			sb.append(" AND NOT EXISTS(SELECT 1 FROM ");
			sb.append(tempFailLogTable);
			sb.append(" f WHERE f.row_id=l.row_Id)");
		}
		return sb.toString();
				
	}
	
	private String operationSql(String dbLinkName,String tempFailLogTable){
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO LOG_OPERATION@");
		sb.append(dbLinkName);
		sb.append("(OP_ID,ACT_ID,OP_DT,OP_SEQ) SELECT T.OP_ID,T.ACT_ID,SYSDATE,LOG_OP_SEQ.NEXTVAL@"+dbLinkName+" FROM (SELECT L.OP_ID,L.ACT_ID FROM LOG_OPERATION L ");
		if(StringUtils.isNotEmpty(tempFailLogTable)){
			sb.append(" WHERE NOT EXISTS(SELECT 1 FROM ");
			sb.append(tempFailLogTable);
			sb.append(" F WHERE F.OP_ID=L.OP_ID)");
		}
		sb.append(" ORDER BY L.OP_DT) T");
		return sb.toString();
	}

	private String dayReleaseSql(String dbLinkName,String tempFailLogTable){
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO LOG_DAY_RELEASE@");
		sb.append(dbLinkName);
		sb.append("(OP_ID) SELECT distinct L.OP_ID FROM ");
		sb.append(" LOG_DETAIL L  ");
		if(StringUtils.isNotEmpty(tempFailLogTable)){
			sb.append("WHERE  NOT EXISTS(SELECT 1 FROM ");
			sb.append(tempFailLogTable);
			sb.append(" F WHERE F.ROW_ID=L.ROW_ID)");
		}
		return sb.toString();
	}
	
}
