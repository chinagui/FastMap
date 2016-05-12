package com.navinfo.dataservice.commons;

import org.junit.Test;

import com.navinfo.dataservice.commons.util.JtsGeometryUtil;
import com.navinfo.navicommons.geo.computation.CompPolylineUtil;
import com.navinfo.navicommons.geo.computation.DoubleLine;
import com.navinfo.navicommons.geo.computation.DoublePoint;
import com.navinfo.navicommons.geo.computation.DoublePolyline;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import junit.framework.Assert;

/** 
* @ClassName: CompLineUtil 
* @author Xiao Xiaowen 
* @date 2016年5月4日 上午11:06:15 
* @Description: TODO
*/
public class CompPolylineUtilTest {
	/**
	 * 水平line
	 */
	@Test
	public void t_000_00(){
		DoublePolyline polyline1 = new DoublePolyline(new DoublePoint[]{
				new DoublePoint(130.0,40.0),new DoublePoint(130.5,40.0)
		});

		DoublePolyline[] polylines = new DoublePolyline[]{polyline1};
		DoublePolyline[] results = CompPolylineUtil.offset(polylines, 1200);
		for(DoublePolyline line:results){
			System.out.println(line);
		}
		Assert.assertTrue(true);
	}
	/**
	 * 垂直线
	 */
	@Test
	public void t_000_01(){
		DoublePolyline polyline1 = new DoublePolyline(new DoublePoint[]{
				new DoublePoint(130.0,40.0),new DoublePoint(130.0,40.5)
		});

		DoublePolyline[] polylines = new DoublePolyline[]{polyline1};
		DoublePolyline[] results = CompPolylineUtil.offset(polylines, 1200);
		for(DoublePolyline line:results){
			System.out.println(line);
		}
		Assert.assertTrue(true);
	}

	/**
	 * 水平线+垂直线
	 */
	@Test
	public void t_000_02(){
		DoublePolyline polyline1 = new DoublePolyline(new DoublePoint[]{
				new DoublePoint(131.0,42.0),new DoublePoint(132.0,42.0),new DoublePoint(132.0,43.0)
		});

		DoublePolyline[] polylines = new DoublePolyline[]{polyline1};
		DoublePolyline[] results = CompPolylineUtil.offset(polylines, 1200);
		for(DoublePolyline line:results){
			System.out.println(line);
		}
		Assert.assertTrue(true);
	}
	@Test
	public void t_001_01(){
		DoublePolyline polyline1 = new DoublePolyline(new DoublePoint[]{
				new DoublePoint(130.0,40.0),new DoublePoint(130.5,40.5),new DoublePoint(131.0,40.5)
		});
		DoublePolyline polyline2 = new DoublePolyline(new DoublePoint[]{
				new DoublePoint(131.0,40.5),new DoublePoint(130.5,41.0),new DoublePoint(130.5,41.5),new DoublePoint(131.0,42.0)
		});
		DoublePolyline polyline3 = new DoublePolyline(new DoublePoint[]{
				new DoublePoint(131.0,42.0),new DoublePoint(132.0,42.0),new DoublePoint(132.0,43.0)
		});

		DoublePolyline[] polylines = new DoublePolyline[]{polyline1,polyline2,polyline3};
		DoublePolyline[] results = CompPolylineUtil.offset(polylines, 1200);
		for(DoublePolyline line:results){
			System.out.println(line);
		}
		Assert.assertTrue(true);
	}
	@Test
	public void separate_01(){
		try{
			LineString ls1=(LineString)JtsGeometryUtil.read("LINESTRING(130.0 40.0,130.5 40.5,131.0 40.5)");
			LineString ls2=(LineString)JtsGeometryUtil.read("LINESTRING(131.0 40.5,130.5 41.0,130.5 41.5,131.0 42.0)");
			LineString ls3=(LineString)JtsGeometryUtil.read("LINESTRING(132.0 43.0,132.0 42.0,131.0 42.0)");
			Point startPoint = (Point)JtsGeometryUtil.read("POINT(130.0 40.0)");
			LineString[] lines = new LineString[]{
					ls1,ls2,ls3
			};
			LineString[] results = CompPolylineUtil.separate(startPoint, lines, 12000);
			for(LineString l:results){
				System.out.println(l.toText());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		Assert.assertTrue(true);
	}
}
