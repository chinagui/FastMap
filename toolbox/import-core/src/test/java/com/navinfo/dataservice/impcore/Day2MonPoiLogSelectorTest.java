package com.navinfo.dataservice.impcore;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.FmDay2MonSync;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.day2mon.Day2MonPoiLogSelector;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flusher.Day2MonLogFlusher;
import com.navinfo.dataservice.day2mon.Day2MonPoiLogByFilterGridsSelector;

public class Day2MonPoiLogSelectorTest {
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-consumer-datahub-test.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	@Test
	public void testSelect() throws Exception {
		DatahubApi datahubApi = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
		int cityId = 17;
		DbInfo dailyDbInfo = datahubApi.getDbById(cityId);
		OracleSchema schema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dailyDbInfo .getConnectParam()));
		Day2MonPoiLogSelector selector = new Day2MonPoiLogSelector(schema );
		ManApi manApi = (ManApi)ApplicationContextUtil
				.getBean("manApi");
		selector.setGrids(manApi.queryGridOfCity(cityId));
		selector.setStopTime(new Date());
		String tempTable = selector.select();
		System.out.println(tempTable);
	}
	@Test
	public void testSelectLog() throws Exception {
		DatahubApi datahubApi = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
		int cityId = 17;
		DbInfo dailyDbInfo = datahubApi.getDbById(cityId);
		DbInfo monthDbInfo = datahubApi.getDbById(19);
		OracleSchema dailyDbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dailyDbInfo.getConnectParam()));
		Connection dailyConn = dailyDbSchema.getPoolDataSource().getConnection();
		OracleSchema monthDbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(monthDbInfo.getConnectParam()));
		Connection monthConn = monthDbSchema.getPoolDataSource().getConnection();
		Day2MonPoiLogByFilterGridsSelector selector = new Day2MonPoiLogByFilterGridsSelector(dailyDbSchema);
		List<Integer> filterGrids= new ArrayList<Integer>();
		filterGrids.add(60563511);
		selector.setFilterGrids(filterGrids);
		selector.setStopTime(new Date());
		try{
			String tempTable = selector.select();
			System.out.println(tempTable);
			//测试刷履历
			FlushResult flushResult= new Day2MonLogFlusher(dailyDbSchema,dailyConn,monthConn,true,tempTable).flush();
			if(0==flushResult.getTotal()){
				return;
			}
		}catch(Exception e){
			if(monthConn!=null)monthConn.rollback();
			if(dailyConn!=null)dailyConn.rollback();
			throw e;
			
		}finally{
			DbUtils.commitAndCloseQuietly(monthConn);
			DbUtils.commitAndCloseQuietly(dailyConn);	
		}

	}

}
