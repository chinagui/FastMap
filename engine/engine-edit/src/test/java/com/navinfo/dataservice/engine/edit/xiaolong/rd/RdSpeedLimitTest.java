package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;

import net.sf.json.JSONObject;

public class RdSpeedLimitTest {
	
	public static void testSearch()
	{
		String parameter = "{\"projectId\":11,\"type\":\"RDSPEEDLIMIT\",\"pid\":20177}";
		JSONObject jsonReq = JSONObject.fromObject(parameter);

		String objType = jsonReq.getString("type");

		int projectId = jsonReq.getInt("projectId");

		int pid = jsonReq.getInt("pid");

		SearchProcess p;
		try {
			p = new SearchProcess(
					DBConnector.getInstance().getConnectionById(11));
			IObj obj = p.searchDataByPid(ObjType.valueOf(objType), pid);

			System.out.println(ResponseUtils.assembleRegularResult(obj.Serialize(ObjLevel.FULL)));
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
	
	public static void testUpdate()
	{
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDSPEEDLIMIT\",\"projectId\":11,\"data\":{\"speedValue\":\"62\",\"pid\":20178,\"objStatus\":\"UPDATE\"}}";
	}
	
	public static void main(String[] args) {
		try {
			testSearch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
