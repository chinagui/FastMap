package com.navinfo.dataservice.engine.edit.pid;

import org.junit.Test;

import com.navinfo.dataservice.engine.edit.service.PidService;

/** 
 * @ClassName: PidServiceTest
 * @author xiaoxiaowen4127
 * @date 2016年8月23日
 * @Description: PidServiceTest.java
 */
public class PidServiceTest {
	
	@Test
	public void applyPid_001()throws Exception{
		String tableName="XX_4_YY";
		int count=10;
		long pid = PidService.getInstance().applyPid(tableName, count);
		System.out.println("StartPid:"+pid+",Length:"+count);
	}
}
