package com.navinfo.dataservice.control.row.batch;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.edit.model.FmMultiSrcSync;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.row.multisrc.MultiSrcFmSyncService;
import com.navinfo.dataservice.control.row.multisrc.FmMultiSrcSyncApiImpl;;

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
		
		FmMultiSrcSyncApiImpl sy = new FmMultiSrcSyncApiImpl();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String syncTime = sdf.format(new Date());
		String msg = sy.insertFmMultiSrcSync(3,syncTime);
		System.out.println(msg);
		
	}
	
	@Test
	public void TestqueryLastSuccessSync() throws Exception{
		
		FmMultiSrcSyncApiImpl sy = new FmMultiSrcSyncApiImpl();
		FmMultiSrcSync fmMultiSrcSync = sy.queryLastSuccessSync();
		System.out.println(fmMultiSrcSync.toString());
		
	}
	
	@Test
	public void TestupdateFmMultiSrcSyncStatus() throws Exception{
		
		FmMultiSrcSyncApiImpl sy = new FmMultiSrcSyncApiImpl();
		sy.updateFmMultiSrcSyncStatus(8);
		
	}
	
	@Test
	public void TestupdateFmMultiSrcSync() throws Exception{
		
		FmMultiSrcSyncApiImpl sy = new FmMultiSrcSyncApiImpl();
		sy.updateFmMultiSrcSync(8, "路径");
		
	}
	
	@Test
	public void TestinsertMultiSrcFmSync() throws Exception{
		
		FmMultiSrcSyncApiImpl sy = new FmMultiSrcSyncApiImpl();
		String msg = sy.insertMultiSrcFmSync(2,1,"zipFile");
		System.out.println(msg);
		
	}
	
	@Test
	public void TestupdateMultiSrcFmSyncStatus() throws Exception{
		
		FmMultiSrcSyncApiImpl sy = new FmMultiSrcSyncApiImpl();
		sy.updateMultiSrcFmSyncStatus(5);
		
	}
	
	@Test
	public void testApplyUploadDay() throws Exception{
		
		MultiSrcFmSyncService.getInstance().applyUploadDay(0, "zipUrl");
		
	}
	
	
	

}
