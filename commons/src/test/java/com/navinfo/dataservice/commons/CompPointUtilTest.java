package com.navinfo.dataservice.commons;

import org.junit.Test;

import com.navinfo.navicommons.geo.computation.CompPointUtil;
import com.navinfo.navicommons.geo.computation.DoublePoint;
/** 
* @ClassName: CompPointUtilTest 
* @author Xiao Xiaowen 
* @date 2016年5月10日 下午9:35:20 
* @Description: TODO
*/
public class CompPointUtilTest {
	@Test
	public void norm_001(){
		DoublePoint p = new DoublePoint(3.0,4.0);
		System.out.println(CompPointUtil.norm(p));
	}
}
