package com.navinfo.dataservice.engine.edit.luyao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
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
	public void deleteTest() throws Exception {
		String parameter = "{\"command\":\"DELETE\",\"type\":\"RWNODE\",\"dbId\":42,\"objId\":5419}";
		
		Transaction t = new Transaction(parameter);
		
		String msg = t.run();
	}

	@Test
	public void updateTest() throws Exception {
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"ADNODE\",\"projectId\":11,\"data\":{\"inLinkPid\":100005725,\"nodePid\":469291,\"outLinkPid\":719802,\"pid\":100000670,\"realimages\":[{\"objStatus\":\"UPDATE\",\"arrowCode\":\"\",\"branchPid\":100000670,\"imageType\":1,\"realCode\":\"123\",\"rowId\":\"C95ED1D783924C2B8F51FE6914A50C68\"}]}}";
		
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	
	
	@Test
	public void testGetByPid() {
		Connection conn;
		try {

			String parameter ="{\"dbId\":42,\"type\":\"RDBRANCH\",\"detailId\":0,\"rowId\":\"41937B0F3A6842929633FA164D077DDC\",\"branchType\":5}";			
			
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			if (jsonReq.containsKey("detailId")) {
				int detailId = jsonReq.getInt("detailId");
				int branchType = jsonReq.getInt("branchType");
				String rowId = jsonReq.getString("rowId");
				RdBranchSelector selector = new RdBranchSelector(conn);
				IRow row = selector.loadByDetailId(detailId, branchType, rowId, false);

				if (row != null) {

					System.out.println(row.Serialize(ObjLevel.FULL));
				}
			}

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

}
