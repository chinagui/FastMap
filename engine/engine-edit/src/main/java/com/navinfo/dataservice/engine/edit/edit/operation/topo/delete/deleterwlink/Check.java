package com.navinfo.dataservice.engine.edit.edit.operation.topo.delete.deleterwlink;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class Check {

	public void checkDupilicateNode(JSONObject geometry) throws Exception{
		
		Geometry geo = GeoTranslator.geojson2Jts(geometry);
		
		Coordinate[] coords = geo.getCoordinates();
		
		for(int i=0;i<coords.length;i++){
			if(i+2<coords.length){
				
				Coordinate current = coords[i];
				
				Coordinate next = coords[i+1];
				
				Coordinate next2 = coords[i+2];
				
				if(current.compareTo(next)==0 || current.compareTo(next2)==0){
					throw new Exception("一根link上不能存在坐标相同的形状点");
				}
			}
		}
	}
}
