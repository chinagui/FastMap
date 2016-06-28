package com.navinfo.dataservice.engine.edit.xiaolong.poi;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.edit.search.SearchProcess;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

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

			IRow jsonObject = selector.loadById(3847439, false);

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
		String parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"IXPOI\",\"objId\":100000106,\"data\":{\"kindCode\":\"110101\",\"pid\":100000106,\"objStatus\":\"UPDATE\",\"chain\":\"2487\",\"open24h\":\"1\",\"postCode\":\"0777711\",\"poiMemo\":\"备注\",\"level\":\"B2\",\"indoor\":\"3\",\"names\":[{\"pid\":100000106,\"poiPid\":0,\"nameGroupid\":1,\"langCode\":\"CHI\",\"nameClass\":1,\"nameType\":2,\"name\":\"名称\",\"namePhonetic\":null,\"keywords\":null,\"nidbPid\":null,\"objStatus\":\"INSERT\"}],\"addresses\":[{\"pid\":100000106,\"nameGroupid\":1,\"poiPid\":0,\"langCode\":\"CHI\",\"fullname\":\"地址\",\"objStatus\":\"INSERT\"}],\"contacts\":[{\"poiPid\":0,\"contactType\":1,\"contact\":\"010-123456\",\"contactDepart\":0,\"priority\":1,\"pid\":100000106,\"objStatus\":\"INSERT\"},{\"poiPid\":0,\"contactType\":1,\"contact\":\"010-654321\",\"contactDepart\":0,\"priority\":1,\"pid\":100000106,\"objStatus\":\"INSERT\"}],\"photos\":[{\"photoId\":0,\"status\":\"\",\"memo\":0,\"pid\":100000106,\"objStatus\":\"INSERT\"},{\"photoId\":0,\"status\":\"\",\"memo\":0,\"pid\":100000106,\"objStatus\":\"INSERT\"},{\"photoId\":0,\"status\":\"\",\"memo\":0,\"pid\":100000106,\"objStatus\":\"INSERT\"},{\"photoId\":0,\"status\":\"\",\"memo\":0,\"pid\":100000106,\"objStatus\":\"INSERT\"},{\"photoId\":0,\"status\":\"\",\"memo\":0,\"pid\":100000106,\"objStatus\":\"INSERT\"},{\"photoId\":0,\"status\":\"\",\"memo\":0,\"pid\":100000106,\"objStatus\":\"INSERT\"}]}}";
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
	
	@Test
	public void testGetPoiList()
	{
		String parameter = "{\"dbId\":42,\"subtaskId\":117,\"type\":1,\"pageNum\":1,\"pageSize\":20,\"pidName\":\"\",\"pid\":0}";
		Connection conn = null;
		Connection manConn=null;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int dbId = jsonReq.getInt("dbId");
			int subtaskId=jsonReq.getInt("subtaskId");
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			manConn=DBConnector.getInstance().getManConnection();
			Subtask subtaskObj=apiService.queryBySubtaskId(subtaskId);
			String sql="SELECT E.STATUS, COUNT(1) COUNT_NUM "
					+ "  FROM POI_EDIT_STATUS E, IX_POI P"
					+ " WHERE E.ROW_ID = P.ROW_ID"
					+ "   AND SDO_RELATE(P.GEOMETRY, SDO_GEOMETRY('"+subtaskObj.getGeometry()+"', 8307), 'MASK=ANYINTERACT') ="
					+ "       'TRUE'"
					+ " GROUP BY E.STATUS";
			conn = DBConnector.getInstance().getConnectionById(dbId);
			ResultSetHandler<JSONObject> rsHandler = new ResultSetHandler<JSONObject>(){
				public JSONObject handle(ResultSet rs) throws SQLException {
					JSONObject staticsObj=new JSONObject();
					while(rs.next()){
						staticsObj.put(rs.getInt("STATUS"), rs.getInt("COUNT_NUM"));
					}
					return staticsObj;
				}	    		
	    	};		
	    	QueryRunner run = new QueryRunner();			
			System.out.println(run.query(conn, sql,rsHandler));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (manConn != null) {
				try {
					manConn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
