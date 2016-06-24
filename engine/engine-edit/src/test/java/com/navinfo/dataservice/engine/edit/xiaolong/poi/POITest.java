package com.navinfo.dataservice.engine.edit.xiaolong.poi;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;

public class POITest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testGetByPid() {
		try {
			Connection conn = DBConnector.getInstance().getConnectionById(42);

			IxPoiSelector selector = new IxPoiSelector(conn);

			IRow jsonObject = selector.loadById(88553093, false);

			System.out.println(jsonObject.Serialize(ObjLevel.FULL));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void getTitleWithGap()
	{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getConnectionById(8);
			
			SearchProcess p = new SearchProcess(conn);
			
			List<ObjType> objType = new ArrayList<>();
			
			objType.add(ObjType.IXPOI);

			System.out.println(p.searchDataByTileWithGap(objType, 107937, 49616, 17, 80));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDeletePoi()
	{
		String parameter = "{\"command\":\"DELETE\",\"type\":\"IXPOI\",\"dbId\":42,\"objId\":642692}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void addPoi(){
		String parameter = "{\"command\":\"CREATE\",\"type\":\"IXPOI\",\"dbId\":42,\"data\":{\"longitude\":116.39552235603331,\"latitude\":39.90676527744907,\"linkPid\":625962}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdatePoi()
	{
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"IXPOI\",\"objId\":88581671,\"data\":{\"names\":[{\"name\":\"充电桩CHENX\",\"rowId\":\"3524EA0D21BD6E1AE050A8C08304BA17\",\"objStatus\":\"UPDATE\"}],\"pid\":88581671}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testDeleteParent()
	{
		String parameter = "{\"command\":\"DELETE\",\"type\":\"IXPOIPARENT\",\"dbId\":42,\"objId\":73341675}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
