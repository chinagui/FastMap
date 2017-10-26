package com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameLinkSelector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crayeres on 2017/3/3.
 */
public class Check {

    public void checkSameLink(ZoneNode node, Connection conn) throws Exception {
        RdSameLinkSelector selector = new RdSameLinkSelector(conn);
        List<RdSameLink> sameLinks = selector.loadSameLinkByNodeAndTableName(node.pid(), "ZONE_LINK", false);
        for (RdSameLink sameLink : sameLinks) {
            boolean flag = true;
            List<IRow> parts = sameLink.getParts();
            for (IRow partRow : parts) {
                RdSameLinkPart part = (RdSameLinkPart) partRow;
                String name = part.getTableName();
                if (name.equalsIgnoreCase("RD_LINK") || name.equalsIgnoreCase("AD_LINK")) {
                    flag = false;
                    break;
                }
            }
            if (!flag)
                throw new Exception("此link不是该组同一关系中的主要素，不能进行修形操作");
        }
    }

    /**
     * SHAPING_CHECK_FACE_SELFINTERSECT：不自相交的既有线构成的背景面，拖动背景node造成自相交
     * @param conn
     * @param command
     * @throws Exception
     */
	public void checkIntersectFace(Connection conn, Command command) throws Exception {
		if (command.getLinks() == null || command.getLinks().size() == 0) {
			return;
		}

		List<ZoneLink> newlinks = updateLinkGeometry(command);

		for (int i = 0; i < newlinks.size(); i++) {

			ZoneLink current = newlinks.get(i);

			if (current.getGeometry().isSimple() == false) {
				throw new Exception("背景面不能自相交");
			}

			for (int j = i + 1; j < newlinks.size(); j++) {

				ZoneLink next = newlinks.get(j);

				if (GeoTranslator.transform(current.getGeometry(), 0.00001, 5)
						.crosses(GeoTranslator.transform(next.getGeometry(), 0.00001, 5))) {
					throw new Exception("背景面不能自相交");
				}
			}
		}

		ZoneLinkSelector linkselector = new ZoneLinkSelector(conn);

		for (ZoneFace face : command.getFaces()) {

			List<IRow> topos = face.getFaceTopos();

			for (ZoneLink newlink : newlinks) {

				for (IRow row : topos) {

					ZoneFaceTopo topo = (ZoneFaceTopo) row;

					if (isContainTopoLink(topo.getLinkPid(), newlinks)) {

						continue;
					}

					ZoneLink link = (ZoneLink) linkselector.loadById(topo.getLinkPid(), true, false);
					if (newlink.getGeometry().crosses(GeoTranslator.transform(link.getGeometry(), 0.00001, 5))) {
						throw new Exception("背景面不能自相交");
					}
				} // for
			} // for
		} // for
	}

	private boolean isContainTopoLink(int linkPid, List<ZoneLink> links) {
		boolean contain = false;

		for (ZoneLink link : links) {
			if (link.getPid() == linkPid) {
				contain = true;
				break;
			}
		}

		return contain;
	}

	private List<ZoneLink> updateLinkGeometry(Command command) throws Exception {
		List<ZoneLink> newlink = new ArrayList<>();

		for (ZoneLink link : command.getLinks()) {
			int nodePid = command.getNodePid();

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

			ZoneLink zoneLink = new ZoneLink();
			zoneLink.setPid(link.getPid());
			zoneLink.setGeometry(newgeo);
			newlink.add(zoneLink);
		}
		return newlink;
	}
}
