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
import com.navinfo.navicommons.database.QueryRunner;

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
		
		String sql="insert into subtask_quality(quality_id,subtask_id,geometry)"
		+ "select subtask_quality_seq.nextval,142,sdo_geometry('POLYGON ((114.56434 39.36486，114.56434 40.21208，116.79367 40.21208，116.79367 39.36486，114.56434 39.36486))',"
		+ "8307) from dual";
		//String filePathString="D:/temp/block_tab2json/bj.TAB";
		//String filePathString = String.valueOf(args[0]);
		Connection conn=DBConnector.getInstance().getManConnection();
		QueryRunner runner=new QueryRunner();
		runner.update(conn, sql);
		//List<Map<String, Object>> dataList = LoadTab.readTabReturnAllData(filePathString);
		//ImportOracle.writeOracle(conn, "test1125", dataList);
		DbUtils.commitAndCloseQuietly(conn);
		System.out.println("end"); 
	}

}
