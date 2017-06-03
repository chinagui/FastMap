package com.navinfo.dataservice.control.dealership;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.ClassPathXmlAppContextInit;
import com.navinfo.dataservice.control.dealership.service.DataPrepareService;
import com.navinfo.dataservice.control.dealership.service.DataEditService;



public class dealtest extends ClassPathXmlAppContextInit{
	@Before
	public void before(){
		initContext(new String[]{"dubbo-app-scripts.xml","dubbo-scripts.xml"});
	}
	
	@Test
	public void testImportSourceExcel() throws Exception{
		DataPrepareService ds = DataPrepareService.getInstance();
		 ds.expTableDiff("4147");
	}
	@Test
	public void testSaveData() throws Exception{
		DataEditService ds = new DataEditService();
		
		String json = "{\"poiData\":{\"command\":\"UPDATE\",\"dbId\":13,\"type\":\"IXPOI\",\"objId\":410000122,\"data\":{\"chain\":\"\",\"contacts\":[{\"rowId\":\"3F378EC170CD44C3A48F8F56BD86126D\",\"objStatus\":\"DELETE\"},{\"rowId\":\"10D4D158DC6F4C4694C31F1173AF60F8\",\"objStatus\":\"DELETE\"}],\"restaurants\":[{\"foodType\":\"3007\",\"rowId\":\"6115657421A2472F987CDAD1624E9A21\",\"pid\":506000040,\"objStatus\":\"UPDATE\"}],\"rowId\":\"B496AD007CB54A6CA8447D51EF73EB58\",\"pid\":410000122,\"objStatus\":\"UPDATE\"},\"subtaskId\":1},dealershipInfo:{\"wkfStatus\":3,\"dbId\":399,\"resultId\":101,\"cfmMemo\":\"宝马采集\"}}";
		JSONObject parameter = JSONObject.fromObject(json);
		ds.saveDataService(parameter,2);
	}
	
}
