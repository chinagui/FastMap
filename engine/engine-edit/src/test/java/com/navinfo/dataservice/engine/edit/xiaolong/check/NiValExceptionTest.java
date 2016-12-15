package com.navinfo.dataservice.engine.edit.xiaolong.check;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.navicommons.database.Page;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class NiValExceptionTest extends InitApplication {

	@Override
	public void init() {
		initContext();
	}

	@Test
	public void testLoadByGrid() throws Exception {

		String parameter = "{\"dbId\":17,\"pageNum\":1,\"subtaskType\":4,\"pageSize\":5,\"subtaskId\":\"78\",\"grids\":[60566121]}";

		Connection conn = null;

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");
			
			int subtaskType = jsonReq.getInt("subtaskType");
			int subtaskId =0;
			if(jsonReq.containsKey("subtaskId")){
				subtaskId=jsonReq.getInt("subtaskId");
			}

			JSONArray gridJas = jsonReq.getJSONArray("grids");

			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);
			
			Set<String> grids = new HashSet<String>();
			for(Object obj:gridJas){
				grids.add(obj.toString());
			}
			if(grids.size()<1){
				ManApi manApi=(ManApi) ApplicationContextUtil.getBean("manApi");
				List<Integer> gridList = manApi.getGridIdsBySubtaskId(subtaskId);
				for(Integer obj:gridList){
					grids.add(obj.toString());
				}
			}		
			Page page = selector.list(subtaskType,grids, pageSize, pageNum);

			System.out.println(page);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Test
	public void testCheck() throws Exception
	{
		String parameter = "{\"dbId\":42,\"type\":2,\"id\":\"9aab29cf60bbbc997f12d8368b5920c2\"}";
		
		JSONObject jsonReq = JSONObject.fromObject(parameter);

		int dbId = jsonReq.getInt("dbId");

		String id = jsonReq.getString("id");

		int type = jsonReq.getInt("type");

		Connection conn = DBConnector.getInstance().getConnectionById(dbId);

		NiValExceptionOperator selector = new NiValExceptionOperator(conn);

		selector.updateCheckLogStatus(id, type);
	}

	@Test
	public void testList() throws Exception
	{
		Connection conn = null;
		try{
			Set<String> grids = new HashSet<String>();
			grids.add("60560303");

			conn = DBConnector.getInstance().getConnectionById(17);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);

			Page page = selector.list(0, grids,20,1);
			System.out.println(page.getResult());
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
}
