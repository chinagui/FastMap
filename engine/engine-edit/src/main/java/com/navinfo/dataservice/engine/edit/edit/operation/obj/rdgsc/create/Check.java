package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.vividsolutions.jts.geom.Geometry;

public class Check {

	public boolean checkIsHasGsc(Geometry gscGeo, Map<Integer, Integer> map, Connection conn) throws Exception {
		boolean flag = false;

		RdGscSelector selector = new RdGscSelector(conn);

		List<Integer> linkPidList = new ArrayList<>();

		for (Integer linkPid : map.keySet()) {
			linkPidList.add(linkPid);
		}

		List<RdGsc> rdGscList = selector.loadRdGscByInterLinkPids(linkPidList, false);
		
		for(RdGsc gsc : rdGscList)
		{
			if(gsc.getGeometry().equals(gscGeo))
			{
				flag = true;
				break;
			}
		}
		
		return flag;
	}
}
