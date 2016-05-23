package com.navinfo.dataservice.commons;

import org.junit.Test;

import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
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
			LineString ls1=(LineString)JtsGeometryFactory.read("LINESTRING(130.0 40.0,130.5 40.5,131.0 40.5)");
			LineString ls2=(LineString)JtsGeometryFactory.read("LINESTRING(131.0 40.5,130.5 41.0,130.5 41.5,131.0 42.0)");
			LineString ls3=(LineString)JtsGeometryFactory.read("LINESTRING(132.0 43.0,132.0 42.0,131.0 42.0)");
			Point startPoint = (Point)JtsGeometryFactory.read("POINT(130.0 40.0)");
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
	/**
	 * 情景：
	 * 右侧切割
	 */
	@Test
	public void cut_001(){
		try{

			LineString ls1=(LineString)JtsGeometryFactory.read("LINESTRING (116.02564 39.76918, 116.02563 39.76873, 116.02605 39.76872)");
			LineString ls2=(LineString)JtsGeometryFactory.read("LINESTRING (116.02605 39.76872, 116.0264 39.76876)");
			LineString ls3=(LineString)JtsGeometryFactory.read("LINESTRING (116.02601 39.76917, 116.02603 39.76889,116.02607 39.76876,116.02624 39.76863)");
			Point fromPoint = (Point)JtsGeometryFactory.read("POINT (116.02601 39.76917)");
			LineString result = CompPolylineUtil.cut(ls1, ls2, ls3, fromPoint, true);
			System.out.println(result.toText());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * 情景：
	 * 右侧切割
	 */
	@Test
	public void cut_001_01(){
		try{

			LineString ls1=(LineString)JtsGeometryFactory.read("LINESTRING (116.02564 39.76918, 116.02564 39.769, 116.02603 39.76899)");
			LineString ls2=(LineString)JtsGeometryFactory.read("LINESTRING (116.02603 39.76899, 116.02637 39.76903)");
			LineString ls3=(LineString)JtsGeometryFactory.read("LINESTRING (116.02601 39.76917, 116.02603 39.76889)");
			Point fromPoint = (Point)JtsGeometryFactory.read("POINT (116.02601 39.76917)");
			LineString result = CompPolylineUtil.cut(ls1, ls2, ls3, fromPoint, true);
			System.out.println(result.toText());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * 情景：
	 * 左侧切割
	 */
	@Test
	public void cut_002(){
		try{

			LineString ls1=(LineString)JtsGeometryFactory.read("LINESTRING (116.02564 39.76918, 116.02563 39.76873, 116.02605 39.76872)");
			LineString ls2=(LineString)JtsGeometryFactory.read("LINESTRING (116.02605 39.76872, 116.0264 39.76876)");
			LineString ls3=(LineString)JtsGeometryFactory.read("LINESTRING (116.02601 39.76917, 116.02603 39.76889,116.02607 39.76876,116.02624 39.76863)");
			Point fromPoint = (Point)JtsGeometryFactory.read("POINT (116.02624 39.76863)");
			LineString result = CompPolylineUtil.cut(ls1, ls2, ls3, fromPoint, false);
			System.out.println(result.toText());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * 情景：
	 * 上下线分离后的线超过了targetLine，此时返回空
	 */
	@Test
	public void cut_003(){
		try{

			LineString ls1=(LineString)JtsGeometryFactory.read("LINESTRING (116.02564 39.76918, 116.02563 39.76873, 116.02605 39.76872)");
			LineString ls2=(LineString)JtsGeometryFactory.read("LINESTRING (116.02605 39.76872, 116.0264 39.76876)");
			LineString ls3=(LineString)JtsGeometryFactory.read("LINESTRING (116.02601 39.76917, 116.02603 39.76889)");
			Point fromPoint = (Point)JtsGeometryFactory.read("POINT (116.02601 39.76917)");
			LineString result = CompPolylineUtil.cut(ls1, ls2, ls3, fromPoint, true);
			System.out.println(result.toText());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
