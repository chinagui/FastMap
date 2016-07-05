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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
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
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.expcore.ExportConfig;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.navicommons.database.QueryRunner;
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
			
			conn = DBConnector.getInstance().getManConnection();
			//得到图幅号
			Map<Integer,List<String>> regionMeshMap = getRegionMeshMap(conn,regionIds);
			for(Integer key:regionMeshMap.keySet()){
				//先写入region表
				insertRegions(conn,key);
				//创建库
				Set<String> meshes = new HashSet<String>(regionMeshMap.get(key));
				Set<String> extendMeshes = MeshUtils.getNeighborMeshSet(meshes,1);
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
				if(job1.getJobInfo().getResponse().getInt("exeStatus")!=3){
					throw new Exception("job1执行失败");
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
				if(job2.getJobInfo().getResponse().getInt("exeStatus")!=3){
					throw new Exception("job2执行失败");
				}
				response.put("region_"+key+"_day_exp", "success");
				//给日库和月库安装包
				installPckUtils(dbDay);
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
				if(job3.getJobInfo().getResponse().getInt("exeStatus")!=3){
					throw new Exception("job3执行失败");
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
				if(job4.getJobInfo().getResponse().getInt("exeStatus")!=3){
					throw new Exception("job4执行失败");
				}
				response.put("region_"+key+"_month_exp", "success");
				installPckUtils(dbMonth);
				response.put("region_"+key+"_month_utils", "success");
				//写入dbID
				insertDbIds(conn,key,dbDay,dbMonth);
				//更新grid表
				insertGrids(conn,key);
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
	
	private static void installPckUtils(int dbId)throws Exception{
		Connection conn = null;
		try{
			DbInfo db = DbService.getInstance()
					.getDbById(dbId);
			DbConnectConfig connConfig = DbConnectConfig.createConnectConfig(db.getConnectParam());
			conn = MultiDataSourceFactory.getInstance().getDataSource(connConfig).getConnection();
			SqlExec sqlExec = new SqlExec(conn);
			String sqlFile = "/com/navinfo/dataservice/scripts/resources/init_edit_tables.sql";
			sqlExec.executeIgnoreError(sqlFile);
			PackageExec packageExec = new PackageExec(conn);
			String pckFile = "/com/navinfo/dataservice/scripts/resources/prj_utils.pck";
			packageExec.execute(pckFile);
			String pckFile2 = "/com/navinfo/dataservice/scripts/resources/create_type_function.sql";
			packageExec.execute(pckFile2);
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
		String sql = "UPDATE GRID G SET G.REGION_ID=? WHERE TRUNC(G.GRID_ID/100) IN (SELECT P.MESH FROM CP_MESHLIST@METADB_LINK P,CP_REGION_PROVINCE T WHERE P.ADMINCODE=T.ADMINCODE AND T.REGION_ID=?)";
		new QueryRunner().update(conn, sql, regionId,regionId);
	}

	private static void insertDbIds(Connection conn,int regionId,int dayDbId,int monthDbId)throws Exception{
		String sql = "UPDATE REGION SET DAILY_DB_ID=?,MONTHLY_DB_ID=? WHERE REGION_ID=?";
		new QueryRunner().update(conn, sql,dayDbId,monthDbId,regionId);
	}
	
	public static void main(String[] args){
		Connection conn = null;
		try{
			JobScriptsInterface.initContext();
			DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
			DbInfo db = datahub.getDbById(45);
			OracleSchema schema = new OracleSchema(DbConnectConfig.createConnectConfig(db.getConnectParam()));
			conn = schema.getDriverManagerDataSource().getConnection();
//			SqlExec sqlExec = new SqlExec(conn);
			PackageExec packageExec = new PackageExec(conn);
			String sqlFile = "/com/navinfo/dataservice/scripts/resources/create_type_function.sql";
//			sqlExec.executeIgnoreError(sqlFile);
			packageExec.execute(sqlFile);
			conn.commit();
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}

}
