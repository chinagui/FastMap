package com.navinfo.dataservice.control.row.batch;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

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
		String param = "{\"command\":\"UPDATE\",\"dbId\":46,\"type\":\"IXPOI\",\"objId\":8509,\"data\":{\"names\":[{\"name\":\"大青鱼垂钓园名称批处理\",\"rowId\":\"3E4477FEB8B67097E050A8C083045F3F\",\"pid\":125593674,\"objStatus\":\"UPDATE\"}],\"rowId\":\"3E447527DADC7097E050A8C083041F3F\",\"pid\":8509}}";
		JSONObject json = JSONObject.fromObject(param);
		json.put("objId", 8509);
		Connection conn=null;
		try {
			conn = DBConnector.getInstance().getConnectionById(46);
			EditApiImpl editApiImpl = new EditApiImpl(conn);
			BatchProcess batchProcess = new BatchProcess();
			batchProcess.execute(json,conn,editApiImpl);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				DbUtils.commitAndClose(conn);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
