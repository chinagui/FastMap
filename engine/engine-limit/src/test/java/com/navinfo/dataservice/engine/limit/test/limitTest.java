package com.navinfo.dataservice.engine.limit.test;

import java.sql.Connection;
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
import com.navinfo.dataservice.engine.limit.search.SearchProcess;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class limitTest extends ClassPathXmlAppContextInit{

	protected Logger log = Logger.getLogger(this.getClass());
	
	@Before
	public void before(){
		initContext(new String[]{"dubbo-consumer-datahub-test.xml"});
	}
	

	@Test
	public void test01(){
		String parameter = "{\"type\":\"SCPLATERESINFO\",\"condition\":{\"adminArea\":\"11000\",\"infoCode\":\"\",\"startTime\":\"20170915\",\"endTime\":\"20170920\",\"complete\":\"1,2,3\",\"condition\":\"'S','D'\",\"pageSize\":20,\"pageNum\":1}}";
        Connection conn = null;

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String objType = jsonReq.getString("type");

            conn = DBConnector.getInstance().getLimitConnection();
    
            JSONObject condition = jsonReq.getJSONObject("condition");

            SearchProcess p = new SearchProcess(conn);

            List<? extends IRow> objList = p.searchLimitDataByCondition(
                    LimitObjType.valueOf(objType), condition);

            JSONArray array = new JSONArray();

            if (objList != null) {
                for (IRow obj : objList) {
                    JSONObject json = obj.Serialize(ObjLevel.FULL);
                    json.put("geoLiveType", objType);
                    array.add(json);
                }
                System.out.print(array);
            } else {
                return;
            }
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
}
