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
	
}
