package com.navinfo.dataservice.impcore;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.day2mon.DeepInfoMarker;

public class deepInfoMarkerTest {
	
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-consumer-datahub-test.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	@Test
	public void testDeepInfoMarker() throws Exception {
		Connection conn  = null;
		try {
			OperationResult opResult = null;
			conn = DBConnector.getInstance().getConnectionById(19);
			OperationResult operationResult=new OperationResult();
			BasicObj obj=ObjSelector.selectByPid(conn, "IX_POI", null, 2179861, false);
			operationResult.putObj(obj);
			DeepInfoMarker deepInfo = new DeepInfoMarker(opResult, conn);
			deepInfo.execute();
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
	}

}
