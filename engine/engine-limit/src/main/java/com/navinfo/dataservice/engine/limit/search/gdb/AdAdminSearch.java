package com.navinfo.dataservice.engine.limit.search.gdb;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

public class AdAdminSearch {
	
	private Connection conn;
	
	public AdAdminSearch(Connection conn){
		this.conn = conn;
	}
	
	public int searchDbId(String adminCode) throws Exception {

		QueryRunner run = new QueryRunner();
		
		if(adminCode.length()<2){
			throw new Exception("输入行政区划编码有误，请重新输入！");
		}

		String newAdmincode = adminCode.substring(0, 2) + "0000";
		
		String sql = "SELECT A.DAILY_DB_ID FROM REGION A,CP_REGION_PROVINCE B WHERE A.REGION_ID = B.REGION_ID AND B.ADMINCODE = '"
				+ newAdmincode + "'";

		int dbId = run.queryForInt(conn, sql);

		return dbId;
	}
	
	public JSONObject searchAdAdminPosition(String adminCode) throws Exception{
		AdAdminSelector selector = new AdAdminSelector(this.conn);
		
		AdAdmin admin = selector.loadByAdminId(Integer.valueOf(adminCode), true);
		
		JSONObject result = admin.Serialize(ObjLevel.FULL);
		
		return result;
	}
}
