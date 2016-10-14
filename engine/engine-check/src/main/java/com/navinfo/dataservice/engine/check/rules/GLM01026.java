package com.navinfo.dataservice.engine.check.rules;

import java.util.Map;


import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
/**
 * Rdlink	html	GLM01026	后台	除起终点，Link的形状点不能在图廓上？？？
 * @author zhangxiaoyi
 *
 */
public class GLM01026 extends baseRule {
	
	public void preCheck(CheckCommand checkCommand) throws Exception{
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;
				
				Geometry geo = rdLink.getGeometry();
				Map<String, Object> changedFields = rdLink.changedFields();
				if(changedFields!=null && changedFields.containsKey("geometry")){
					JSONObject geojson=(JSONObject) changedFields.get("geometry");
					geo=GeoTranslator.geojson2Jts(geojson);}

				if(this.hasBorderNode(geo)){
					this.setCheckResult("", "", 0);
					return;
					}
				}
			}
		}
	/**
	 * 除起终点，Link的形状点不能在图廓上
	 * @param geo
	 * @return 有形状点在图廓上，return true；否则false
	 */
	private boolean hasBorderNode(Geometry geo){
		Coordinate[] coords = geo.getCoordinates();				
		for(int j=1;j<coords.length-1;j++){
			Coordinate current = coords[j];
			if (MeshUtils.isPointAtMeshBorderWith100000(current.x,current.y)==true){
				return true;
			}
		}
		return false;
	}

	public void postCheck(CheckCommand checkCommand) throws Exception{
	}
	
}