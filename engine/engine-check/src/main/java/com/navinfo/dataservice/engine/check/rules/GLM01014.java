package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.NiValException;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.navinfo.dataservice.engine.check.core.NiValException;

public class GLM01014 extends baseRule {
	
	public void preCheck(CheckCommand checkCommand){
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;
				
				Coordinate[] cs = rdLink.getGeometry().getCoordinates();
				
				int midP = (int)Math.round(cs.length/2);
				
				double x = cs[midP].x;
				
				double y = cs[midP].y;
				
				String pointWkt = "Point ("+x+" "+y+")";
				
				if (rdLink.getsNodePid() == rdLink.geteNodePid()){
					this.setCheckResult(pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId());
					return;
					}
				}
			}
	}

	public void postCheck(CheckCommand checkCommand){
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;
				
				Coordinate[] cs = rdLink.getGeometry().getCoordinates();
				
				int midP = (int)Math.round(cs.length/2);
				
				double x = cs[midP].x;
				
				double y = cs[midP].y;
				
				String pointWkt = "Point ("+x+" "+y+")";
				
				if (rdLink.getsNodePid() == rdLink.geteNodePid()){
					this.setCheckResult(pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId());
					}
				}
			}
	}
}
