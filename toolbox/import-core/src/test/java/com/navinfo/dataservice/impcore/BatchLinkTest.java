package com.navinfo.dataservice.impcore;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.day2mon.PoiGuideLinkBatch;

public class BatchLinkTest {
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-consumer-datahub-test.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void testBatchLink() throws Exception {
		DatahubApi datahubApi = (DatahubApi)ApplicationContextUtil
				.getBean("datahubApi");
		DbInfo monthDbInfo = datahubApi.getDbById(12);
		OracleSchema monthDbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(monthDbInfo.getConnectParam()));
		PoiGuideLinkBatch batch = new PoiGuideLinkBatch("temp_poi_glink_1",monthDbSchema);
		batch.execute();

	}
	
}
