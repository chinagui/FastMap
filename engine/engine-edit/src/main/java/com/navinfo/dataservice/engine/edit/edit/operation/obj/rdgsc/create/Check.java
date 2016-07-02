package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.engine.edit.comm.util.operate.RdGscOperateUtils;
import com.vividsolutions.jts.geom.Geometry;

public class Check {
	
	private Connection conn;
	
	public Check(Connection conn)
	{
		this.conn = conn;
	}
	
	public boolean checkIsHasGsc(Geometry gscGeo, Map<Integer, RdGscLink> map) throws Exception {
		boolean flag = false;

		RdGscSelector selector = new RdGscSelector(conn);

		List<Integer> linkPidList = new ArrayList<>();

		for (RdGscLink rdGscLink : map.values()) {
			if(!linkPidList.contains(rdGscLink.getLinkPid()))
			{
				linkPidList.add(rdGscLink.getLinkPid());
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
	public void checkGsc(Geometry gscGeo,Map<Integer, RdGscLink> map) throws Exception {

		if (gscGeo == null || gscGeo.isEmpty()) {
			throw new Exception("矩形框和线没有相交点");
		}

		if (gscGeo.getNumGeometries() != 1) {
			throw new Exception("矩形框内线的交点有多个");
		}
		
		//检查线上该点位是否存在立交
		List<Integer> linkPidList = new ArrayList<>();

		for (RdGscLink rdGscLink : map.values()) {
			if (!linkPidList.contains(rdGscLink.getLinkPid())) {
				linkPidList.add(rdGscLink.getLinkPid());
			}
		}
		
		boolean flag = RdGscOperateUtils.checkIsHasGsc(gscGeo, linkPidList, conn);

		if (flag) {
			throw new Exception("同一点位不能重复创建立交");
		}
	}
}
