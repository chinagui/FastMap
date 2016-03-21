package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdspeedlimit.create;

import com.navinfo.dataservice.commons.util.MeshUtils;

public class Check {

	public void checkGeometryNoOnMeshBoarder(double lon, double lat) throws Exception{
		
		if(MeshUtils.isPointAtMeshBorder(lon, lat)){
			throw new Exception("点限速点位不能在图框线上");
		}
	}
}
