package com.navinfo.dataservice.engine.fcc;

import org.junit.Test;

import com.navinfo.dataservice.engine.fcc.tips.TipsOperator;

/** 
 * @ClassName: TipsOperateTest.java
 * @author y
 * @date 2016-7-2下午1:56:04
 * @Description: TODO
 *  
 */
public class TipsOperateTest {
	
	@Test
	public void testEdit(){
		
		TipsOperator operate=new TipsOperator();
		
		try {
			operate.update("021806d2379145037f471ebda56b88a659999", 123, 0, "m");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
