package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

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
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDGSC\",\"dbId\":17,\"data\":{\"gscPoint\":{\"latitude\":40.05472,\"longitude\":116.4015},\"linkObjs\":[{\"pid\":\"309002761\",\"type\":\"RDLINK\",\"zlevel\":0},{\"pid\":\"309002761\",\"type\":\"RDLINK\",\"zlevel\":1},{\"pid\":\"206002850\",\"type\":\"RDLINK\",\"zlevel\":2}]}}";
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
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDGSC\",\"dbId\":42,\"objId\":100002946}";
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
	
	@Test //新增道路立交
	public void testAddNewRdGsc()
	{
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDGSC\",\"dbId\":42,\"data\":{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[116.46927237510681,40.018839782769234],[116.46927237510681,40.01899589603678],[116.4696156978607,40.01899589603678],[116.4696156978607,40.018839782769234],[116.46927237510681,40.018839782769234]]]},\"linkObjs\":[{\"pid\":\"100005960\",\"level_index\":0,\"type\":\"RDLINK\"},{\"pid\":\"100005958\",\"level_index\":1,\"type\":\"RDLINK\"}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test //新增铁路立交
	public void testAddNewRwGsc()
	{
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDGSC\",\"dbId\":42,\"data\":{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[116.47628769278528,40.014403757182116],[116.47628769278528,40.01445408648021],[116.47639498114584,40.01445408648021],[116.47639498114584,40.014403757182116],[116.47628769278528,40.014403757182116]]]},\"linkObjs\":[{\"pid\":\"100006640\",\"type\":\"RWLINK\",\"zlevel\":0},{\"pid\":\"100006641\",\"type\":\"RWLINK\",\"zlevel\":1}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testDelRdLink()
	{
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDLINK\",\"dbId\":42,\"objId\":     100005984}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAddRwLink()
	{
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDGSC\",\"dbId\":42,\"data\":{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[116.4772707223892,40.013802884124104],[116.4772707223892,40.01399187724597],[116.47748798131943,40.01399187724597],[116.47748798131943,40.013802884124104],[116.4772707223892,40.013802884124104]]]},\"linkObjs\":[{\"pid\":\" 100006440\",\"level_index\":0,\"type\":\"RWLINK\"},{\"pid\":\" 100006441\",\"level_index\":1,\"type\":\"RWLINK\"}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAddRdLink()
	{
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.47676110267639,40.01131305630903],[116.47781252861023,40.01052418102419]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test //测试创建rwlink和rdlink组成的立交
	public void testAddRwRdGsc()
	{
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDGSC\",\"dbId\":42,\"data\":{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[116.4768898487091,40.01065566087148],[116.4768898487091,40.011124055769244],[116.47757649421692,40.011124055769244],[116.47757649421692,40.01065566087148],[116.4768898487091,40.01065566087148]]]},\"linkObjs\":[{\"pid\":\"100005914\",\"level_index\":0,\"type\":\"RWLINK\"},{\"pid\":\"100005983\",\"level_index\":1,\"type\":\"RDLINK\"}]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void repairRwLink()
	{
		String parameter = "{\"command\":\"REPAIR\",\"dbId\":42,\"objId\":100006637,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.47341,40.02349],[116.47314548492432,40.023284762950325],[116.47275,40.02298],[116.47244,40.02274],[116.47256,40.02185],[116.47278,40.02187],[116.47295,40.02192]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RWLINK\"}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
