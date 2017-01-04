package com.navinfo.dataservice.engine.check.rules;


import java.util.Map;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Rdlink	word	RDLINK002	后台	不允许移动形状点到角点处
 * @author zhangxiaoyi
 *
 */

public class RdLink002 extends baseRule {

	public RdLink002() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;
				Map<String, Object> changedFields = rdLink.changedFields();
				if(changedFields==null || !changedFields.containsKey("geometry")){continue;}
				if(changedFields.containsKey("eNodePid") || changedFields.containsKey("sNodePid")){continue;}
				Geometry geo=GeoTranslator.transform(rdLink.getGeometry(), 0.00001, 5);
				Coordinate[] coordOlds = geo.getCoordinates();
				JSONObject geojson=(JSONObject) changedFields.get("geometry");
				Geometry geoNew=GeoTranslator.geojson2Jts(geojson);
				Coordinate[] coords = geoNew.getCoordinates();
				//是否移动端点
				if(coords[0].equals(coordOlds[0])){
					if(isCorner(coords[0].x,coords[0].y)){
						this.setCheckResult("", "", 0);
						break;
					}
				}else if(coords[coords.length-1].equals(coordOlds[coordOlds.length-1])){
					if(isCorner(coords[coords.length-1].x,coords[coords.length-1].y)){
						this.setCheckResult("", "", 0);
						break;
					}
				}
				//仅有端点，没有形状点
				if(coords.length<=2){continue;}
				//形状点不能到角点处
				for(int j=1;j<coords.length-1;j++){
					if(isCorner(coords[j].x,coords[j].y)){
						this.setCheckResult("", "", 0);
						break;
						}
					}
			}
			}
	}

	private boolean isCorner(double x,double y){
		String[] meshes = MeshUtils.point2Meshes(x,y);
		if(meshes.length==4){return true;}
		else{return false;}
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {}

	}