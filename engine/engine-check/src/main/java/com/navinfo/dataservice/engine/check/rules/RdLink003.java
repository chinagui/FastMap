package com.navinfo.dataservice.engine.check.rules;

import java.util.Map;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Rdlink html/word RDLINK003 后台 相邻形状点不可过近，不能小于2m
 * 
 * @author zhangxiaoyi
 *
 */

public class RdLink003 extends baseRule {

	public RdLink003() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			if (obj.objType() != ObjType.RDLINK || obj.status() == ObjStatus.DELETE) {
				continue;
			}

			RdLink rdLink = (RdLink) obj;
			Geometry geo = rdLink.getGeometry();
			geo = GeoTranslator.transform(geo, 0.00001, 5);

			Map<String, Object> changedFields = rdLink.changedFields();
			if (changedFields.containsKey("geometry")) {
				JSONObject geojson = (JSONObject) changedFields.get("geometry");
				geo = GeoTranslator.geojson2Jts(geojson);
			}

			Coordinate[] coords = geo.getCoordinates();

			for (int j = 0; j < coords.length - 1; j++) {
				Coordinate current = coords[j];
				Coordinate next = coords[j + 1];
				if (GeometryUtils.getDistance(current, next) < 2) {
					this.setCheckResult("", "", 0);
					return;
				}
			}//for
		}//for
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
	}

}