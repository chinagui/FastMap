package com.navinfo.dataservice.scripts;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.utils.MeshUtils;
import com.navinfo.dataservice.diff.config.DiffConfig;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: InitProjectScriptsInterface 
 * @author Xiao Xiaowen 
 * @date 2016-1-15 下午3:40:32 
 * @Description: TODO
 */
public class InitProjectScriptsInterface {
	
	public static JSONObject initProject(JSONObject request){
		JSONObject response = new JSONObject();
		Connection conn = null;
		PreparedStatement stmt = null;
		try{
			String sourceDbId = (String)request.get("sourceDbId");
			Assert.notNull(sourceDbId,"projectId不能为空");
			String projectIdStr = (String)request.get("projectId");
			Assert.notNull(projectIdStr,"projectId不能为空");
			int projectId = Integer.valueOf(projectIdStr);
			String dbName = (String)request.get("dbName");
			Assert.notNull(dbName,"dbName不能为空");
//			String gdbVersion = (String)request.get("gdbVersion");
//			Assert.notNull(gdbVersion,"gdbVersion不能为空");
			String gdbVersion = "240+";
			String projectName = (String)request.get("projectName");
			String meshes = (String)request.get("meshes");
			meshes = com.navinfo.navicommons.utils.StringUtils.removeBlankChar(meshes);
			Assert.notNull(meshes,"projectId不能为空");

			//create_db
			JSONObject createDbRequest = new JSONObject();
			createDbRequest.put("name", dbName);
			createDbRequest.put("type", "prjRoad");
			createDbRequest.put("gdbVersion", gdbVersion);
			JSONObject createDbResponse = ToolScriptsInterface.createDb(createDbRequest);
			response.put("create_db", createDbResponse);
			
			//fm_man写记录
			String prjDbId = createDbResponse.getString("dbId");
			QueryRunner runner = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
			
			
			
			//fm_man中写project记录
			String sqlPrj = "INSERT INTO PROJECT_INFO(PROJECT_ID,PROJECT_NAME,DB_ID)VALUES(?,?,?)";
			runner.update(conn, sqlPrj, projectId,projectName,Integer.valueOf(prjDbId));
			//project_mesh
			String sqlMesh = "INSERT INTO PROJECT_MESH(PROJECT_ID,MESH_ID,MESH_TYPE,SYNC_TIME)VALUES(?,?,?,SYSDATE)";
			stmt = conn.prepareStatement(sqlMesh);
			Set<String> coreMeshes = new HashSet<String>();
			CollectionUtils.addAll(coreMeshes, meshes.split(","));
			for(String mesh:coreMeshes){
				stmt.setInt(1, projectId);
				stmt.setInt(2, Integer.valueOf(mesh));
				stmt.setInt(3, 1);
				stmt.addBatch();
			}
			Set<String> extendMeshes = MeshUtils.getNeighborMeshSet(coreMeshes);
			//gei 导数据用
			String allMeshesStr = StringUtils.join(extendMeshes,",");
			extendMeshes.removeAll(coreMeshes);
			for(String mesh:extendMeshes){
				stmt.setInt(1, projectId);
				stmt.setInt(2, Integer.valueOf(mesh));
				stmt.setInt(3, 2);
				stmt.addBatch();
			}
			stmt.executeBatch();
			stmt.clearBatch();
			conn.commit();
			response.put("init_fm_man", "success");
			//export data
			JSONObject expRequest = new JSONObject();
			expRequest.put("exportMode", "copy");
			expRequest.put("feature", "gdb");
			expRequest.put("condition", "mesh");
			expRequest.put("conditionParams", allMeshesStr);
			expRequest.put("dataIntegrity", "false");
			expRequest.put("sourceDbId", sourceDbId);
			expRequest.put("targetDbId", prjDbId);
			expRequest.put("gdbVersion", gdbVersion);
			JSONObject expResponse = ToolScriptsInterface.exportData(expRequest);
			response.put("export_data", expResponse);

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
			String dir = SystemConfig.getSystemConfig().getValue("scripts.dir");
			request = ToolScriptsInterface.readJson(dir+"request"+File.separator+"init_project.json");
			response = initProject(request);
			ToolScriptsInterface.writeJson(response,dir+"response"+File.separator+"init_project.json");

			System.out.println(response);
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}

	}

}
