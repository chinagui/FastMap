package com.navinfo.dataservice.control.row.batch;

import org.junit.Test;

import com.navinfo.dataservice.control.row.multisrc.SyncApiImpl;

/**
 * 
 * @ClassName SyncTest
 * @author Han Shaoming
 * @date 2016年11月18日 下午5:08:53
 * @Description TODO
 */
public class SyncTest {
	
	@Test
	public void TestInsertFmMultiSrcSync() throws Exception{
		SyncApiImpl sy = new SyncApiImpl();
		String msg = sy.insertFmMultiSrcSync(1);
		System.out.println(msg);
	}
	

}
