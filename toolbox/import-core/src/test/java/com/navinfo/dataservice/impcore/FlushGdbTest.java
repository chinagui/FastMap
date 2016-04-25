package com.navinfo.dataservice.impcore;

import org.junit.Assert;
import org.junit.Test;

/** 
* @ClassName: FlushGdbTest 
* @author Xiao Xiaowen 
* @date 2016年4月22日 上午9:24:26 
* @Description: TODO
*/
public class FlushGdbTest {

	@Test
	public void StringBuidler_01(){
		StringBuilder sb = new StringBuilder();
		sb.append("ABCD");
		sb.append("+"+"EFG");
		System.out.println(sb.toString());
		sb.delete(0, sb.length());
		sb.append("XXXX");
		System.out.println(sb.toString());
		Assert.assertTrue(true);
	}
}
