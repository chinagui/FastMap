package com.navinfo.dataservice.commons;

import org.junit.Test;

import com.navinfo.navicommons.geo.computation.MyGeometryConvertor;
import com.vividsolutions.jts.geom.Polygon;

public class MyGeometryConvertorTest {
	@Test
	public void convert_001(){
		Polygon p = MyGeometryConvertor.convert(new double[]{0,0,5,5});
		System.out.println(p.toText());
	}
}
