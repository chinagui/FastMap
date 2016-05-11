package com.navinfo.dataservice.scripts;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.util.Assert;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.expcore.external.ExternalTool4Exporter;
import com.navinfo.dataservice.expcore.external.RemoveDuplicateRow;

/** 
 * @ClassName: CkCop2PrjScriptsInterface 
 * @author Xiao Xiaowen 
 * @date 2016-1-15 下午3:40:32 
 * @Description: TODO
 */
public class CkCop2PrjScriptsInterface {
	
	public static JSONObject distribute(JSONObject request){
		JSONObject response = new JSONObject();
		Connection conn = null;
		PreparedStatement stmt = null;
		try{
			String sourceDbId = (String)request.get("sourceDbId");
			Assert.notNull(sourceDbId,"sourceDbId不能为空");
			String targetDbId = (String)request.get("targetDbId");
			Assert.notNull(targetDbId,"targetDbId不能为空");
			
			//先批md5值
			OracleSchema sourceDb = (OracleSchema)new DbManager().getDbById(Integer.valueOf(sourceDbId));
			ExternalTool4Exporter.generateCkMd5(sourceDb);
			response.put("md5", "success");
			//generate ck_result_object
			ExternalTool4Exporter.generateCkResultObject(sourceDb);
			response.put("ck_result_object", "success");
			
			
			String gdbVersion = "240+";
			JSONObject expRequest = new JSONObject();
			expRequest.put("exportMode", "full_copy");
			expRequest.put("specificTables", "NI_VAL_EXCEPTION,CK_RESULT_OBJECT");
			expRequest.put("dataIntegrity", "false");
			expRequest.put("sourceDbId", sourceDbId);
			expRequest.put("gdbVersion", gdbVersion);
			expRequest.put("targetDbId", targetDbId);
			//
			JSONObject expResponse = ToolScriptsInterface.exportData(expRequest);
			response.put("exp", expResponse);
			//去重
			List<String> tables = new ArrayList<String>();
			tables.add("NI_VAL_EXCEPTION");
			OracleSchema targetDb = (OracleSchema)new DbManager().getDbById(Integer.valueOf(targetDbId));
			RemoveDuplicateRow.removeDup(tables, targetDb);
			response.put("removeDup", "success");

			response.put("msg", "success");
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
			request = ToolScriptsInterface.readJson(dir+"request"+File.separator+"cop_xcopy_exception.json");
			response = distribute(request);
			ToolScriptsInterface.writeJson(response,dir+"response"+File.separator+"cop_xcopy_exception.json");

			System.out.println(response);
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}

	}

}
