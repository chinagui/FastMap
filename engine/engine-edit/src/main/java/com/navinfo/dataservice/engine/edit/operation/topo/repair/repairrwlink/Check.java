package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrwlink;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.engine.edit.utils.RdGscOperateUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class Check {

	// 形状点和形状点不能重合
	public void checkPointCoincide(double[][] ps) throws Exception {

		Set<String> set = new HashSet<String>();

		for (double[] p : ps) {
			set.add(p[0] + "," + ps[1]);
		}

		if (ps.length != set.size()) {
			throwException("形状点和形状点不能重合");
		}
	}

	// 相邻形状点不可过近，不能小于2m
	public void checkShapePointDistance(JSONObject geom) throws Exception {

		Geometry g = GeoTranslator.geojson2Jts(geom);

		Coordinate[] coords = g.getCoordinates();

		for (int i = 0; i < coords.length - 1; i++) {

			double distance = GeometryUtils.getDistance(coords[i].y, coords[i].x, coords[i + 1].y, coords[i + 1].x);

			if (distance <= 2) {
				throwException("相邻形状点不可过近，不能小于2m");
			}
		}
	}

	public void checkIsMoveGscPoint(JSONObject linkGeo, Connection conn, int linkPid) throws Exception {
		
		Geometry geo = GeoTranslator.geojson2Jts(linkGeo, 100000, 0);

		RdGscSelector selector = new RdGscSelector(conn);

		List<RdGsc> rdGscList = selector.onlyLoadRdGscLinkByLinkPid(linkPid,
				"RW_LINK", true);

		boolean flag = RdGscOperateUtils.isMoveGscLink(geo, rdGscList,
				"RW_LINK", linkPid);

		if (flag) {
			throw new Exception("不允许去除有立交关系的形状点");
		}
	}

	private void throwException(String msg) throws Exception {
		throw new Exception(msg);
	}

}
