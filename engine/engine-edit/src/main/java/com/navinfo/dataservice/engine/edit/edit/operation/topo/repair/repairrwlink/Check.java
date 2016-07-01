package com.navinfo.dataservice.engine.edit.edit.operation.topo.repair.repairrwlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.engine.edit.comm.util.operate.RdGscOperateUtils;
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

			double distance = GeometryUtils.getDistance(coords[i].y,
					coords[i].x, coords[i + 1].y, coords[i + 1].x);

			if (distance <= 2) {
				throwException("相邻形状点不可过近，不能小于2m");
			}
		}
	}
	
	/**
	 * 检查修行的线修行的点上是否有立交存在
	 * @param linkPid
	 * @param geo
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public boolean checkIsGscPoint(int linkPid,Geometry geo,Connection conn) throws Exception
	{
		List<Integer> linkPidList = new ArrayList<>();
		
		linkPidList.add(linkPid);
		
		return RdGscOperateUtils.checkIsHasGsc(geo, linkPidList, conn);
	}
	
	private void throwException(String msg) throws Exception {
		throw new Exception(msg);
	}

}
