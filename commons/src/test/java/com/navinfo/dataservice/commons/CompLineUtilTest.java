package com.navinfo.dataservice.commons;

import org.junit.Test;

import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.navicommons.geo.computation.CompLineUtil;
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
	
	@Test
	public void isRightSide_001(){
		DoubleLine line1 = new DoubleLine(new DoublePoint(0.0,0.0),
				new DoublePoint(1.0,0.0));
		DoubleLine line2 = new DoubleLine(new DoublePoint(1.0,0.0),
				new DoublePoint(2.0,1.0));
		DoubleLine lineAdj = new DoubleLine(new DoublePoint(1.0,0.0),
				new DoublePoint(2.0,1.1));
		if(CompLineUtil.isRightSide(line1,line2,lineAdj)){
			System.out.println("YESSSSS...");
		}else{
			System.out.println("NOOOOOO...");
		}
	}
}
