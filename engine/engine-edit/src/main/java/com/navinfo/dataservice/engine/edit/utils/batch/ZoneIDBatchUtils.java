package com.navinfo.dataservice.engine.edit.utils.batch;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkZone;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.engine.edit.utils.GeoRelationUtils;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @Title: ZoneIDBatchUtils.java
 * @Description: RdLink赋ZoneID
 * @author zhangyt
 * @date: 2016年8月17日 下午2:52:23
 * @version: v1.0
 */
public class ZoneIDBatchUtils extends BaseBatchUtils {

	public ZoneIDBatchUtils() {
	}

	private static ZoneFaceSelector zoneFaceSelector = null;

	protected static ZoneFaceSelector getZoneFaceSelector(Connection conn) throws Exception {
		if (null == zoneFaceSelector)
			zoneFaceSelector = new ZoneFaceSelector(conn);
		return zoneFaceSelector;
	}

	public static void updateZoneID(RdLink link, Connection conn, Result result) throws Exception {
		Geometry linkGeometry = shrink(link.getGeometry());
		List<ZoneFace> faces = getZoneFaceSelector(conn).loadRelateFaceByGeometry(linkGeometry);
		if (faces.isEmpty() || faces.size() > 1) {
			return;
		}
		ZoneFace zoneFace = faces.get(0);
		Geometry faceGeometry = shrink(zoneFace.getGeometry());
		if (isContainOrCover(linkGeometry, faceGeometry)) {
			for (IRow row : link.getZones()) {
				RdLinkZone linkZone = (RdLinkZone) row;
				if (zoneFace.getRegionId() != linkZone.getRegionId()) {
					linkZone.changedFields().put("regionId", zoneFace.getRegionId());
					result.insertObject(linkZone, ObjStatus.UPDATE, linkZone.parentPKValue());
				}
			}

		} else if (GeoRelationUtils.Boundary(linkGeometry, faceGeometry)) {
			RdLinkZone linkZone = null;
			if (GeoRelationUtils.IsLinkOnLeftOfRing(linkGeometry, faceGeometry)) {
				boolean isNew = false;
				for (IRow row : link.getZones()) {
					linkZone = (RdLinkZone) row;
					if (linkZone.getRegionId() != linkZone.getRegionId() && linkZone.getSide() == 1) {
						isNew = true;
						break;
					}
				}
				if (isNew) {
					linkZone = new RdLinkZone();
					linkZone.setLinkPid(link.pid());
					linkZone.setRegionId(zoneFace.getRegionId());
					linkZone.setSide(1);
					result.insertObject(linkZone, ObjStatus.INSERT, linkZone.parentPKValue());
				}
			} else {
				boolean isNew = false;
				for (IRow row : link.getZones()) {
					linkZone = (RdLinkZone) row;
					if (linkZone.getRegionId() != linkZone.getRegionId() && linkZone.getSide() == 0) {
						isNew = true;
					}
				}
				if (isNew) {
					linkZone = new RdLinkZone();
					linkZone.setLinkPid(link.pid());
					linkZone.setRegionId(zoneFace.getRegionId());
					linkZone.setSide(0);
					result.insertObject(linkZone, ObjStatus.INSERT, linkZone.parentPKValue());
				}
			}
		} else {

		}
	}

}
