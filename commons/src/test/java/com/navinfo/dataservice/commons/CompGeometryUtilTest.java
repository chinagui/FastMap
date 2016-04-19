package com.navinfo.dataservice.commons;

import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.CompGridUtil;

/** 
* @ClassName: CompGridUtilTest 
* @author Xiao Xiaowen 
* @date 2016年4月19日 上午10:11:04 
* @Description: TODO
*/
public class CompGeometryUtilTest {
	private static void t1(){
		try{
			//59567003
			double[] line = new double[]{116.0,39.917,116.09375,39.927};
			double[] rect = CompGridUtil.grid2Rect("59567003");
			if(CompGeometryUtil.intersectLineRect(line,rect)){
				System.out.println("Yes...");
			}else{
				System.out.println("No...");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
		t1();
	}
}
