package com.navinfo.dataservice.engine.edit.edit.operation.obj.rwlink.create;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Check {

	public void checkDupilicateNode(Geometry geo) throws Exception{
		
		Coordinate[] coords = geo.getCoordinates();
		
		for(int i=0;i<coords.length;i++){
			if(i+2<coords.length){
				
				Coordinate current = coords[i];
				
				Coordinate next = coords[i+1];
				
				Coordinate next2 = coords[i+2];
				
				if(current.compareTo(next)==0 || current.compareTo(next2)==0){
					throw new Exception(" 一根link上不能存在坐标相同的形状点");
				}
			}
		}
	}
}
