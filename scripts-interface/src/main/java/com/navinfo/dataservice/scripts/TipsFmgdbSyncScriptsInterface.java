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
public class TipsFmgdbSyncScriptsInterface {

	public static JSONObject sync(JSONObject request) {
		JSONObject response = new JSONObject();
		Connection conn = null;
		try {
			response.put("msg", "执行成功");
		} catch (Exception e) {
			response.put("msg", "ERROR:" + e.getMessage());
			DbUtils.rollbackAndCloseQuietly(conn);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
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
			String dir = SystemConfigFactory.getSystemConfig().getValue("scripts.dir");
			request = ToolScriptsInterface.readJson(dir + "request"
					+ File.separator + "tips_fmgdb_sync.json");
			response = sync(request);
			ToolScriptsInterface.writeJson(response, dir + "response"
					+ File.separator + "tips_fmgdb_sync.json.json");

			System.out.println(response);
			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}

	}

}
