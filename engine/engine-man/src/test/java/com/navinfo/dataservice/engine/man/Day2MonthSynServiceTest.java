package com.navinfo.dataservice.engine.man;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.man.model.FmDay2MonSync;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.day2Month.Day2MonthSyncService;

public class Day2MonthSynServiceTest {
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
				new String[] { "dubbo-consumer.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	@Test
	public void testInsertSyncInfo() throws Exception {
		Day2MonthSyncService svr = Day2MonthSyncService.getInstance();
		FmDay2MonSync info = new FmDay2MonSync();
		info.setCityId(7);
		info.setSyncStatus(FmDay2MonSync.SyncStatusEnum.CREATE.getValue());
		info.setSyncTime(new Date());
		svr.insertSyncInfo(info );
	}

	@Test
	public void testUpdateSyncInfo() throws Exception {
		Day2MonthSyncService svr = Day2MonthSyncService.getInstance();
		FmDay2MonSync info = new FmDay2MonSync();
		info.setSid(3);
		info.setCityId(7);
		info.setSyncStatus(FmDay2MonSync.SyncStatusEnum.SUCCESS.getValue());
		info.setSyncTime(new Date());
		svr.updateSyncInfo(info );
	}

	@Test
	public void testQueryLastedSyncInfo() throws Exception {
		Day2MonthSyncService svr = Day2MonthSyncService.getInstance();
		FmDay2MonSync info = svr.queryLastedSyncInfo(7);
		System.out.println(info);
	}

}
