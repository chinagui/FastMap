package com.navinfo.dataservice.engine.edit.operation.topo.move.movecmgnode;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildfaceTopo;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildlinkSelector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class Check {
	 /**
     * SHAPING_CHECK_FACE_SELFINTERSECT：不自相交的既有线构成的背景面，拖动背景node造成自相交
     * @param conn
     * @param command
     * @throws Exception
     */
	public void checkIntersectFace(Connection conn, Command command) throws Exception {
		if (command.getCmglinks() == null || command.getCmglinks().size() == 0) {
			return;
		}

		List<CmgBuildlink> newlinks = updateLinkGeometry(command);

		for (int i = 0; i < newlinks.size(); i++) {

			CmgBuildlink current = newlinks.get(i);

			if (current.getGeometry().isSimple() == false) {
				throw new Exception("背景面不能自相交");
			}

			for (int j = i + 1; j < newlinks.size(); j++) {

				CmgBuildlink next = newlinks.get(j);

				if (GeoTranslator.transform(current.getGeometry(), 0.00001, 5)
						.crosses(GeoTranslator.transform(next.getGeometry(), 0.00001, 5))) {
					throw new Exception("背景面不能自相交");
				}
			}
		}

		CmgBuildlinkSelector linkselector = new CmgBuildlinkSelector(conn);

		for (CmgBuildface face : command.getCmgfaces()) {

			List<IRow> topos = face.getTopos();

			for (CmgBuildlink newlink : newlinks) {

				for (IRow row : topos) {

					CmgBuildfaceTopo topo = (CmgBuildfaceTopo) row;

					if (isContainTopoLink(topo.getLinkPid(), newlinks)) {

						continue;
					}

					CmgBuildlink link = (CmgBuildlink) linkselector.loadById(topo.getLinkPid(), true, false);
					if (newlink.getGeometry().crosses(GeoTranslator.transform(link.getGeometry(), 0.00001, 5))) {
						throw new Exception("背景面不能自相交");
					}
				} // for
			} // for
		} // for
	}

	private boolean isContainTopoLink(int linkPid, List<CmgBuildlink> links) {
		boolean contain = false;

		for (CmgBuildlink link : links) {
			if (link.getPid() == linkPid) {
				contain = true;
				break;
			}
		}

		return contain;
	}

	private List<CmgBuildlink> updateLinkGeometry(Command command) throws Exception {
		List<CmgBuildlink> newlink = new ArrayList<>();

		for (CmgBuildlink link : command.getCmglinks()) {
			CmgBuildnode node = command.getCmgnode();
			
			int nodePid = node.getPid();

			double lon = command.getLongitude();

			double lat = command.getLatitude();

			Geometry geom = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
			Coordinate[] cs = geom.getCoordinates();
			Coordinate[] ps = new Coordinate[cs.length];

			for (int i = 0; i < cs.length; i++) {
				ps[i] = new Coordinate();

				ps[i].x = cs[i].x;

				ps[i].y = cs[i].y;
			}

			if (link.getsNodePid() == nodePid) {
				ps[0].x = lon;

				ps[0].y = lat;
			}
			if (link.geteNodePid() == nodePid) {
				ps[ps.length - 1].x = lon;

				ps[ps.length - 1].y = lat;
			}

			LineString newgeo = GeoTranslator.createLineString(ps);

			CmgBuildlink adLink = new CmgBuildlink();
			adLink.setPid(link.getPid());
			adLink.setGeometry(newgeo);
			newlink.add(adLink);
		}
		return newlink;
	}
}
