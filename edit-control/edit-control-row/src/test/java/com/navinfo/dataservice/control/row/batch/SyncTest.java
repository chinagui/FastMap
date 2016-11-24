package com.navinfo.dataservice.control.row.batch;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.edit.model.FmMultiSrcSync;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.row.multisrc.SyncApiImpl;

/**
 * 
 * @ClassName SyncTest
 * @author Han Shaoming
 * @date 2016年11月18日 下午5:08:53
 * @Description TODO
 */
public class SyncTest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void TestinsertFmMultiSrcSync() throws Exception{
		
		SyncApiImpl sy = new SyncApiImpl();
		String msg = sy.insertFmMultiSrcSync(3);
		System.out.println(msg);
		
	}
	
	@Test
	public void TestqueryLastSuccessSync() throws Exception{
		
		SyncApiImpl sy = new SyncApiImpl();
		FmMultiSrcSync fmMultiSrcSync = sy.queryLastSuccessSync();
		System.out.println(fmMultiSrcSync.toString());
		
	}
	
	@Test
	public void TestupdateFmMultiSrcSyncStatus() throws Exception{
		
		SyncApiImpl sy = new SyncApiImpl();
		sy.updateFmMultiSrcSyncStatus(8);
		
	}
	
	@Test
	public void TestupdateFmMultiSrcSync() throws Exception{
		
		SyncApiImpl sy = new SyncApiImpl();
		sy.updateFmMultiSrcSync(8, "路径");
		
	}
	
	@Test
	public void TestinsertMultiSrcFmSync() throws Exception{
		
		SyncApiImpl sy = new SyncApiImpl();
		String msg = sy.insertMultiSrcFmSync(2,1);
		System.out.println(msg);
		
	}
	
	@Test
	public void TestupdateMultiSrcFmSyncStatus() throws Exception{
		
		SyncApiImpl sy = new SyncApiImpl();
		sy.updateMultiSrcFmSyncStatus(5);
		
	}
	
	
	@Test
	public void TestupdateMultiSrcFmSync() throws Exception{
		
		SyncApiImpl sy = new SyncApiImpl();
		sy.updateMultiSrcFmSync(3, "路径");
		
	}
	

}
