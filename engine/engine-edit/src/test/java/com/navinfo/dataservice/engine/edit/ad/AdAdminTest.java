package com.navinfo.dataservice.engine.edit.ad;

import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;

import net.sf.json.JSONObject;

public class AdAdminTest {

	private static final String configPath = "D:/ws_new/DataService/web/edit-web/src/main/resources/config.properties";

	static {
		ConfigLoader.initDBConn(configPath);
	}

	public AdAdminTest() {
	}

	public void testAdd() {
		String parameter = "{\"command\":\"CREATE\",\"type\":\"ADADMIN\",\"projectId\":11,\"data\":{\"longitude\":116.39552235603331,\"latitude\":39.90676527744907,\"linkPid\":625962}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testDelete()
	{
		String parameter = "{\"command\":\"DELETE\",\"type\":\"ADADMIN\",\"projectId\":11,\"objId\":100000138}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testMove()
	{
		String parameter = "{\"command\":\"MOVE\",\"type\":\"ADADMIN\",\"projectId\":11,\"objId\":100000136,\"data\":{\"longitude\":116.39932036399843,\"latitude\":39.9071109355894,\"linkPid\":19609778}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testUpdateAttr()
	{
		String paratmeter = "{\"command\":\"UPDATE\",\"type\":\"ADADMIN\",\"projectId\":11,\"data\":{\"names\":[{\"regionId\":100000136,\"nameGroupId\":1,\"langCode\":\"CHI\",\"nameClass\":1,\"name\":\"测试\",\"phonetic\":\"Ce Shi\",\"srcFlag\":0,\"pid\":100000136,\"objStatus\":\"INSERT\"}],\"pid\":100000136}}";
		Transaction t = new Transaction(paratmeter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testSearch()
	{
		String paratmeter = "{\"projectId\":11,\"type\":\"ADADMIN\",\"pid\":100000137}";
		try {
			
			JSONObject jsonReq = JSONObject.fromObject(paratmeter);

			String objType = jsonReq.getString("type");

			int projectId = jsonReq.getInt("projectId");
			
			int pid = jsonReq.getInt("pid");
			
			SearchProcess p = new SearchProcess(GlmDbPoolManager.getInstance().getConnection(projectId));
			
			IObj obj = p.searchDataByPid(ObjType.valueOf(objType), pid);
			
			System.out.println(ResponseUtils.assembleRegularResult(obj
									.Serialize(ObjLevel.FULL)));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testUpdatTree()
	{
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"ADADMINGROUP\",\"projectId\":11,\"data\":{\"groupTree\":{\"regionId\":1273,\"name\":\"中国大陆\",\"group\":{\"groupId\":248,\"regionIdUp\":1273,\"rowId\":\"2D71EFCB1966DCE7E050A8C083040693\"},\"children\":[{\"regionId\":163,\"name\":\"北京市\",\"group\":{\"groupId\":40,\"regionIdUp\":163,\"rowId\":\"2D71EFCB16D7DCE7E050A8C083040693\"},\"part\":{\"groupId\":248,\"regionIdDown\":163,\"rowId\":\"2D71EFCB56BEDCE7E050A8C083040693\"},\"children\":[{\"regionId\":580,\"name\":\"北京市\",\"group\":{\"groupId\":114,\"regionIdUp\":580,\"rowId\":\"2D71EFCB1711DCE7E050A8C083040693\"},\"part\":{\"groupId\":40,\"regionIdDown\":580,\"rowId\":\"2D71EFCB642CDCE7E050A8C083040693\"},\"children\":[{\"regionId\":387274,\"name\":\"东直门\",\"group\":null,\"part\":{\"groupId\":114,\"rowId\":null,\"objType\":\"insert\"},\"children\":[]}]}]}]}}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//new AdAdminTest().testAdd();
		testUpdatTree();
	}
}
