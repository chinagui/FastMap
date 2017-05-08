package com.navinfo.dataservice.engine.fcc.tips;

import java.util.HashSet;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: TipsGridCalculate.java
 * @author y
 * @date 2017-4-19 下午9:43:42
 * @Description: TODO
 *  
 */
public class TipsGridCalculate {
	
	
	
	/**
	 * @Description:根据geomery返回对应的girds
	 * @param geo
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2017-4-19 下午9:47:54
	 */
	public static Set<String> calculate(Geometry geo) throws Exception{
		
		Set<String> grids=new HashSet<String>();
		
		String geoType=geo.getGeometryType();
		
		//简单几何的处理方法
		if(GeometryTypeName.POINT.equals(geoType)||GeometryTypeName.LINESTRING.equals(geoType)||GeometryTypeName.POLYGON.equals(geoType)){
			
			grids=CompGeometryUtil.geo2GridsWithoutBreak(geo);
		}
		
		else if ( GeometryTypeName.MULTILINESTRING.equals(geoType)||GeometryTypeName.MULTIPOINT.equals(geoType)||GeometryTypeName.MULTIPOLYGON.equals(geoType)) {
            for (int i = 0; i < geo.getNumGeometries(); i++) {
            	
            	 Geometry geometry = geo.getGeometryN(i);
            	
            	Set<String> grid=CompGeometryUtil.geo2GridsWithoutBreak(geometry);
            	
            	grids.addAll(grid);
              }
            }
        else if(GeometryTypeName.MULTILINESTRING.equals(geoType)){
        	
            for (int i = 0; i < geo.getNumGeometries(); i++) {
                
            	Geometry geometry = geo.getGeometryN(i);
                	
                	//简单几何的处理方法
        			if(GeometryTypeName.POINT.equals(geometry.getGeometryType())||GeometryTypeName.LINESTRING.equals(geometry.getGeometryType())||GeometryTypeName.POLYGON.equals(geometry.getGeometryType())){
        				
        				grids=CompGeometryUtil.geo2GridsWithoutBreak(geo);
        			}
        			
        			else if ( GeometryTypeName.MULTILINESTRING.equals(geometry.getGeometryType())||GeometryTypeName.MULTIPOINT.equals(geometry.getGeometryType())||GeometryTypeName.MULTIPOLYGON.equals(geometry.getGeometryType())) {
                        for (int k = 0; k < geo.getNumGeometries(); k++) {
                        	
                        	Set<String> grid=CompGeometryUtil.geo2GridsWithoutBreak(geo);
                        	
                        	grids.addAll(grid);
                          }
                        }
                }
            }
        
		
		return grids;
		
	}

}
