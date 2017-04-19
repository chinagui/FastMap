
package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONObject;

/**
 * Created by Crayeres on 2017/2/20. 前检查：背景link不能自相交
 * 
 * @author Feng Haixia
 * @since 2017/4/12
 * 
 */
public class ShapingCheckLinkSelfintersect2 extends baseRule {
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row.status() == ObjStatus.DELETE) {
				continue;
			}

			ObjType objType = row.objType();

			switch (objType) {
			case ADLINK:
				AdLink adLink = (AdLink) row;
				checkDiffObjIsIntersect(row, adLink.getGeometry());
				break;
			case ZONELINK:
				ZoneLink zoneLink = (ZoneLink) row;
				checkDiffObjIsIntersect(row, zoneLink.getGeometry());
				break;
			case RWLINK:
				RwLink rwLink = (RwLink) row;
				checkDiffObjIsIntersect(row, rwLink.getGeometry());
				break;
			case LULINK:
				LuLink luLink = (LuLink) row;
				checkDiffObjIsIntersect(row, luLink.getGeometry());
				break;
			case LCLINK:
				LcLink lcLink = (LcLink) row;
				checkDiffObjIsIntersect(row, lcLink.getGeometry());
				break;
			case CMGBUILDLINK:
				CmgBuildlink cmgLink = (CmgBuildlink) row;
				checkDiffObjIsIntersect(row, cmgLink.getGeometry());
				break;
			default:
				break;
			}
		} // for
	}

	/**
	 * 判断对象的几何是否自相交
	 * 
	 * @param row
	 * @param geo
	 * @throws Exception
	 */
	private void checkDiffObjIsIntersect(IRow row, Geometry geo) throws Exception {
		if (row.changedFields().containsKey("geometry")) {
			JSONObject obj = (JSONObject) row.changedFields().get("geometry");
			geo = GeoTranslator.geojson2Jts(obj);
		}
		
		 List<Point> points = new ArrayList<>();
         GeoHelper.isSample(geo, points);
         if (!points.isEmpty()){
        	 this.setCheckResult("", "", 0);}
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {

	}
}
