package com.navinfo.dataservice.engine.edit.utils.batch;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.engine.edit.utils.GeoRelationUtils;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @Title: AdminIDBatchUtils.java
 * @Description: RdLink赋RegionId
 * @author zhangyt
 * @date: 2016年8月17日 下午2:52:23
 * @version: v1.0
 */
public class AdminIDBatchUtils extends BaseBatchUtils {

	public AdminIDBatchUtils() {
	}

	private static AdFaceSelector adFaceSelector = null;

	protected static AdFaceSelector getAdFaceSelector(Connection conn) throws Exception {
		if (null == adFaceSelector)
			adFaceSelector = new AdFaceSelector(conn);
		return adFaceSelector;
	}

	public static void updateZoneID(IRow row, Connection conn, Result result) throws Exception {
		if (row instanceof RdLink) {
			RdLink link = (RdLink) row;
			Geometry linkGeometry = shrink(link.getGeometry());
			AdFace face = loadFaceByGeometry(conn, linkGeometry);
			if (null == face)
				return;
			Integer regionId = face.getRegionId();
			Geometry faceGeometry = shrink(face.getGeometry());

			if (isContainOrCover(linkGeometry, faceGeometry)) {
				if (link.getLeftRegionId() != regionId) {
					link.changedFields().put("leftRegionId", regionId);
				} else if (link.getRightRegionId() != regionId) {
					link.changedFields().put("rightRegionId", regionId);
				}
				result.insertObject(link, ObjStatus.UPDATE, link.pid());
			} else if (GeoRelationUtils.Boundary(linkGeometry, faceGeometry)) {
				if (GeoRelationUtils.IsLinkOnLeftOfRing(linkGeometry, faceGeometry)) {
					if (link.getRightRegionId() != regionId) {
						link.changedFields().put("rightRegionId", regionId);
					}
				} else {
					if (link.getLeftRegionId() != regionId) {
						link.changedFields().put("leftRegionId", regionId);
					}
				}
				result.insertObject(link, ObjStatus.UPDATE, link.pid());
			} else {

			}
		} else {
			Geometry geometry = shrink(loadGeometry(row));
			AdFace face = loadFaceByGeometry(conn, geometry);
			if (null == face)
				return;
			Geometry faceGeometry = shrink(face.getGeometry());
			if (isContainOrCover(geometry, faceGeometry)) {
//				setRegionId(row, face.getRegionId());
				row.changedFields().put("regionId", face.getRegionId());
			}
			result.insertObject(row, ObjStatus.UPDATE, row.parentPKValue());
		}
	}

	private static AdFace loadFaceByGeometry(Connection conn, Geometry linkGeometry) throws Exception {
		List<AdFace> faces = getAdFaceSelector(conn).loadRelateFaceByGeometry(linkGeometry);
		if (faces.isEmpty() || faces.size() > 1) {
			return null;
		}
		return faces.get(0);
	}

	private static Geometry loadGeometry(IRow row) throws Exception {
		Class<?> clazz;
		try {
			clazz = Class.forName(row.getClass().getName());
			Method method = clazz.getMethod("getGeometry");
			return (Geometry) method.invoke(row);
		} catch (Exception e) {
			throw new Exception(
					"PID为" + row.parentPKValue() + "的" + row.getClass().getSimpleName() + "对象没有找到Geometry属性");
		}
	}

	private static void setRegionId(IRow row, Integer regionId) throws Exception {
		Class<?> clazz;
		try {
			clazz = Class.forName(row.getClass().getName());
			Method method = clazz.getMethod("setRegionId");
			method.invoke(row, regionId);
		} catch (Exception e) {
			throw new Exception("PID为" + row.parentPKValue() + "的" + row.getClass().getSimpleName() + "对象设置RegionId失败");
		}
	}
}
