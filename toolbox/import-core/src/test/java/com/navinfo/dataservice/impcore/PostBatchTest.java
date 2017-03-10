package com.navinfo.dataservice.impcore;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.day2mon.DeepInfoMarker;
import com.navinfo.dataservice.day2mon.PostBatch;

public class PostBatchTest {
	
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-consumer-datahub-test.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	@Test
	public void testPostBatch() throws Exception {
		Connection conn  = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(12);
			Set<String> tabNames = new HashSet<String>();
			tabNames.add("IX_POI_NAME");
			tabNames.add("IX_POI_NAME_FLAG");
			OperationResult operationResult=new OperationResult();
			BasicObj obj=ObjSelector.selectByPid(conn, "IX_POI", tabNames,false, 501000006, false);
			operationResult.putObj(obj);
			PostBatch postBatch = new PostBatch(operationResult, conn);
			postBatch.execute();
		} catch (Exception e){
			System.out.println(e.getMessage());
		} finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
