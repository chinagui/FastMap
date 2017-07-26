package com.navinfo.dataservice.commons;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;


/** 
* @ClassName: CompGridUtilTest 
* @author Xiao Xiaowen 
* @date 2016年4月19日 上午10:11:04 
* @Description: TODO
*/
public class CompGeometryUtilTest{
	
	@Test
	public void intersect_001(){
		try{
			Polygon p1 = (Polygon)JtsGeometryFactory.read("Polygon ((118.01369349658568808 37.6084294790892244, 118.09926057332071991 37.65289740873105728, 118.160909293960529 37.64380169584977409, 118.20335595407318863 37.56362467119253523, 118.15855114617649235 37.61348265291216109, 118.15619299839245571 37.56160340166336198, 118.09454427775264662 37.56092964515363519, 118.07062592165742387 37.59798625318849474, 118.05310825240457007 37.55823461911473515, 118.04300190475871091 37.59832313144335814, 118.02784238328990796 37.55722398435015208, 118.01369349658568808 37.6084294790892244))");
			Polygon p2 = (Polygon)JtsGeometryFactory.read("POLYGON((118.0 37.58333,118.125 37.58333,118.125 37.66666,118.0 37.66666,118.0 37.58333))");
			Geometry result = p1.intersection(p2);
			System.out.println(result.getGeometryType());
			System.out.println(result.getNumGeometries());
			System.out.println(result.getGeometryN(0).toText());
			System.out.println(result.toText());
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	@Test
	public void intersect_002(){
		try{
			Polygon p1 = (Polygon)JtsGeometryFactory.read("Polygon ((118.01369349658568808 37.6084294790892244, 118.09926057332071991 37.65289740873105728, 118.160909293960529 37.64380169584977409, 118.20335595407318863 37.56362467119253523, 118.15855114617649235 37.61348265291216109, 118.15619299839245571 37.56160340166336198, 118.09454427775264662 37.56092964515363519, 118.07062592165742387 37.59798625318849474, 118.05310825240457007 37.55823461911473515, 118.04300190475871091 37.59832313144335814, 118.02784238328990796 37.55722398435015208, 118.01369349658568808 37.6084294790892244))");
			Polygon p2 = (Polygon)JtsGeometryFactory.read("POLYGON((118.0 37.5,118.125 37.5,118.125 37.58333,118.0 37.58333,118.0 37.5))");
			Geometry result = p1.intersection(p2);
			System.out.println(result.getGeometryType());
			System.out.println(result.getNumGeometries());
			System.out.println(result.getGeometryN(0).toText());
			System.out.println(result.toText());
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	/**
	 * 在图幅内，和图廓线只有一个交点的情况
	 */
	@Test
	public void intersect_003(){
		try{
			Polygon p1 = (Polygon)JtsGeometryFactory.read("Polygon ((118.02 37.56,118.07 37.5,118.11 37.56,118.02 37.56))");
			Polygon p2 = (Polygon)JtsGeometryFactory.read("POLYGON((118.0 37.5,118.125 37.5,118.125 37.58333,118.0 37.58333,118.0 37.5))");
			Geometry result = p1.intersection(p2);
			System.out.println(result.getGeometryType());
			System.out.println(result.getNumGeometries());
			System.out.println(result.getGeometryN(0).toText());
			System.out.println(result.toText());
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	/**
	 * 多边形被图幅打断，跨越4个图幅
	 */
	@Test
	public void cut_001(){
		try{
			Polygon p = (Polygon)JtsGeometryFactory.read("Polygon ((118.0137 37.60843, 118.09926 37.6529, 118.16091 37.6438, 118.20336 37.56362, 118.15855 37.61348, 118.15619 37.5616, 118.09454 37.56093, 118.07063 37.59799, 118.05311 37.55823, 118.043 37.59832, 118.02784 37.55722, 118.0137 37.60843))");
			String[] meshes = new String[]{"565820","565821","565831","565830"};
			Map<String,Set<LineString[]>> result = CompGeometryUtil.cut(p, meshes);
			int c = 1;
			for(String key:result.keySet()){
				//System.out.println(key+":");
				for(LineString[] lss:result.get(key)){
					for(LineString ls:lss){
						System.out.println(ls.toText()+"\t"+c++);
					}
					//System.out.print("\n");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void geo2GridsWithoutBreak_001(){
		try{
			Polygon p = (Polygon)JtsGeometryFactory.read("POLYGON((116.30559466134446 39.92893348687963,116.30559466134446 39.98670948206808,116.43661979549893 39.98670948206808,116.43661979549893 39.92893348687963,116.30559466134446 39.92893348687963))");
			Set<String> result = CompGeometryUtil.geo2GridsWithoutBreak(p);
			System.out.println(StringUtils.join(result, ","));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void intersect_004(){
		try{
			Geometry p = JtsGeometryFactory.read("POLYGON((116.38559 39.92893,116.30559 39.9867,116.43661 39.9867,116.43661 39.92893,116.38559 39.92893))");

			Geometry l = JtsGeometryFactory.read("LINESTRING (116.28432 39.93663,116.3577 39.9766, 116.57634 39.93663)");
			Geometry r = p.intersection(l);
			System.out.println(r.getGeometryType());

			System.out.println(r.toText());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
