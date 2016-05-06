package com.navinfo.dataservice.commons;

import org.junit.Test;

import com.navinfo.navicommons.geo.computation.CompLineUtil;
import com.navinfo.navicommons.geo.computation.DoubleLine;
import com.navinfo.navicommons.geo.computation.DoublePoint;
import com.navinfo.navicommons.geo.computation.DoublePolyline;

import junit.framework.Assert;

/** 
* @ClassName: CompLineUtil 
* @author Xiao Xiaowen 
* @date 2016年5月4日 上午11:06:15 
* @Description: TODO
*/
public class CompLineUtilTest {
	@Test
	public void LineExtIntersect_001(){
		DoublePoint p = CompLineUtil.LineExtIntersect(new DoubleLine(new DoublePoint(131.0,42.01078),new DoublePoint(132.0,42.01078))
				,new DoubleLine(new DoublePoint(131.98922,42.0),new DoublePoint(131.98922,43.0)));
		System.out.println(p);
	}

	@Test
	public void LineExtIntersect_002(){
		DoublePoint p = CompLineUtil.LineExtIntersect(new DoubleLine(new DoublePoint(130.99238,40.49238),new DoublePoint(130.49238,40.99238))
				,new DoubleLine(new DoublePoint(130.48922,41.0),new DoublePoint(130.48922,41.5)));
		System.out.println(p);
	}
	/**
	 * 水平line
	 */
	@Test
	public void t_000_00(){
		DoublePolyline polyline1 = new DoublePolyline(new DoublePoint[]{
				new DoublePoint(130.0,40.0),new DoublePoint(130.5,40.0)
		});

		DoublePolyline[] polylines = new DoublePolyline[]{polyline1};
		DoublePolyline[] results = CompLineUtil.offset(polylines, 1200);
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
		DoublePolyline[] results = CompLineUtil.offset(polylines, 1200);
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
		DoublePolyline[] results = CompLineUtil.offset(polylines, 1200);
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
		DoublePolyline[] results = CompLineUtil.offset(polylines, 1200);
		for(DoublePolyline line:results){
			System.out.println(line);
		}
		Assert.assertTrue(true);
	}
}
