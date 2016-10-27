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
			LineString ls1=(LineString)JtsGeometryFactory.read("LINESTRING (116.3563 40.03676, 116.35615 40.03672, 116.35601 40.03668, 116.35589 40.03665, 116.3558 40.03662, 116.35573 40.03659, 116.35568 40.03656, 116.35566 40.03652, 116.35565 40.03649, 116.35566 40.0364, 116.35568 40.03636, 116.35571 40.03629, 116.35572 40.03625, 116.35577 40.03617)");
			LineString ls2=(LineString)JtsGeometryFactory.read("LINESTRING (116.3563 40.03676, 116.35651 40.03682, 116.35696 40.03695)");
			LineString ls3=(LineString)JtsGeometryFactory.read("LINESTRING (116.35696 40.03695, 116.35715 40.03701, 116.35716 40.03704, 116.35716 40.03708, 116.35714 40.03711, 116.35711 40.03711, 116.3567 40.03701)");
			Point startPoint = (Point)JtsGeometryFactory.read("POINT (116.35577 40.03617)");
			LineString[] lines = new LineString[]{
					ls1,ls2,ls3
			};
			LineString[] results = CompPolylineUtil.separate(startPoint, lines, 7.7);
			for(LineString l:results){
				System.out.println(l.toText());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		Assert.assertTrue(true);
	}
	@Test
	public void separate_02(){
		try{
			LineString ls=(LineString)JtsGeometryFactory.read("LINESTRING (116.35696 40.03695, 116.35715 40.03701, 116.35716 40.03704, 116.35716 40.03708, 116.35714 40.03711, 116.35711 40.03711, 116.3567 40.03701)");
			Point startPoint = (Point)JtsGeometryFactory.read("POINT (116.35696 40.03695)");
			LineString[] lines = new LineString[]{
					ls
			};
			LineString[] results = CompPolylineUtil.separate(startPoint, lines, 7.7);
			for(LineString l:results){
				System.out.println(l.toText());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		Assert.assertTrue(true);
	}

	@Test
	public void separate_03(){
		try{
			LineString ls1=(LineString)JtsGeometryFactory.read("LINESTRING (116.19394 40.53876, 116.19402 40.53878)");
			LineString ls2=(LineString)JtsGeometryFactory.read("LINESTRING (116.19402 40.53878, 116.19408 40.5388)");
			LineString ls3=(LineString)JtsGeometryFactory.read("LINESTRING (116.19408 40.5388, 116.19411 40.53881)");
			LineString ls4=(LineString)JtsGeometryFactory.read("LINESTRING (116.19411 40.53881, 116.19418 40.53883)");
			LineString ls5=(LineString)JtsGeometryFactory.read("LINESTRING (116.19418 40.53883, 116.19426 40.53885)");
			LineString ls6=(LineString)JtsGeometryFactory.read("LINESTRING (116.19426 40.53885, 116.19432 40.53886)");
			LineString ls7=(LineString)JtsGeometryFactory.read("LINESTRING (116.19432 40.53886, 116.19438 40.53887)");
			LineString ls8=(LineString)JtsGeometryFactory.read("LINESTRING (116.19438 40.53887, 116.19444 40.53888)");
			LineString ls9=(LineString)JtsGeometryFactory.read("LINESTRING (116.19444 40.53888, 116.19448 40.53888)");
			LineString ls10=(LineString)JtsGeometryFactory.read("LINESTRING (116.19448 40.53888, 116.19455 40.53889)");
			LineString ls11=(LineString)JtsGeometryFactory.read("LINESTRING (116.19455 40.53889, 116.19464 40.5389)");
			Point startPoint = (Point)JtsGeometryFactory.read("POINT (116.19394 40.53876)");
			LineString[] lines = new LineString[]{
					ls1,ls2,ls3,ls4,ls5,ls6,ls7,ls8,ls9,ls10,ls11
			};
			LineString[] results = CompPolylineUtil.separate(startPoint, lines, 8.3);
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
