package com.navinfo.dataservice.control.row.batch;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;

import net.sf.json.JSONObject;

public class BatchProcessTest {
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void testGetRdName() {

		Connection conn=null;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);
			IxPoiSelector selector = new IxPoiSelector(conn);
			IxPoi poi = (IxPoi) selector.loadById(4696166, false);
			JSONObject test = new JSONObject();
			IxPoiParking parking = (IxPoiParking) poi.getParkings().get(0);
			test.put("resHigh", 0);
			parking.fillChangeFields(test);
			System.out.println(test);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	@Test
	public void stringTest() {
		String test = "京晟大酒店－会议室";
		System.out.println(ExcelReader.h2f(test));
	}
}
