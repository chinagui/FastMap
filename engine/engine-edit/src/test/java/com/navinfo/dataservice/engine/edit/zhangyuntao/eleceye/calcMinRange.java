package com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye;

import org.junit.Test;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * @Title: calcMinRange.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月28日 上午10:32:15
 * @version: v1.0
 */
public class calcMinRange extends InitApplication {

	public calcMinRange() {
	}

	@Override
	public void init() {
		super.initContext();
	}

	@Test
	public Coordinate GetNearestPointOnLine(Coordinate point, RdLink path) {
		Coordinate[] coll = path.getGeometry().getCoordinates();

		Coordinate targetPoint = new Coordinate();

		double minDistance = 0;

		if (coll.length < 2) {
			return null;
		}
		targetPoint = coll[0];
		// 计算线段的第一点与电子眼的距离
		minDistance = GeometryUtils.getDistance(point, targetPoint);
		for (int i = 0; i < coll.length - 1; i++) {
			Coordinate point1 = new Coordinate();
			Coordinate point2 = new Coordinate();
			Coordinate pedalPoint = new Coordinate();

			point1 = coll[i];
			point2 = coll[i + 1];

			pedalPoint = GetPedalPoint(point1, point2, point);

			boolean isPointAtLine = IsPointAtLineInter(point1, point2, pedalPoint);

			// 如果在线上
			if (isPointAtLine) {
				double pedalLong = GeometryUtils.getDistance(point, pedalPoint);
				if (pedalLong < minDistance) {
					minDistance = pedalLong;
					targetPoint = pedalPoint;
				}
			} else {
				// 计算与点1的最小距离
				double long1 = GeometryUtils.getDistance(point1, point);
				// 计算与点2的最小距离
				double long2 = GeometryUtils.getDistance(point2, point);
				if (long1 <= long2) {
					if (long1 < minDistance) {
						minDistance = long1;
						targetPoint = point1;
					}
				} else {
					if (long2 < minDistance) {
						minDistance = long2;
						targetPoint = point2;
					}
				}
			}
		}
		return targetPoint;
	}

	// 计算垂足点
	public static Coordinate GetPedalPoint(Coordinate point1, Coordinate point2, Coordinate point) {
		Coordinate targetPoint = new Coordinate();

		double x1, x2, y1, y2;
		x1 = point1.x;
		y1 = point1.y;
		x2 = point2.x;
		y2 = point2.y;

		if (x1 == x2 && y1 == y2) {
			return null;
		} else if (x1 == x2) {
			targetPoint.x = x1;
			targetPoint.y = point.y;
		} else if (y1 == y2) {
			targetPoint.x = point.x;
			targetPoint.y = y1;
		} else {
			double k = (y2 - y1) / (x2 - x1);
			double x = (k * k * x1 + k * (point.y - y1) + point.x) / (k * k + 1);
			double y = k * (x - x1) + y1;

			targetPoint.x = x;
			targetPoint.y = y;
		}
		return targetPoint;
	}

	/**
	 * 判断点point是否在point1和point2组成的线上
	 */
	public static boolean IsPointAtLineInter(Coordinate point1, Coordinate point2, Coordinate point) {
		boolean result = false;
		double x1, x2, y1, y2, x, y;

		x1 = point1.x;
		y1 = point1.y;
		x2 = point2.x;
		y2 = point2.y;
		x = point.x;
		y = point.y;

		if (x >= min(x1, x2) && x <= max(x1, x2) && y >= min(y1, y2) && y <= max(y1, y2)) {
			result = true;
		}
		return result;
	}

	/**
	 * 判断两点的最小值
	 * 
	 * @param x1
	 * @param x2
	 * @return
	 */
	private static double min(double x1, double x2) {
		if (x1 > x2)
			return x2;
		else
			return x1;
	}

	/**
	 * 判断两点的最大值
	 * 
	 * @param x1
	 * @param x2
	 * @return
	 */
	private static double max(double x1, double x2) {
		if (x1 < x2)
			return x2;
		else
			return x1;
	}

}
