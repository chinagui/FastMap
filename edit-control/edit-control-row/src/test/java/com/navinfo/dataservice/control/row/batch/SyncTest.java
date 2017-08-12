package com.navinfo.dataservice.control.row.batch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.edit.model.FmMultiSrcSync;
import com.navinfo.dataservice.api.edit.model.MultiSrcFmSync;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.row.multisrc.MultiSrcFmSyncService;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.control.row.charge.Fm2ChargeAdd;
import com.navinfo.dataservice.control.row.charge.Fm2ChargeInit;
import com.navinfo.dataservice.control.row.charge.RowChargeService;
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
		sy.updateFmMultiSrcSyncStatus(8,1);
		
	}
	
	@Test
	public void TestupdateFmMultiSrcSync() throws Exception{
		
		FmMultiSrcSyncApiImpl sy = new FmMultiSrcSyncApiImpl();
		sy.updateFmMultiSrcSync(8, "路径",1);
		
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
		sy.updateMultiSrcFmSyncStatus(5,5);
		
	}
	
	@Test
	public void test01() throws Exception{
		
		MultiSrcFmSync multiSrcFmSync = MultiSrcFmSyncService.getInstance().queryLastSuccessSync();
		System.out.println("状态:"+multiSrcFmSync.getSyncStatus()+"压缩包:"+multiSrcFmSync.getZipFile());
		
		
	}
	
	@Test
	public void testApplyUploadDay() throws Exception{
		String zipUrl = "http://192.168.0.40:8090/pdf/upload/fmJsonZips/20161201/20161202103647_day.zip";
		MultiSrcFmSyncService.getInstance().applyUploadDay(0, zipUrl);
	}
	
	
	@Test
	public void test02() throws Exception{
		
		MetadataApi metadataApi=(MetadataApi)ApplicationContextUtil.getBean("metadataApi");
		Map<String, Map<String, String>> scPointAdminarea = metadataApi.scPointAdminareaByAdminId();
		System.out.println(scPointAdminarea.toString());
		
	}
	
//	@Test
//	public void test03() throws Exception{
//		List<Integer> dbIds = new ArrayList<Integer>();
////		dbIds.add(13);
////		dbIds.add(330);
//		JSONObject chargePoiConvertor = RowChargeService.getInstance().chargePoiConvertor(1, "20010722150900", "20170723230000",dbIds);
//		System.out.println(chargePoiConvertor.toString());
//		
//	}
	
	@Test
	public void test04() throws Exception{
		for(int i = 0;i<10;i++){
			Fm2ChargeInit a = new Fm2ChargeInit();
			System.out.println(a);
		}
	}

}
