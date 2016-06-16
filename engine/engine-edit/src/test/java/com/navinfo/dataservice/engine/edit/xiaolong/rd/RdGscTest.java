package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.edit.xiaolong.InitApplication;

import net.sf.json.JSONObject;

public class RdGscTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		//调用父类初始化contex方法
		initContext();
	}
	
	@Test
	public void testCreate() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDGSC\",\"dbId\":8,\"data\":{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[116.46834969520569,40.028041639054926],[116.46834969520569,40.02857563725303],[116.46884322166441,40.02857563725303],[116.46884322166441,40.028041639054926],[116.46834969520569,40.028041639054926]]]},\"linkObjs\":[{\"pid\":\"100004083\",\"level_index\":0},{\"pid\":\"100004084\",\"level_index\":1}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDelete() {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDGSC\",\"projectId\":11,\"objId\":100002634}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSearch() throws Exception {
		String parameter = "{\"projectId\":11,\"type\":\"RDGSC\",\"pid\":100002452}";
		JSONObject jsonReq = JSONObject.fromObject(parameter);

		String objType = jsonReq.getString("type");

		int projectId = jsonReq.getInt("projectId");

		int pid = jsonReq.getInt("pid");

		SearchProcess p = new SearchProcess(
				DBConnector.getInstance().getConnectionById(projectId));
		
		Calendar.getInstance().getTimeInMillis();

		IObj obj = p.searchDataByPid(ObjType.valueOf(objType), pid);

		System.out.println(ResponseUtils.assembleRegularResult(obj.Serialize(ObjLevel.FULL)));
	}

	@Test
	public void testUpdate()
	{
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDGSC\",\"projectId\":11,\"data\":{\"processFlag\":2,\"pid\":100002767,\"objStatus\":\"UPDATE\",\"objId\":13}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
