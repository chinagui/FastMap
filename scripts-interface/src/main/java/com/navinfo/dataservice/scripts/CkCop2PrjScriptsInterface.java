package com.navinfo.dataservice.scripts;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.util.Assert;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.expcore.external.ExternalTool4Exporter;
import com.navinfo.dataservice.expcore.external.RemoveDuplicateRow;

/** 
 * @ClassName: InitProjectScriptsInterface 
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
			String gdbVersion = (String)request.get("gdbVersion");
			Assert.notNull(gdbVersion,"gdbVersion不能为空");
			String grids = (String)request.get("grids");
			grids = StringUtils.removeBlankChar(grids);
			Assert.notNull(grids,"grids不能为空");
			
			//先批md5值
			DbInfo sourceDb = DbService.getInstance().getDbById(Integer.valueOf(sourceDbId));
			OracleSchema sourceSchema = new OracleSchema(
					MultiDataSourceFactory.createConnectConfig(sourceDb.getConnectParam()));
			ExternalTool4Exporter.generateCkMd5(sourceSchema);
			response.put("md5", "success");
			//generate ck_result_object
			ExternalTool4Exporter.generateCkResultObject(sourceSchema);
			response.put("ck_result_object", "success");
			//generate ni_val_exception_grid
			ExternalTool4Exporter.generateCkResultGrid(sourceSchema,gdbVersion);
			response.put("ni_val_exception_grid", "success");

			DbInfo targetDb = DbService.getInstance().getDbById(Integer.valueOf(targetDbId));
			OracleSchema targetSchema = new OracleSchema(
					MultiDataSourceFactory.createConnectConfig(targetDb.getConnectParam()));
			//将在grids范围导出到目标
			ExternalTool4Exporter.selectLogGrids(sourceSchema,targetSchema,grids.split(","));
			response.put("exp", "success");
			//去重
			List<String> tables = new ArrayList<String>();
			tables.add("NI_VAL_EXCEPTION");
			RemoveDuplicateRow.removeDup(tables, targetSchema);
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
//			String dir = "F:\\Fm_Projects_Doc\\scripts\\";
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
