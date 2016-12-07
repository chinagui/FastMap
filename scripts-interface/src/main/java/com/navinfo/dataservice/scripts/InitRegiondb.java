package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.BizType;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.DbServerType;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.expcore.ExportConfig;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DbLinkCreator;
import com.navinfo.navicommons.database.sql.PackageExec;
import com.navinfo.navicommons.database.sql.SqlExec;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;

/**
 * @ClassName: InitProjectScriptsInterface
 * @author Xiao Xiaowen
 * @date 2016-1-15 下午3:40:32
 * @Description: TODO
 */
public class InitRegiondb {

	public static JSONObject execute(JSONObject request) throws Exception{
		JSONObject response = new JSONObject();
		Connection conn = null;
		try {
			String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
			Assert.notNull(gdbVersion, "gdbVersion不能为空,检查是否sys_config表中未配置当前gdb版本");
			DbInfo fmgdb = DbService.getInstance().getOnlyDbByBizType("nationRoad");
//			Assert.notNull(fmgdbId, "fmgdbId不能为空");
//			int fmMetaId = request.getInt("fmMetaId");
//			Assert.notNull(fmMetaId, "fmMetaId不能为空");
			JSONArray regionIds = (JSONArray) request.get("regionIds");
			Assert.notNull(regionIds, "regionIds不能为空");
			String userNamePrefix = (String) request.get("userNamePrefix");
			Assert.notNull(userNamePrefix, "userNamePrefix不能为空");
			
			int meshExtendCount = 1;
			if(request.containsKey("meshExtendCount")){
				meshExtendCount = request.getInt("meshExtendCount");
			}
			
			conn = DBConnector.getInstance().getManConnection();
			//得到图幅号
			Map<Integer,List<String>> regionMeshMap = getRegionMeshMap(conn,regionIds);
			for(Integer key:regionMeshMap.keySet()){
				//先写入region表
				insertRegions(conn,key);
				//创建库
				Set<String> meshes = new HashSet<String>(regionMeshMap.get(key));
				Set<String> extendMeshes = null;
				if(meshExtendCount>0){
					extendMeshes = MeshUtils.getNeighborMeshSet(meshes,1);
				}else{
					extendMeshes=meshes;
				}
				//大区库不直接做检查批处理，不维护M_MESH_TYPE表
				//创建日db
				JobInfo info1 = new JobInfo(0, "");
				info1.setType("createDb");
				JSONObject req1 = new JSONObject();
				req1.put("dbName", "orcl");
				req1.put("serverType", DbServerType.TYPE_ORACLE);
				req1.put("userName", userNamePrefix+"_d_"+key);
				req1.put("userPasswd", userNamePrefix+"_d_"+key);
				req1.put("bizType", "regionRoad");
				req1.put("descp", "region db");
				req1.put("gdbVersion", gdbVersion);
				info1.setRequest(req1);
				AbstractJob job1 = JobCreateStrategy.createAsMethod(info1);
				job1.run();
				if(job1.getJobInfo().getStatus()!=3){
					String msg = (job1.getException()==null)?"未知错误。":"错误："+job1.getException().getMessage();
					throw new Exception("创建日库DB过程中job内部发生"+msg);
				}
				int dbDay = job1.getJobInfo().getResponse().getInt("outDbId");
				response.put("region_"+key+"_day_db", dbDay);
				JobInfo info2 = new JobInfo(0,"");
				info2.setType("gdbExport");
				JSONObject req2 = new JSONObject();
				req2.put("gdbVersion", gdbVersion);
				req2.put("sourceDbId", fmgdb.getDbId());
				req2.put("condition", ExportConfig.CONDITION_BY_MESH);
				req2.put("conditionParams", JSONArray.fromObject(extendMeshes));
				req2.put("featureType", GlmTable.FEATURE_TYPE_ALL);
				req2.put("dataIntegrity", false);
				req2.put("targetDbId", dbDay);
				info2.setRequest(req2);
				AbstractJob job2 = JobCreateStrategy.createAsMethod(info2);
				job2.run();
				if(job2.getJobInfo().getStatus()!=3){
					String msg = (job2.getException()==null)?"未知错误。":"错误："+job2.getException().getMessage();
					throw new Exception("日库导数据过程中job内部发生"+msg);
				}
				response.put("region_"+key+"_day_exp", "success");
				//给日库和月库安装包
				installPckUtils(dbDay,1);
				response.put("region_"+key+"_day_utils", "success");
				//创建月db
				JobInfo info3 = new JobInfo(0, "");
				info3.setType("createDb");
				JSONObject req3 = new JSONObject();
				req3.put("dbName", "orcl");
				req3.put("userName", userNamePrefix+"_m_"+key);
				req3.put("userPasswd", userNamePrefix+"_m_"+key);
				req3.put("bizType", "regionRoad");
				req3.put("descp", "region db");
				req3.put("gdbVersion", gdbVersion);
				req3.put("serverType", DbServerType.TYPE_ORACLE);
				info3.setRequest(req3);
				AbstractJob job3 = JobCreateStrategy.createAsMethod(info3);
				job3.run();
				if(job3.getJobInfo().getStatus()!=3){
					String msg = (job3.getException()==null)?"未知错误。":"错误："+job3.getException().getMessage();
					throw new Exception("创建月库DB过程中job内部发生"+msg);
				}
				int dbMonth = job3.getJobInfo().getResponse().getInt("outDbId");
				response.put("region_"+key+"_month", dbMonth);
				JobInfo info4 = new JobInfo(0,"");
				info4.setType("gdbFullCopy");
				JSONObject req4 = new JSONObject();
				req4.put("sourceDbId", dbDay);
				req4.put("targetDbId", dbMonth);
				req4.put("featureType", GlmTable.FEATURE_TYPE_ALL);
				req4.put("gdbVersion", gdbVersion);
				info4.setRequest(req4);
				AbstractJob job4 = JobCreateStrategy.createAsMethod(info4);
				job4.run();
				if(job4.getJobInfo().getStatus()!=3){
					String msg = (job4.getException()==null)?"未知错误。":"错误："+job4.getException().getMessage();
					throw new Exception("月库导数据过程中job内部发生"+msg);
				}
				response.put("region_"+key+"_month_exp", "success");
				installPckUtils(dbMonth,2);
				response.put("region_"+key+"_month_utils", "success");
				//写入dbID
				insertDbIds(conn,key,dbDay,dbMonth);
				//更新grid表
				insertGrids(conn,key);
				//维护情报的block
				maintainInfoBlock(conn,key);
				conn.commit();
				response.put("region_"+key+"_man_rows", "success");
			}
			response.put("msg", "执行成功");
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			response.put("msg", "ERROR:" + e.getMessage());
			throw e;
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return response;
	}
	
	private static void createMetaDbLink(DataSource dataSource)throws Exception{
		DbLinkCreator cr = new DbLinkCreator();
		DbInfo metaDb = DbService.getInstance().getOnlyDbByBizType(BizType.DB_META_ROAD);
		cr.create("DBLINK_RMS", false, dataSource,metaDb.getDbUserName(),metaDb.getDbUserPasswd(),metaDb.getDbServer().getIp(),String.valueOf(metaDb.getDbServer().getPort()),metaDb.getDbServer().getServiceName());
	}

	
	/**
	 * @Title: createRegionDbLinks
	 * @Description: 创建大区库的dblink
	 * @param regionDbId
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月11日 下午8:40:41 
	 */
	private static void createRegionDbLinks(DbInfo dbRegion)throws Exception{
		DbLinkCreator cr = new DbLinkCreator();
		Connection metaConn = null;
		try{
			DbInfo db = DbService.getInstance()
					.getOnlyDbByBizType(BizType.DB_META_ROAD); //获取元数据库的连接参数
			
			DbConnectConfig connConfig = DbConnectConfig.createConnectConfig(db.getConnectParam());
			DataSource metaDataSource =MultiDataSourceFactory.getInstance().getDataSource(connConfig);
			DataSource regDdataSource = MultiDataSourceFactory.getInstance().getDataSource(DbConnectConfig.createConnectConfig(dbRegion.getConnectParam())); //获取大区库的数据源
			metaConn = metaDataSource.getConnection();//获取元数据库的连接
			//创建元数据库dblink
			String dbLinkName = null;
			String dbUserName = dbRegion.getDbUserName();
			if(dbUserName.contains("_d_")){
				dbLinkName = "d_"+dbUserName.split("_d_")[1];
			}else if(dbUserName.contains("_m_")){
				dbLinkName = "m_"+dbUserName.split("_m_")[1];
			}
			if(dbLinkName != null && StringUtils.isNotEmpty(dbLinkName)){
				cr.create("RG_DBLINK_"+dbLinkName, false, metaDataSource,dbRegion.getDbUserName(),dbRegion.getDbUserPasswd(),dbRegion.getDbServer().getIp(),String.valueOf(dbRegion.getDbServer().getPort()),dbRegion.getDbServer().getServiceName());
				metaConn.commit();
			}
		}finally{
			DbUtils.closeQuietly(metaConn);
		}
		 
	}
	
	
	/**
	 * @Title: installPckUtils
	 * @Description: (修改)(在初始化日大区库时增加创建元数据库对大区库的dblink)
	 * @param dbId
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月11日 下午8:46:15 
	 */
	private static void installPckUtils(int dbId,int dbType)throws Exception{
		Connection conn = null;
		try{
			DbInfo db = DbService.getInstance()
					.getDbById(dbId);
			DbConnectConfig connConfig = DbConnectConfig.createConnectConfig(db.getConnectParam());
			//创建元数据库dblink
			createMetaDbLink(MultiDataSourceFactory.getInstance().getDataSource(connConfig));
			//************2016.11.11 zl****************
			//在元数据库中创建大区库的dblink
			createRegionDbLinks(db);
			
			conn = MultiDataSourceFactory.getInstance().getDataSource(connConfig).getConnection();
			//修改log_action默认值
			new QueryRunner().execute(conn, "ALTER TABLE LOG_ACTION MODIFY SRC_DB DEFAULT "+dbType);
			//
			SqlExec sqlExec = new SqlExec(conn);
			String sqlFile = "/com/navinfo/dataservice/scripts/resources/init_edit_tables.sql";
			sqlExec.executeIgnoreError(sqlFile);
			String metaFile = "/com/navinfo/dataservice/scripts/resources/mv_rel_rdname_meta.sql";
			sqlExec.execute(metaFile);
			
			PackageExec packageExec = new PackageExec(conn);
			String spatialIndexSql = "/com/navinfo/dataservice/scripts/resources/create_spatial_utils_and_rebuild.sql";
			packageExec.execute(spatialIndexSql);
			String pckFile = "/com/navinfo/dataservice/scripts/resources/prj_utils.pck";
			packageExec.execute(pckFile);
			String pckFile2 = "/com/navinfo/dataservice/scripts/resources/create_type_function.sql";
			packageExec.execute(pckFile2);
//			String pyutils = "/com/navinfo/dataservice/scripts/resources/pyutils.pck";
//			packageExec.execute(pyutils,"UTF-8");
			conn.commit();
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	
	private static Map<Integer,List<String>> getRegionMeshMap(Connection conn,Collection<Integer> regionIds)throws Exception{

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT C.REGION_ID,P.MESH FROM CP_REGION_PROVINCE C,CP_MESHLIST@METADB_LINK P WHERE C.ADMINCODE=P.ADMINCODE AND C.REGION_ID IN (");
		sb.append(StringUtils.join(regionIds, ","));
		sb.append(")");
		QueryRunner run = new QueryRunner();
		return run.query(conn, sb.toString(), new ResultSetHandler<Map<Integer,List<String>>>(){

			@Override
			public Map<Integer, List<String>> handle(ResultSet rs) throws SQLException {
				Map<Integer,List<String>> results = new HashMap<Integer,List<String>>();
				while(rs.next()){
					int regionId = rs.getInt("REGION_ID");
					List<String> meshes = results.get(regionId);
					if(meshes==null){
						meshes = new ArrayList<String>();
						meshes.add(rs.getString("MESH"));
						results.put(regionId, meshes);
					}else{
						meshes.add(rs.getString("MESH"));
					}
				}
				return results;
			}
			
		});
	}
	private static void insertRegions(Connection conn,int regionId)throws Exception{
		String sql = "INSERT INTO REGION(REGION_ID)VALUES(?)";
		new QueryRunner().update(conn, sql, regionId);
	}
	
	private static void insertGrids(Connection conn,int regionId)throws Exception{
		QueryRunner run = new QueryRunner();
		String sql = "UPDATE GRID G SET G.REGION_ID=? WHERE TRUNC(G.GRID_ID/100) IN (SELECT P.MESH FROM CP_MESHLIST@METADB_LINK P,CP_REGION_PROVINCE T WHERE P.ADMINCODE=T.ADMINCODE AND T.REGION_ID=?)";
		run.update(conn, sql, regionId,regionId);
		String sql2 = "UPDATE GRID_LOCK_DAY SET REGION_ID=?,HANDLE_REGION_ID=? WHERE GRID_ID IN (SELECT GRID_ID FROM GRID WHERE REGION_ID=?)";
		run.update(conn, sql2, regionId,regionId,regionId);
		String sql3 = "UPDATE GRID_LOCK_MONTH SET REGION_ID=?,HANDLE_REGION_ID=? WHERE GRID_ID IN (SELECT GRID_ID FROM GRID WHERE REGION_ID=?)";
		run.update(conn, sql3, regionId,regionId,regionId);
	}

	private static void insertDbIds(Connection conn,int regionId,int dayDbId,int monthDbId)throws Exception{
		String sql = "UPDATE REGION SET DAILY_DB_ID=?,MONTHLY_DB_ID=? WHERE REGION_ID=?";
		new QueryRunner().update(conn, sql,dayDbId,monthDbId,regionId);
	}
	private static void maintainInfoBlock(Connection conn,int regionId)throws Exception{
		QueryRunner run = new QueryRunner();
		String sql = "INSERT INTO BLOCK (BLOCK_ID,CITY_ID,GEOMETRY,PLAN_STATUS,REGION_ID) VALUES (BLOCK_SEQ.NEXTVAL,100002,NULL,0,?)";
		run.update(conn, sql, regionId);
		String sql2 = "INSERT INTO BLOCK_GRID_MAPPING (GRID_ID,BLOCK_ID) SELECT G.GRID_ID,B.BLOCK_ID FROM BLOCK B,GRID G WHERE B.REGION_ID=G.REGION_ID AND B.CITY_ID=100002 AND B.REGION_ID=?";
		run.update(conn, sql2,regionId);
	}
	
	
	private static void testExeSqlOrPck(){
		Connection conn = null;
		try{
			JobScriptsInterface.initContext();
			DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
			DbInfo db = datahub.getDbById(19);
			OracleSchema schema = new OracleSchema(DbConnectConfig.createConnectConfig(db.getConnectParam()));
			conn = schema.getDriverManagerDataSource().getConnection();
//			SqlExec sqlExec = new SqlExec(conn);
			PackageExec packageExec = new PackageExec(conn);
//			String sqlFile = "/com/navinfo/dataservice/scripts/resources/create_type_function.sql";

			String sqlFile = "/com/navinfo/dataservice/scripts/resources/prj_utils.pck";
//			sqlExec.executeIgnoreError(sqlFile);
			packageExec.execute(sqlFile,"UTF-8");
			conn.commit();
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	
	public static void main(String[] args){
//		testExeSqlOrPck();
	testInstallPcks(111);
	}
	
	private static void testInstallPcks(int dbId){
		Connection conn = null;
		
			DbInfo db;
			try {
				db = DbService.getInstance()
						.getDbById(dbId);
				DbConnectConfig connConfig = DbConnectConfig.createConnectConfig(db.getConnectParam());
				//************2016.11.11 zl****************
				//在元数据库中创建大区库的dblink
				createRegionDbLinks(db);
				
			} catch (DataHubException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	}
}
