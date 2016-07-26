package com.navinfo.dataservice.engine.edit.luyao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.selector.rd.warninginfo.RdWarninginfoSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

public class RdWarninginfoTest  extends InitApplication{

	@Override
	@Before
	public void init() {
		initContext();
	}
	
	
	@Test
	public void createTest() throws Exception {
		String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"linkPid\":733938,\"nodePid\":470009},\"type\":\"RDWARNINGINFO\"}";
		
		Transaction t = new Transaction(parameter);
		
		String msg = t.run();
	}
	
	@Test
	public void updateTest() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDWARNINGINFO\",\"dbId\":42,\"data\":{\"validDis\":1,\"warnDis\":1,\"pid\":100000020,\"objStatus\":\"UPDATE\"}}";
		
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void deleteTest() throws Exception {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RDWARNINGINFO\",\"dbId\":42,\"objId\":100000020}";
		
		Transaction t = new Transaction(parameter);
		
		String msg = t.run();
	}
	
	
	@Test
	public void testGetByPid() {
		
		try {
			Connection conn = DBConnector.getInstance().getConnectionById(42);

			RdWarninginfoSelector selector = new RdWarninginfoSelector(conn);

			IRow jsonObject = selector.loadById(100000021, false);

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
			conn = DBConnector.getInstance().getConnectionById(42);
			
			SearchProcess p = new SearchProcess(conn);
			
			List<ObjType> objType = new ArrayList<>();
			
			objType.add(ObjType.RDWARNINGINFO);

			System.out.println(p.searchDataByTileWithGap(objType, 107937, 49616, 17, 80));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
