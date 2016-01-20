package com.navinfo.dataservice.scripts;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.utils.MeshUtils;

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
			Assert.notNull(sourceDbId,"projectId不能为空");
			String targetDbId = (String)request.get("targetDbId");
			Assert.notNull(targetDbId,"targetDbId不能为空");
			String meshes = (String)request.get("meshes");
			meshes = com.navinfo.navicommons.utils.StringUtils.removeBlankChar(meshes);
			Assert.notNull(meshes,"meshes不能为空");
			String extendCountStr = (String)request.get("extendCount");
			int extendCount = StringUtils.isEmpty(extendCountStr)?0:Integer.valueOf(extendCountStr);

			String allMeshesStr = null;
			conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
			//计算扩圈，写m_mesh_type
			String sqlMesh = "INSERT INTO M_MESH_TYPE(MESH_ID,MESH_TYPE)VALUES(?,?)";
			stmt = conn.prepareStatement(sqlMesh);
			Set<String> coreMeshes = new HashSet<String>();
			CollectionUtils.addAll(coreMeshes, meshes.split(","));
			for(String mesh:coreMeshes){
				stmt.setInt(1, Integer.valueOf(mesh));
				stmt.setInt(2, 0);
				stmt.addBatch();
			}
			if(extendCount>0){
				Set<String> extendMeshes = MeshUtils.getNeighborMeshSet(coreMeshes,extendCount);
				//gei 导数据用
				allMeshesStr = StringUtils.join(extendMeshes,",");
				extendMeshes.removeAll(coreMeshes);
				for(String mesh:extendMeshes){
					stmt.setInt(1, Integer.valueOf(mesh));
					stmt.setInt(1, 1);
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
			String gdbVersion = "240+";
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
