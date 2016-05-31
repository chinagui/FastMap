package com.navinfo.dataservice.scripts;

import java.io.File;
import java.sql.Connection;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;

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
