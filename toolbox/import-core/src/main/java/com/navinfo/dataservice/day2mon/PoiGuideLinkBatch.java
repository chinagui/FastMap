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

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.SingleBatchSelRsHandler;
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
	public PoiGuideLinkBatch(OperationResult opResult, Connection conn) {
		super();
		this.opResult = opResult;
		this.conn = conn;
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
		//4.1创建子版本库并初始化子版本数据；
		//4.2创建需要的表结构:ix_poi,ix_poi_address,rd_link,rd_link_form,rd_name
		exeTabelCreateSql(copVersionSchema);
		//4.3导入ix_poi:根据tempPoiGLinkTab 进行关联
		initIxPoi(copVersionSchema,tempPoiGLinkTab);
		//4.4导入ix_poi_address:根据tempPoiGLinkTab 进行关联
		initIxPoiAddress(copVersionSchema,tempPoiGLinkTab);
		//4.5导入rd_link:按照4.3的poi进行扩圈，计算要导入的rd_link的pid
		initRdLink(copVersionSchema,tempPoiGLinkTab);
		//4.6导入rd_link_form :按照4.5得到的rd_link导入rd_link_form;
		initRdLinkForm(copVersionSchema,tempPoiGLinkTab);
		//4.7导入rd_name:按照4.5得到的rd_link和rd_link_name 进行关联，得到要导入的rd_name的group_id；再关联月库的rd_name； 得到要导出的rd_name
		initRdName(copVersionSchema,tempPoiGLinkTab);
		//4.8备份ix_poi为ix_poi_back(可以只备份pid,x_guid,y_guid,link_pid,name_groupid,side,pmesh_id),为后续的差分及生成履历做准备；
		backupIxPoi(copVersionSchema);
		//5.调用cop的批处理程序；
		callCopPackage(copVersionSchema);
		//6.差分ix_poi和ix_poi_back;生成差分履历；
		diff(copVersionSchema);
		//7.根据6的差分履历刷新月库；
		flushDiffLog(copVersionSchema);
		//8.把6生成的履历搬移到月库
		moveDiffLog(copVersionSchema);
		
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
	private void callCopPackage(OracleSchema copVersionSchema) throws Exception {
		
		Connection conn = copVersionSchema.getPoolDataSource().getConnection();
		
		CallableStatement cs = null;
		long pid=0L;
		try{
			String sql = "{call POINT_FEATURE_BATCH.BATCH_IXPOI_LINK()}";
			cs = conn.prepareCall(sql);
			cs.execute();
			conn.commit();
		}catch(Exception e){
			log.error(e);
    		DbUtils.closeQuietly(cs);
			DbUtils.rollbackAndCloseQuietly(conn);
		}finally{
    		DbUtils.closeQuietly(cs);
    		DbUtils.closeQuietly(conn);
		}
		
	}
	
	private void backupIxPoi(OracleSchema copVersionSchema) {
		// TODO Auto-generated method stub
		
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
	private void initIxPoiAddress(OracleSchema copVersionSchema, String tempPoiGLinkTab) {
		// TODO Auto-generated method stub
		
	}
	private void initIxPoi(OracleSchema copVersionSchema, String tempPoiGLinkTab) {
		// TODO Auto-generated method stub
		
	}
	private void exeTabelCreateSql(OracleSchema copVersionSchema) throws Exception {
		String sqlFile = "/com/navinfo/dataservice/scripts/resources/poi_guide_link_batch_db_create.sql";
		SqlExec sqlExec = new SqlExec(copVersionSchema.getPoolDataSource().getConnection());
		sqlExec.execute(sqlFile);
	}
	private DbInfo createCopVersion() {
		// TODO 创建cop子版本库
		return null;
		
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
		PoiGuideLinkBatch batch = new PoiGuideLinkBatch(null,null);
		System.out.print(batch.createTempPoiGLinkTable());
	}
	
}
