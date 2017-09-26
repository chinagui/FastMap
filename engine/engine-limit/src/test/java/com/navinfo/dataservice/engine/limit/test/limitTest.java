package com.navinfo.dataservice.engine.limit.test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.ClassPathXmlAppContextInit;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.operation.Transaction;
import com.navinfo.dataservice.engine.limit.search.SearchProcess;
import com.navinfo.dataservice.engine.limit.search.gdb.RdLinkSearch;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class limitTest extends ClassPathXmlAppContextInit{

	protected Logger log = Logger.getLogger(this.getClass());
	
	@Before
	public void before(){
		initContext(new String[]{"dubbo-consumer-datahub-test.xml"});
	}
	

	@Test
	public void testInfosearch(){
		String parameter = "{\"type\":\"SCPLATERESINFO\",\"condition\":{\"adminArea\":\"11000\",\"infoCode\":\"\",\"startTime\":\"20170915\",\"endTime\":\"20170920\",\"complete\":\"1,2,3\",\"condition\":\"'S','D'\",\"pageSize\":20,\"pageNum\":1}}";
        Connection conn = null;

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String objType = jsonReq.getString("type");

            conn = DBConnector.getInstance().getLimitConnection();
    
            JSONObject condition = jsonReq.getJSONObject("condition");

            SearchProcess p = new SearchProcess(conn);

            List<IRow> objList = new ArrayList<>();
            
            int total = p.searchLimitDataByCondition(
                    LimitObjType.valueOf(objType), condition,objList);

            JSONArray array = new JSONArray();

            for (IRow obj : objList) {
			    JSONObject json = obj.Serialize(ObjLevel.FULL);
			    json.put("geoLiveType", objType);
			    array.add(json);
			}
			System.out.print(array);
        } catch (Exception e) {

            log.error(e.getMessage(), e);           
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
	public void testRdName() throws Exception{
		String parameter = "{\"dbId\":13,\"type\":3,\"condition\":{\"linkPid\":573595}}";
		
		JSONObject jsonReq = JSONObject.fromObject(parameter);

        if (jsonReq == null || !jsonReq.containsKey("dbId") || !jsonReq.containsKey("type") || !jsonReq.containsKey("condition")) {
            throw new Exception("输入信息不完善，无法查询道路link！");
        }

        int dbId = jsonReq.getInt("dbId");
        
        int type = jsonReq.getInt("type");

        JSONObject condition = jsonReq.getJSONObject("condition");

        Connection conn = DBConnector.getInstance().getConnectionById(dbId);

        SearchProcess p = new SearchProcess(conn);
        
        JSONObject result = p.searchRdLinkDataByCondition(type, condition);
        
        System.out.println(result);
	}
	
	@Test
	public void testMetaSearch() throws Exception {
		String parameter = "{\"type\":\"SCPLATERESGROUP\",\"condition\":{\"adminArea\":110000,\"groupType\":\"1,2\",\"pageSize\":20,\"pageNum\":1}}";

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			conn = DBConnector.getInstance().getLimitConnection();

			JSONObject condition = jsonReq.getJSONObject("condition");

			SearchProcess p = new SearchProcess(conn);

			List<IRow> objList = new ArrayList<>();
			int count = p.searchMetaDataByCondition(LimitObjType.valueOf(objType), condition, objList);

			JSONObject result = new JSONObject();
			JSONArray array = new JSONArray();

			for (IRow obj : objList) {
				JSONObject json = obj.Serialize(ObjLevel.FULL);
				json.put("geoLiveType", objType);
				array.add(json);
			}

			result.put("total", count);
			result.put("data", array);

			System.out.println(result);
		} catch (Exception e) {
			log.error(e.getMessage(), e);

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
	public void testRun() throws Exception{
		String parameter = "{\"type\":\"SCPLATERESMANOEUVRE\",\"command\":\"CREATE\",\"data\":{\"groupId\":\"S1100000002\",\"vehicle\":\"1|2|3\",\"attribution\":\"1|2\",\"restrict\":\"京G\",\"tempPlate\":2,\"tempPlateNum\":0,\"charSwitch\":2,\"charToNum\":1,\"tailNumber\":\"1|2|3\",\"plateColor\":\"1|2\",\"energyType\":\"1|2|3\",\"gasEmisstand\":\"1\",\"seatNum\":10,\"vehicleLength\":5.5,\"resWeigh\":15.0,\"resAxleLoad\":15.0,\"resAxleCount\":3,\"startDate\":\"20170915\",\"endDate\":\"20170922\",\"resDatetype\":\"1|2\",\"time\":\"20170922\",\"specFlag\":\"1|2\"}}";
		
		Transaction t = new Transaction(parameter);
		
		t.run();
	}
}
