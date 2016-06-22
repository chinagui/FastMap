package com.navinfo.dataservice.scripts;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.datarow.PhysicalDeleteRow;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.navicommons.geo.computation.MeshUtils;

/** 
 * @ClassName: InitProjectScriptsInterface 
 * @author Xiao Xiaowen 
 * @date 2016-1-15 下午3:40:32 
 * @Description: TODO
 */
public class Exp2CopVersionScriptsInterface {
	
	public static JSONObject distribute(JSONObject request){
		JSONObject response = new JSONObject();
		Connection conn = null;
		PreparedStatement stmt = null;
		try{
			String sourceDbId = (String)request.get("sourceDbId");
			Assert.notNull(sourceDbId,"sourceDbId不能为空");
			String targetDbId = (String)request.get("targetDbId");
			Assert.notNull(targetDbId,"targetDbId不能为空");
			String meshes = (String)request.get("meshes");
			String gdbVersion = (String)request.get("gdbVersion");
			Assert.notNull(gdbVersion,"gdbVersion不能为空");
			meshes = com.navinfo.dataservice.commons.util.StringUtils.removeBlankChar(meshes);
			Assert.notNull(meshes,"meshes不能为空");
			String extendCountStr = (String)request.get("extendCount");
			int extendCount = StringUtils.isEmpty(extendCountStr)?0:Integer.valueOf(extendCountStr);

			String allMeshesStr = null;
			DbInfo db = DbService.getInstance().getDbById(Integer.valueOf(targetDbId));
			OracleSchema schema = new OracleSchema(
					DbConnectConfig.createConnectConfig(db.getConnectParam()));
			DbConnectConfig connConfig = DbConnectConfig.createConnectConfig(db.getConnectParam()); 
			
			conn = schema.getDriverManagerDataSource().getConnection();
			//计算扩圈，写m_mesh_type
			String sqlMesh = "INSERT INTO M_MESH_TYPE(MESH_ID,\"TYPE\")VALUES(?,?)";
			stmt = conn.prepareStatement(sqlMesh);
			Set<String> coreMeshes = new HashSet<String>();
			CollectionUtils.addAll(coreMeshes, meshes.split(","));
			for(String mesh:coreMeshes){
				stmt.setInt(1, Integer.valueOf(mesh));
				stmt.setInt(2, 1);
				stmt.addBatch();
			}
			if(extendCount>0){
				Set<String> extendMeshes = MeshUtils.getNeighborMeshSet(coreMeshes,extendCount);
				//gei 导数据用
				allMeshesStr = StringUtils.join(extendMeshes,",");
				extendMeshes.removeAll(coreMeshes);
				for(String mesh:extendMeshes){
					stmt.setInt(1, Integer.valueOf(mesh));
					stmt.setInt(2, 2);
					stmt.addBatch();
				}
			}else{
				allMeshesStr = StringUtils.join(coreMeshes,",");
			}
			stmt.executeBatch();
			stmt.clearBatch();
			conn.commit();
			response.put("m_mesh_type", "success");
			//export data
			JSONObject expRequest = new JSONObject();
			expRequest.put("exportMode", "copy");
			expRequest.put("feature", "gdb");
			expRequest.put("condition", "mesh");
			expRequest.put("conditionParams", allMeshesStr);
			expRequest.put("dataIntegrity", "false");
			expRequest.put("sourceDbId", sourceDbId);
			expRequest.put("targetDbId", targetDbId);
			expRequest.put("gdbVersion", gdbVersion);
			JSONObject expResponse = ToolScriptsInterface.exportData(expRequest);
			response.put("export_data", expResponse);
			//逻辑删除数据
			PhysicalDeleteRow.doDelete(schema);
			response.put("physical_delete", "success");
			response.put("msg", "执行成功");
		}catch(Exception e){
			response.put("msg", "ERROR:"+e.getMessage());
			DbUtils.closeQuietly(stmt);
			DbUtils.rollbackAndCloseQuietly(conn);
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
		return response;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			JSONObject request=null;
			JSONObject response = null;
			String dir = SystemConfigFactory.getSystemConfig().getValue("scripts.dir");
			request = ToolScriptsInterface.readJson(dir+"request"+File.separator+"fmgdb_to_cop.json");
			response = distribute(request);
			ToolScriptsInterface.writeJson(response,dir+"response"+File.separator+"fmgdb_to_cop.json");

			System.out.println(response);
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}

	}

}
