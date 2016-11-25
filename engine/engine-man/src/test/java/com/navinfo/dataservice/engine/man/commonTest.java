package com.navinfo.dataservice.engine.man;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.fileConvert.ImportOracle;
import com.navinfo.dataservice.commons.fileConvert.LoadTab;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

public class commonTest {
	
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-consumer.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println("start"); 
		commonTest test=new commonTest();
		test.before();
		String filePathString="D:/temp/block_tab2json/bj.TAB";
		Connection conn=DBConnector.getInstance().getManConnection();
		List<Map<String, Object>> dataList = LoadTab.readTabReturnAllData(filePathString);
		ImportOracle.writeOracle(conn, "test1125", dataList, null, null);
		DbUtils.commitAndCloseQuietly(conn);
		System.out.println("end"); 
	}

}
