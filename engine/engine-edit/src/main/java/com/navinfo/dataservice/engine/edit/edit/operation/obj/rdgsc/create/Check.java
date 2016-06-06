package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.vividsolutions.jts.geom.Geometry;

public class Check {
	
	private Connection conn;
	
	public Check(Connection conn)
	{
		this.conn = conn;
	}
	
	public boolean checkIsHasGsc(Geometry gscGeo, Map<Integer, Integer> map) throws Exception {
		boolean flag = false;

		RdGscSelector selector = new RdGscSelector(conn);

		List<Integer> linkPidList = new ArrayList<>();

		for (Integer linkPid : map.values()) {
			if(!linkPidList.contains(linkPid))
			{
				linkPidList.add(linkPid);
			}
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
	
	/**
	 * 立交检查
	 * 
	 * @param gscGeo
	 * @throws Exception
	 */
	public void checkGsc(Geometry gscGeo,Map<Integer, Integer> map) throws Exception {

		if (gscGeo == null || gscGeo.isEmpty()) {
			throw new Exception("矩形框和线没有相交点");
		}

		if (gscGeo.getNumGeometries() != 1) {
			throw new Exception("矩形框内线的交点有多个");
		}

		boolean flag = checkIsHasGsc(gscGeo, map);

		if (flag) {
			throw new Exception("同一点位不能重复创建立交");
		}
	}
}
