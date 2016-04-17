package com.navinfo.dataservice.scripts;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.expcore.external.RemoveDuplicateRow;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: InitProjectScriptsInterface 
 * @author Xiao Xiaowen 
 * @date 2016-1-15 下午3:40:32 
 * @Description: TODO
 */
public class DistributeCkScriptsInterface {
	
	public static JSONObject distribute(JSONObject request){
		JSONObject response = new JSONObject();
		Connection conn = null;
		PreparedStatement stmt = null;
		try{
			String sourceDbId = (String)request.get("sourceDbId");
			Assert.notNull(sourceDbId,"projectId不能为空");
			String projectIds = (String)request.get("projectIds");
			projectIds = com.navinfo.dataservice.commons.util.StringUtils.removeBlankChar(projectIds);
			Assert.notNull(projectIds,"projectIds不能为空");
			String[] projectIdArr = projectIds.split(",");
			String gdbVersion = "240+";
			JSONObject expRequest = new JSONObject();
			expRequest.put("exportMode", "copy");
			expRequest.put("feature", "ck");
			expRequest.put("condition", "mesh");
			expRequest.put("dataIntegrity", "false");
			expRequest.put("sourceDbId", sourceDbId);
			expRequest.put("gdbVersion", gdbVersion);
			//
			QueryRunner runner = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
			String sql = "SELECT P.PROJECT_ID,P.DB_ID,M.MESH_ID FROM PROJECT_INFO P,PROJECT_MESH M WHERE P.PROJECT_ID=M.PROJECT_ID AND P.PROJECT_ID=?";
			for(String prjIdStr:projectIdArr){
				String[] strs= runner.query(conn, sql, new ResultSetHandler<String[]>() {
					public String[] handle(ResultSet rs) throws SQLException {
						String[] strs = new String[2];
						Set<String> meshSet = new HashSet<String>();
						while (rs.next()) {
							strs[0]=rs.getString("DB_ID");
							meshSet.add(rs.getString("MESH_ID"));
						}
						strs[1]=StringUtils.join(meshSet,",");
						return strs;
					}
				}, Integer.valueOf(prjIdStr));
				expRequest.put("conditionParams", strs[1]);
				expRequest.put("targetDbId", strs[0]);
				JSONObject expResponse = ToolScriptsInterface.exportData(expRequest);
				response.put("prj_"+prjIdStr, expResponse);
				//去重
				List<String> tables = new ArrayList<String>();
				tables.add("NI_VAL_EXCEPTION");
				OracleSchema targetDb = (OracleSchema)new DbManager().getDbById(Integer.valueOf(strs[0]));
				RemoveDuplicateRow.removeDup(tables, targetDb);
				response.put("removeDup_"+prjIdStr, "success");
				
			}

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
			request = ToolScriptsInterface.readJson(dir+"request"+File.separator+"distribute_ck.json");
			response = distribute(request);
			ToolScriptsInterface.writeJson(response,dir+"response"+File.separator+"distribute_ck.json");

			System.out.println(response);
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}

	}

}
