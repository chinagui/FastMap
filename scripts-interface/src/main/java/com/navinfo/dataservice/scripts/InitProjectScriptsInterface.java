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

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.PackageExec;
import com.navinfo.navicommons.database.sql.SqlExec;

/**
 * @ClassName: InitProjectScriptsInterface
 * @author Xiao Xiaowen
 * @date 2016-1-15 下午3:40:32
 * @Description: TODO
 */
public class InitProjectScriptsInterface {

	public static JSONObject initProject(JSONObject request) {
		JSONObject response = new JSONObject();
		Connection conn = null;
		PreparedStatement stmt = null;
		Connection tarConn = null;
		try {
			String sourceDbId = (String) request.get("sourceDbId");
			Assert.notNull(sourceDbId, "projectId不能为空");
			String projectIdStr = (String) request.get("projectId");
			Assert.notNull(projectIdStr, "projectId不能为空");
			int projectId = Integer.valueOf(projectIdStr);
			String dbName = (String) request.get("dbName");
			Assert.notNull(dbName, "dbName不能为空");
			String gdbVersion = (String) request.get("gdbVersion");
			Assert.notNull(gdbVersion, "gdbVersion不能为空");
			String projectName = (String) request.get("projectName");
			String meshes = (String) request.get("meshes");
			meshes = com.navinfo.dataservice.commons.util.StringUtils
					.removeBlankChar(meshes);
			Assert.notNull(meshes, "meshes不能为空");

			// create_db
			JSONObject createDbRequest = new JSONObject();
			createDbRequest.put("name", dbName);
			createDbRequest.put("type", "prjRoad");
			createDbRequest.put("gdbVersion", gdbVersion);
			JSONObject createDbResponse = ToolScriptsInterface
					.createDb(createDbRequest);
			response.put("create_db", createDbResponse);

			// fm_man写记录
			String prjDbId = createDbResponse.getString("dbId");
			QueryRunner runner = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource()
					.getConnection();

			// fm_man中写project记录
			String sqlPrj = "INSERT INTO PROJECT_INFO(PROJECT_ID,PROJECT_NAME,DB_ID)VALUES(?,?,?)";
			runner.update(conn, sqlPrj, projectId, projectName,
					Integer.valueOf(prjDbId));
			// project_mesh
			String sqlMesh = "INSERT INTO PROJECT_MESH(PROJECT_ID,MESH_ID,MESH_TYPE,SYNC_TIME)VALUES(?,?,?,SYSDATE)";
			stmt = conn.prepareStatement(sqlMesh);
			Set<String> coreMeshes = new HashSet<String>();
			CollectionUtils.addAll(coreMeshes, meshes.split(","));
			for (String mesh : coreMeshes) {
				stmt.setInt(1, projectId);
				stmt.setInt(2, Integer.valueOf(mesh));
				stmt.setInt(3, 1);
				stmt.addBatch();
			}

			Set<String> extendMeshes = MeshUtils.getNeighborMeshSet(coreMeshes);
			// 给导数据用
			String allMeshesStr = StringUtils.join(extendMeshes, ",");
			extendMeshes.removeAll(coreMeshes);
			for (String mesh : extendMeshes) {
				stmt.setInt(1, projectId);
				stmt.setInt(2, Integer.valueOf(mesh));
				stmt.setInt(3, 2);
				stmt.addBatch();
			}
			stmt.executeBatch();
			stmt.clearBatch();
			stmt.close();

			// project_grid
			String sqlGrid = "INSERT INTO PROJECT_GRID(PROJECT_ID,GRID_ID) VALUES(?,?)";
			stmt = conn.prepareStatement(sqlGrid);
			for (String mesh : coreMeshes) {
				for (int i = 0; i < 4; i++) {
					for (int j = 0; j < 4; j++) {
						stmt.setInt(1, projectId);
						stmt.setInt(2, Integer.valueOf(mesh + i + j));
						stmt.addBatch();
					}
				}
			}
			stmt.executeBatch();
			stmt.clearBatch();

			// mesh表
			String uSql = "UPDATE MESH SET PROJECT_ID=?,HANDLE_PROJECT_ID=? WHERE MESH_ID IN (SELECT MESH_ID FROM PROJECT_MESH WHERE PROJECT_ID=? AND MESH_TYPE=1)";
			runner.update(conn, uSql, projectId, projectId, projectId);

			// grid表
			String gSql = "UPDATE GRID A SET A.PROJECT_ID=?,A.HANDLE_PROJECT_ID=? WHERE EXISTS(SELECT NULL FROM PROJECT_GRID B WHERE A.GRID_ID=B.GRID_ID AND B.PROJECT_ID=?)";
			runner.update(conn, gSql, projectId, projectId, projectId);

			conn.commit();
			response.put("init_fm_man", "success");
			// export data
			JSONObject expRequest = new JSONObject();
			expRequest.put("exportMode", "copy");
			expRequest.put("feature", "gdb");
			expRequest.put("condition", "mesh");
			expRequest.put("conditionParams", allMeshesStr);
			expRequest.put("dataIntegrity", "false");
			expRequest.put("sourceDbId", sourceDbId);
			expRequest.put("targetDbId", prjDbId);
			expRequest.put("gdbVersion", gdbVersion);
			JSONObject expResponse = ToolScriptsInterface
					.exportData(expRequest);
			response.put("export_data", expResponse);

			// 创建索引、包等等
			OracleSchema schema = (OracleSchema) new DbManager()
					.getDbById(Integer.valueOf(prjDbId));
			tarConn = schema.getDriverManagerDataSource().getConnection();
			String sqlFile = "/com/navinfo/dataservice/scripts/resources/prj_utils.sql";
			SqlExec sqlExec = new SqlExec(tarConn);
			sqlExec.executeIgnoreError(sqlFile);
			String pckFile = "/com/navinfo/dataservice/scripts/resources/prj_utils.pck";
			PackageExec packageExec = new PackageExec(tarConn);
			packageExec.execute(pckFile);
			response.put("prj_utils_install", "success");

			response.put("msg", "执行成功");
		} catch (Exception e) {
			response.put("msg", "ERROR:" + e.getMessage());
			DbUtils.closeQuietly(stmt);
			DbUtils.rollbackAndCloseQuietly(conn);
			DbUtils.rollbackAndCloseQuietly(tarConn);
		} finally {
			DbUtils.closeQuietly(stmt);
			DbUtils.commitAndCloseQuietly(conn);
			DbUtils.commitAndCloseQuietly(tarConn);
		}
		return response;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			JSONObject request = null;
			JSONObject response = null;
			String dir = SystemConfigFactory.getSystemConfig().getValue(
					"scripts.dir");
			request = ToolScriptsInterface.readJson(dir + "request"
					+ File.separator + "init_project.json");
			response = initProject(request);
			ToolScriptsInterface.writeJson(response, dir + "response"
					+ File.separator + "init_project.json");

			System.out.println(response);
			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}

	}

}
