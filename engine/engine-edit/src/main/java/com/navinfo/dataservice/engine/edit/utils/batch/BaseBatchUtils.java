package com.navinfo.dataservice.engine.edit.utils.batch;

import java.lang.reflect.Method;
import java.sql.Connection;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameNodeSelector;
import com.navinfo.dataservice.engine.edit.utils.GeoRelationUtils;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @Title: BaseBatchUtils.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月17日 下午2:28:10
 * @version: v1.0
 */
public class BaseBatchUtils {

	private static RdSameNodeSelector rdSameNodeSelector = null;

	protected static RdSameNodeSelector getRdSameNodeSelector(Connection conn) throws Exception {
		if (null == rdSameNodeSelector)
			rdSameNodeSelector = new RdSameNodeSelector(conn);
		return rdSameNodeSelector;
	}

	private static RdNodeSelector rdNodeSelector = null;

	protected static RdNodeSelector getRdNodeSelector(Connection conn) throws Exception {
		if (null == rdNodeSelector)
			rdNodeSelector = new RdNodeSelector(conn);
		return rdNodeSelector;
	}

	public BaseBatchUtils() {
	}

	/**
	 * 判断是否同一点
	 * 
	 * @param conn
	 * @param nodePids
	 * @return
	 * @throws Exception
	 */
	protected static boolean isSameNode(Connection conn, Integer... nodePids) throws Exception {
		boolean result = true;
		IRow row = null;
		for (Integer nodePid : nodePids) {
			row = getRdSameNodeSelector(conn).loadByNodePidAndTableName(nodePid, "rd_node", false);
			if (null == row)
				return false;
		}
		return result;
	}

	/**
	 * 将传入几何缩小100000倍后返回
	 * 
	 * @param g
	 * @return
	 * @throws JSONException
	 */
	protected static Geometry shrink(Geometry g) throws JSONException {
		return GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(g, 0.00001, 5));
	}

	/**
	 * 判断点是否处于ring的组成线上
	 * 
	 * @param conn
	 * @param nodePid
	 * @param faceGeometry
	 * @return
	 * @throws Exception
	 */
	protected static boolean isInBoundary(Connection conn, Integer nodePid, Geometry faceGeometry) throws Exception {
		RdNode node = (RdNode) getRdNodeSelector(conn).loadById(nodePid, false);
		// 获取边界线几何
		Geometry faceBoundary = faceGeometry.getBoundary();
		return faceBoundary.distance(node.getGeometry()) <= 1;
	}

	/**
	 * 包含在ring内部，不考虑是否有交点在线上
	 * 
	 * @param linkGeometry
	 * @param faceGeometry
	 * @return
	 */
	protected static boolean isContainOrCover(Geometry linkGeometry, Geometry faceGeometry) {
		return GeoRelationUtils.Interior(linkGeometry, faceGeometry)
				|| GeoRelationUtils.InteriorAnd1Intersection(linkGeometry, faceGeometry)
				|| GeoRelationUtils.InteriorAnd2Intersection(linkGeometry, faceGeometry);
	}

	/**
	 * 获取IRow对象Geometry属性
	 * 
	 * @param row
	 * @return
	 * @throws Exception
	 */
	protected static Geometry loadGeometry(IRow row) throws Exception {
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

	/**
	 * IRow对象设置RegionId
	 * 
	 * @param row
	 * @param regionId
	 * @throws Exception
	 */
	protected static void setRegionId(IRow row, Integer regionId) throws Exception {
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
