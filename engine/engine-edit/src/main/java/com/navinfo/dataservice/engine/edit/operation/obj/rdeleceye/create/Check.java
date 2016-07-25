package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.create;

import com.navinfo.navicommons.geo.computation.MeshUtils;

public class Check {
	public void checkGeometryNoOnMeshBoarder(double lon, double lat) throws Exception {
		if (MeshUtils.isPointAtMeshBorder(lon, lat)) {
		}
	}
}
