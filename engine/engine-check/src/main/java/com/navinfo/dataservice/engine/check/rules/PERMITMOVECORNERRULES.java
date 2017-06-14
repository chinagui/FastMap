package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/**
 * 该点是角点，不能移动
 * 
 * @author Feng Haixia
 * @since 2017/4/14
 */
public class PERMITMOVECORNERRULES extends baseRule {
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if ((row.status() == ObjStatus.DELETE) || (!row.changedFields().containsKey("geometry"))) {
				continue;
			}

			ObjType objType = row.objType();

			switch (objType) {
			case ADNODE:
				AdNode adLink = (AdNode) row;
				checkDiffObjIsCornerNode(adLink, adLink.getGeometry());
				break;
			case ZONENODE:
				ZoneNode zoneLink = (ZoneNode) row;
				checkDiffObjIsCornerNode(zoneLink,zoneLink.getGeometry());
				break;
			case RWNODE:
				RwNode rwLink = (RwNode) row;
				checkDiffObjIsCornerNode(rwLink,rwLink.getGeometry());
				break;
			case LUNODE:
				LuNode luLink = (LuNode) row;
				checkDiffObjIsCornerNode(luLink,luLink.getGeometry());
				break;
			case LCNODE:
				LcNode lcLink = (LcNode) row;
				checkDiffObjIsCornerNode(lcLink,lcLink.getGeometry());
				break;
			case CMGBUILDNODE:
				CmgBuildnode cmgLink = (CmgBuildnode) row;
				checkDiffObjIsCornerNode(cmgLink,cmgLink.getGeometry());
				break;
			default:
				break;
			}
		}//for
	}

	private void checkDiffObjIsCornerNode(IRow row,Geometry geom) throws Exception{		
		if (isPointCornerNode(geom)) {
			this.setCheckResult("", "", 0);
		}
	}
	
	/**
	 * 判断该node点是否为角点
	 * 
	 * @param geo
	 *            node几何
	 * @return
	 */
	public boolean isPointCornerNode(Geometry geo) {
		boolean result = false;
		Coordinate coord =GeoTranslator.transform(geo, 0.00001, 5).getCoordinate(); 
	
		if (coord == null)
			return result;

		String[] meshes = MeshUtils.point2Meshes(coord.x, coord.y);
		result = meshes.length == 4 ? true : false;

		return result;
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
	}

}
