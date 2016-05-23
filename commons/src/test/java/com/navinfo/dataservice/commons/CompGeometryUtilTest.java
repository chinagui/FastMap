package com.navinfo.dataservice.commons;

import org.junit.Test;

import com.navinfo.dataservice.commons.util.JtsGeometryUtil;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Polygon;

import junit.framework.TestCase;

/** 
* @ClassName: CompGridUtilTest 
* @author Xiao Xiaowen 
* @date 2016年4月19日 上午10:11:04 
* @Description: TODO
*/
public class CompGeometryUtilTest{
	
	@Test
	public void t1(){
		try{
			//59567003
			double[] line = new double[]{116.0,39.917,116.09375,39.927};
			double[] rect = CompGridUtil.grid2Rect("59567003");
			if(CompGeometryUtil.intersectLineRect(CompGridUtil.convertLatlon(line),CompGridUtil.convertLatlon(rect))){
				System.out.println("Yes...");
			}else{
				System.out.println("No...");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@Test
	public void intersect_001(){
		try{
			Polygon p1 = (Polygon)JtsGeometryUtil.read("Polygon ((118.01369349658568808 37.6084294790892244, 118.09926057332071991 37.65289740873105728, 118.160909293960529 37.64380169584977409, 118.20335595407318863 37.56362467119253523, 118.15855114617649235 37.61348265291216109, 118.15619299839245571 37.56160340166336198, 118.09454427775264662 37.56092964515363519, 118.07062592165742387 37.59798625318849474, 118.05310825240457007 37.55823461911473515, 118.04300190475871091 37.59832313144335814, 118.02784238328990796 37.55722398435015208, 118.01369349658568808 37.6084294790892244))");
			Polygon p2 = (Polygon)JtsGeometryUtil.read("POLYGON((118.0 37.58333,118.125 37.58333,118.125 37.66666,118.0 37.66666,118.0 37.58333))");
			System.out.println(p1.intersection(p2).toText());
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
