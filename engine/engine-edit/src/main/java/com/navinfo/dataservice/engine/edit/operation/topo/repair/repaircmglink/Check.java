package com.navinfo.dataservice.engine.edit.operation.topo.repair.repaircmglink;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildfaceTopo;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildfaceSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildlinkSelector;
import com.navinfo.dataservice.engine.edit.operation.topo.repair.repaircmglink.Command;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class Check {
	// 背景：前检查“不允许对构成面的Link的端点处形状点，进行修形操作”
	public void PERMIT_MODIFICATE_POLYGON_ENDPOINT(Command command, Connection conn) throws Exception {
		int linkPid = command.getCmglink().getPid();
		CmgBuildfaceSelector selector = new CmgBuildfaceSelector(conn);
		List<CmgBuildface> faces = selector.listTheAssociatedFaceOfTheLink(linkPid, false);

		if (command.getCatchInfos() == null || faces.size() == 0) {
			return;
		}

		for (int i = 0; i < command.getCatchInfos().size(); i++) {
			JSONObject obj = command.getCatchInfos().getJSONObject(i);
			int nodePid = obj.getInt("nodePid");
			if (faces.size() > 0 && nodePid != 0) {
				throwException("不允许对构成面的Link的端点处形状点，进行修形操作");
			}
		}
	}

	
	/**
	 * CMG_LINK修形，背景面不能自相交
	 * 
	 * @param command
	 * @param conn
	 * @throws Exception
	 */
	public void checkIntersectFace(Command command, Connection conn) throws Exception {
		CmgBuildlink cmglink = command.getCmglink();
		int linkpid = cmglink.getPid();
		Geometry g = command.getGeometry();

		List<CmgBuildface> faces = command.getCmgfaces();

		if (faces.size() == 0) {
			return;
		}

		CmgBuildlinkSelector linkselector = new CmgBuildlinkSelector(conn);

		for (CmgBuildface face : faces) {
			List<IRow> topos = face.getTopos();
			for (IRow row : topos) {
				CmgBuildfaceTopo topo = (CmgBuildfaceTopo) row;

				if (topo.getLinkPid() == linkpid) {
					continue;
				}

				CmgBuildlink link = (CmgBuildlink) linkselector.loadById(topo.getLinkPid(), true, false);
				if (g.crosses(GeoTranslator.transform(link.getGeometry(),0.00001,5))) {
					throwException("背景面不能自相交");
				}
			} // for
		} // for
	}
	
	private void throwException(String msg) throws Exception {
		throw new Exception(msg);
	}
}
