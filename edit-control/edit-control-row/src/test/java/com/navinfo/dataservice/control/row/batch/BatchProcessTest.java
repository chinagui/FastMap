package com.navinfo.dataservice.control.row.batch;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

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
		JSONObject json = new JSONObject();
		json.put("objId", 4788600);
		Connection conn=null;
		try {
			conn = DBConnector.getInstance().getConnectionById(42);
			BatchProcess batchProcess = new BatchProcess();
			batchProcess.execute("PoiBatchProcessorFM_BAT_20_104",json,conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
}
