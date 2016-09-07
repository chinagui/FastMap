package com.navinfo.dataservice.bizcommons;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.springmvc.ClassPathXmlAppContextInit;

/** 
 * @ClassName: PidUtilTest
 * @author xiaoxiaowen4127
 * @date 2016年9月6日
 * @Description: PidUtilTest.java
 */
public class PidUtilTest extends ClassPathXmlAppContextInit{
	@Before
	public void before(){
		initContext(new String[]{"dubbo-app-scripts.xml","dubbo-scripts.xml"});
	}
	
	@Test
	public void apply_001(){
		try{
			int pid = PidUtil.getInstance().applyAdAdminNamePid();
			System.out.println(pid);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
